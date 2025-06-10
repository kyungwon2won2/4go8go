package com.example.demo.domain.post.service;

import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.post.dto.MyProductDto;
import com.example.demo.domain.post.helper.ImageHelper;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.stringcode.ProductCategory;
import com.example.demo.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyProductService {
    
    private final ProductMapper productMapper;
    private final ImageHelper imageHelper;
    private final ChatService chatService;
    
    /**
     * 사용자가 등록한 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public List<MyProductDto> getMyProducts(int userId, int offset, int limit, 
                                          ProductCategory category, String search, 
                                          String sort, String status) {
        // 상품 상태 변환
        Product.TradeStatus tradeStatus = convertStatus(status);
        
        // 정렬 방식 결정
        String orderBy = determineOrderBy(sort);
        
        // 상품 목록 조회
        List<MyProductDto> products = productMapper.selectMyProducts(
                userId, offset, limit, category, search, orderBy, tradeStatus);
        
        // 이미지 URL 및 추가 정보 설정
        for (MyProductDto product : products) {
            // 대표 이미지 설정
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            if (imageUrl == null) {
                imageUrl = "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            }
            product.setImageUrl(imageUrl);
            
            // 채팅방 수 설정 (이미 쿼리에서 처리했지만, 필요시 여기서 다시 설정)
            if (product.getChatCount() <= 0) {
                int chatCount = chatService.countChatRoom(product.getPostId());
                product.setChatCount(chatCount);
            }
        }
        
        return products;
    }
    
    /**
     * 사용자가 등록한 상품 수 조회
     */
    @Transactional(readOnly = true)
    public int getMyProductsCount(int userId, ProductCategory category, 
                                String search, String status) {
        // 상품 상태 변환
        Product.TradeStatus tradeStatus = convertStatus(status);
        
        // 상품 수 조회
        return productMapper.countMyProducts(userId, category, search, tradeStatus);
    }
    
    /**
     * 상태 문자열을 TradeStatus 열거형으로 변환
     */
    private Product.TradeStatus convertStatus(String status) {
        if (status == null) {
            return null;
        }
        
        switch (status) {
            case "available":
                return Product.TradeStatus.AVAILABLE;
            case "completed":
                return Product.TradeStatus.COMPLETED;
            default:
                return null;
        }
    }
    
    /**
     * 정렬 방식 문자열을 SQL 정렬 표현식으로 변환
     */
    private String determineOrderBy(String sort) {
        if (sort == null) {
            return "p.created_at DESC"; // 기본값: 최신순
        }
        
        switch (sort) {
            case "oldest":
                return "p.created_at ASC";
            case "price-high":
                return "pr.price DESC";
            case "price-low":
                return "pr.price ASC";
            case "views":
                return "p.view_count DESC";
            default:
                return "p.created_at DESC";
        }
    }

    // 구매한 상품 조회
    public List<MyProductDto> getPurchasedProducts(int userId, int offset, int limit,
                                                   ProductCategory category, String search, String sort) {
        // 정렬 방식 결정
        String orderBy = determineOrderBy(sort);

        // 구매한 상품 목록 조회 (buyerId = userId)
        List<MyProductDto> products = productMapper.selectPurchasedProducts(
                userId, offset, limit, category, search, orderBy);

        // 이미지 URL 및 추가 정보 설정
        for (MyProductDto product : products) {
            // 대표 이미지 설정
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            if (imageUrl == null) {
                imageUrl = "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            }
            product.setImageUrl(imageUrl);

            // 리뷰 작성 가능 여부 확인 (추후 구현)
            // boolean canWriteReview = reviewService.canWriteReview(product.getPostId(), userId);
            // product.setCanWriteReview(canWriteReview);
        }

        return products;
    }

    public int getPurchasedProductsCount(int userId, ProductCategory category, String search) {
        return productMapper.countPurchasedProducts(userId, category, search);
    }
}
