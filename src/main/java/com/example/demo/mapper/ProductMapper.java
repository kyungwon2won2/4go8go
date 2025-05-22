package com.example.demo.mapper;

import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.model.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    void insert(Product product);

    Product selectByPostId(@Param("postId") int postId);

    ProductDetailDto selectProductDetail(int postId);

    List<ProductListDto> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    void update(Product product);

    void delete(@Param("postId") int postId);

    ProductDetailDto selectByPostIdDetail(@Param("postId") int postId);

    // 상품 전체 개수 반환
    int countAllProducts();
}
