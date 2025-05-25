package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.CreateProductDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.dto.UpdateProductDto;
import com.example.demo.domain.post.helper.ImageHelper;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.mapper.ImageMapper;
import com.example.demo.mapper.ProductMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final ImageHelper imageHelper;

    private final PostService postService;

    public List<ProductListDto> getProductsByPage(int offset, int limit) {
        List<ProductListDto> products = productMapper.selectByPage(offset, limit);

        for (ProductListDto product : products) {
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            imageUrl = (imageUrl != null)
                    ? imageUrl
                    : "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            product.setImageUrl(imageUrl);
        }

        return products;
    }

    // 카테고리별 상품 조회
    public List<ProductListDto> getProductsByPageAndCategory(int offset, int limit, com.example.demo.domain.stringcode.ProductCategory category) {
        List<ProductListDto> products = productMapper.selectByPageAndCategory(offset, limit, category);

        for (ProductListDto product : products) {
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            imageUrl = (imageUrl != null)
                    ? imageUrl
                    : "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            product.setImageUrl(imageUrl);
        }

        return products;
    }

    @Transactional
    public void addProduct(CreateProductDto productDto, CustomerUser loginUser) {
        // Post, Product 객체 생성 및 매핑
        Post post = new Post();
        post.setPostCategoryId(1); // product 구분
        post.setUserId(loginUser.getUserId());
        post.setTitle(productDto.getTitle());
        post.setContent(productDto.getContent());
        post.setCreatedAt(new Date());
        postService.insertPost(post, loginUser.getUserId());

        Product product = new Product();
        product.setPostId(post.getPostId());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setTradeStatus(Product.TradeStatus.AVAILABLE);
        product.setCondition(productDto.getCondition());

        productMapper.insert(product);

        // 이미지 업로드 및 DB 저장
        imageHelper.productImageSave(productDto.getImageFiles(), post.getPostId());
    }
    public UpdateProductDto getProductByPostId(int postId) {
        UpdateProductDto dto = productMapper.selectByUpdateProductDto(postId);
        List<Image> images = imageHelper.getImagesByPostId(postId);
        dto.setImage(images); // 이미지 목록 추가
        return dto;
    }

    @Transactional
    public void updateProduct(int postId, UpdateProductDto dto, MultipartFile[] imageFiles, String deletedImageIds) {
        // 1. post 테이블 업데이트
        postService.updatePostContentAndTitle(postId, dto.getTitle(), dto.getContent());

        // 2. product 테이블 업데이트
        productMapper.updateProductDetails(postId, dto.getPrice(), dto.getCondition(), dto.getCategory());

        // 3. 삭제할 이미지들 처리
        if (deletedImageIds != null && !deletedImageIds.trim().isEmpty()) {
            String[] imageIdsArray = deletedImageIds.split(",");
            for (String imageIdStr : imageIdsArray) {
                try {
                    Long imageId = Long.parseLong(imageIdStr.trim());
                    
                    // imageId로 Image 객체를 조회하여 URL 정보 가져오기
                    Image image = imageHelper.getImageById(imageId);
                    if (image != null && image.getUrl() != null) {
                        // S3에서 파일 삭제 + DB에서 레코드 삭제
                        imageHelper.deleteImageById(image.getUrl(), imageId);
                    } else {
                        // Image 객체가 없거나 URL이 없는 경우 로그만 출력
                        System.err.println("Image not found or URL is null for imageId: " + imageId);
                    }
                } catch (NumberFormatException e) {
                    // 잘못된 ID 형식은 무시
                    System.err.println("Invalid image ID format: " + imageIdStr);
                }
            }
        }

        // 4. 새로운 이미지들 업로드
        if (imageFiles != null && imageFiles.length > 0) {
            imageHelper.productImageSave(imageFiles, postId);
        }
    }

    @Transactional
    public void deleteProduct(int postId) {
        // 1. 이미지 파일들 먼저 삭제 (S3에서도 삭제)
        List<Image> images = imageHelper.getImagesByPostId(postId);
        if (images != null && !images.isEmpty()) {
            for (Image image : images) {
                // Image 객체에서 URL과 ID를 모두 가져와서 삭제
                if (image.getUrl() != null) {
                    imageHelper.deleteImageById(image.getUrl(), image.getImageId());
                }
            }
        }
        
        // 2. 데이터베이스에서 관련 테이블들 순서대로 삭제
        // 외래키 제약조건을 고려하여 순서대로 삭제
        productMapper.delete(postId);          // 상품 테이블
        postService.deletePostById(postId);    // 게시글
    }

    public ProductDetailDto getProductDetailByPostId(int postId) {
        // 1. 상품 상세 정보 조회
        ProductDetailDto detailDto = productMapper.selectByPostIdDetail(postId);
        List<Image> images = new ArrayList<>();
        // 2. 이미지 목록 조회 및 URL만 추출하여 세팅
        if (detailDto != null) {
            images = imageHelper.getImagesByPostId(postId);
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = images.stream()
                        .map(Image::getUrl)
                        .collect(Collectors.toList());
                detailDto.setImageUrls(imageUrls);
            } else {
                detailDto.setImageUrls(Collections.emptyList());
            }
        }
        // 조회수 증가
        postService.incrementViewCount(postId);

        return detailDto;
    }

    // 상품 전체 개수 반환
    public int getTotalProductCount() {
        return productMapper.countAllProducts();
    }

    // 카테고리별 상품 개수 반환
    public int getTotalProductCountByCategory(com.example.demo.domain.stringcode.ProductCategory category) {
        return productMapper.countProductsByCategory(category);
    }

    // 메인 페이지용: 조회수가 높은 상품 4개 조회
    public List<ProductListDto> getTopViewedProducts() {
        List<ProductListDto> products = productMapper.selectTopViewedProducts();
        
        // 이미지 URL 설정
        for (ProductListDto product : products) {
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            imageUrl = (imageUrl != null)
                    ? imageUrl
                    : "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            product.setImageUrl(imageUrl);
        }
        
        return products;
    }

    // 메인 페이지용: 가격이 저렴한 상품 4개 조회
    public List<ProductListDto> getCheapestProducts() {
        List<ProductListDto> products = productMapper.selectCheapestProducts();
        
        // 이미지 URL 설정
        for (ProductListDto product : products) {
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            imageUrl = (imageUrl != null)
                    ? imageUrl
                    : "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            product.setImageUrl(imageUrl);
        }
        
        return products;
    }

    // 검색 기능
    public List<ProductListDto> getProductsBySearch(int offset, int limit, String keyword) {
        List<ProductListDto> products = productMapper.selectBySearchKeyword(offset, limit, keyword);

        for (ProductListDto product : products) {
            String imageUrl = imageHelper.selectFirstImageByPostId(product.getPostId());
            imageUrl = (imageUrl != null)
                    ? imageUrl
                    : "https://4go8go-bucket.s3.ap-northeast-2.amazonaws.com/post-images/253a2530-c603-4a0e-8156-ace42e72bd45.png";
            product.setImageUrl(imageUrl);
        }

        return products;
    }

    // 검색 결과 개수 반환
    public int getTotalProductCountBySearch(String keyword) {
        return productMapper.countProductsBySearch(keyword);
    }
}
