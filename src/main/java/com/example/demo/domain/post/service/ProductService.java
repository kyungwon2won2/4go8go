package com.example.demo.domain.post.service;

import com.example.demo.domain.post.DTO.CreateProductDto;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductMapper productMapper;

    @Autowired
    public ProductService(ProductMapper productMapper) {
        this.productMapper = productMapper;
    }

    public List<Product> getAllProducts() {
        return productMapper.selectAll();
    }

    public void addProduct(CreateProductDto productDto, CustomerUser loginUser) {
        // Post, Product 객체 생성 및 매핑
        Post post = new Post();
        post.setPostCategoryId(1); // product 구분
        post.setUserId(loginUser.getUserId());
        // 기타 post 필드 세팅 필요시 추가

        Product product = new Product();
        product.setPost(post);
        product.setCategory(productDto.getCategory());
        product.setPrice(productDto.getPrice());
        product.setTradeStatus(productDto.getTradeStatus());
        product.setCondition(productDto.getCondition());
        product.setLocation(loginUser.getUser().getAddress());

        productMapper.insert(product);
    }

    public Product getProductByPostId(int postId) {
        return productMapper.selectByPostId(postId);
    }

    public void updateProduct(int postId, CreateProductDto productDto) {
        Product product = productMapper.selectByPostId(postId);
        if (product != null) {
            product.setCategory(productDto.getCategory());
            product.setPrice(productDto.getPrice());
            product.setTradeStatus(productDto.getTradeStatus());
            product.setCondition(productDto.getCondition());
            productMapper.update(product);
        }
    }

    public void deleteProduct(int postId) {
        productMapper.delete(postId);
    }
}
