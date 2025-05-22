package com.example.demo.domain.post.service;

import com.example.demo.domain.post.dto.CreateProductDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.helper.ImageHelper;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.post.model.Image;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.mapper.PostMapper;
import com.example.demo.mapper.ProductMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final ImageHelper imageHelper;
    private final PostMapper postMapper;

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

    @Transactional
    public void addProduct(CreateProductDto productDto, CustomerUser loginUser) {
        // Post, Product 객체 생성 및 매핑
        Post post = new Post();
        post.setPostCategoryId(1); // product 구분
        post.setUserId(loginUser.getUserId());
        post.setTitle(productDto.getTitle());
        post.setContent(productDto.getContent());
        post.setCreatedAt(new Date());
        postMapper.insertPost(post);

        Product product = new Product();
        product.setPostId(post.getPostId());
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setTradeStatus(Product.TradeStatus.AVAILABLE);
        product.setCondition(productDto.getCondition());

        productMapper.insert(product);

        // 이미지 업로드 및 DB 저장
//        imageHelper.productImageSave(productDto.getImageFiles(), post.getPostId());
    }

    public Product getProductByPostId(int postId) {
        return productMapper.selectByPostId(postId);
    }

    public void updateProduct(int postId, CreateProductDto productDto) {
        Product product = productMapper.selectByPostId(postId);
        if (product != null) {
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setCondition(productDto.getCondition());
            productMapper.update(product);
        }
    }

    public void deleteProduct(int postId) {
        productMapper.delete(postId);
    }

    public ProductDetailDto getProductDetailByPostId(int postId) {
        // 1. 상품 상세 정보 조회
        ProductDetailDto detailDto = productMapper.selectByPostIdDetail(postId);

        // 2. 이미지 목록 조회 및 URL만 추출하여 세팅
        if (detailDto != null) {
            List<Image> images = imageHelper.getImagesByPostId(postId);
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = images.stream()
                        .map(Image::getUrl)
                        .collect(Collectors.toList());
                detailDto.setImageUrls(imageUrls);
            } else {
                detailDto.setImageUrls(Collections.emptyList());
            }
        }

        return detailDto;
    }

    // 상품 전체 개수 반환
    public int getTotalProductCount() {
        return productMapper.countAllProducts();
    }
}
