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

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductMapper productMapper;
    private final ImageHelper imageHelper;
    private final PostMapper postMapper;

    public List<ProductListDto> getProductsByPage(int offset, int limit) {
        return productMapper.selectByPage(offset, limit);
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
        ProductDetailDto detailDto = productMapper.selectByPostIdDetail(postId);
        // 이미지 URL 리스트 추가
        List<Image> images = imageHelper.getImagesByPostId(postId);
        if (detailDto != null) {
            detailDto.setImageUrls(images.stream().map(Image::getUrl).toList());
        }
        return detailDto;
    }
}
