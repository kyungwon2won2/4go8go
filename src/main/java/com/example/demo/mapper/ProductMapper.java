package com.example.demo.mapper;

import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.dto.UpdateProductDto;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.stringcode.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {

    void insert(Product product);

    Product selectByPostId(@Param("postId") int postId);

    UpdateProductDto selectByUpdateProductDto(@Param("postId") int postId);

    ProductDetailDto selectProductDetail(int postId);

    List<ProductListDto> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

    void update(Product product);

    void delete(@Param("postId") int postId);

    ProductDetailDto selectByPostIdDetail(@Param("postId") int postId);

    void updateProductDetails(@Param("postId") int postId,
                              @Param("price") int price,
                              @Param("condition") Product.ProductCondition condition,
                              @Param("category") ProductCategory category);

    // 상품 전체 개수 반환
    int countAllProducts();

    // 조회수가 높은 상품 4개 조회 (메인 페이지용)
    List<ProductListDto> selectTopViewedProducts();

    // 가격이 저렴한 상품 4개 조회 (메인 페이지용)  
    List<ProductListDto> selectCheapestProducts();
}
