package com.example.demo.mapper;

import com.example.demo.domain.post.dto.MyProductDto;
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

    // 카테고리별 상품 목록 조회 (페이징)
    List<ProductListDto> selectByPageAndCategory(@Param("offset") int offset, @Param("limit") int limit, @Param("category") ProductCategory category);

    void update(Product product);

    void delete(@Param("postId") int postId);

    ProductDetailDto selectByPostIdDetail(@Param("postId") int postId);

    void updateProductDetails(@Param("postId") int postId,
                              @Param("price") int price,
                              @Param("condition") Product.ProductCondition condition,
                              @Param("category") ProductCategory category);

    // 상품 전체 개수 반환
    int countAllProducts();

    // 카테고리별 상품 개수 반환
    int countProductsByCategory(@Param("category") ProductCategory category);

    // 조회수가 높은 상품 4개 조회 (메인 페이지용)
    List<ProductListDto> selectTopViewedProducts();

    // 가격이 저렴한 상품 4개 조회 (메인 페이지용)  
    List<ProductListDto> selectCheapestProducts();

    // 검색 기능 - 제목과 내용으로 검색
    List<ProductListDto> selectBySearchKeyword(@Param("offset") int offset, @Param("limit") int limit, @Param("keyword") String keyword);

    // 검색 결과 개수 반환
    int countProductsBySearch(@Param("keyword") String keyword);

    /**
     * 상품 상태 변경
     */
    int updateProductStatus(@Param("postId") int postId, @Param("tradeStatus") Product.TradeStatus status);
    
    //내 상품 목록 조회
    List<MyProductDto> selectMyProducts(
            @Param("userId") int userId, 
            @Param("offset") int offset, 
            @Param("limit") int limit, 
            @Param("category") ProductCategory category,
            @Param("search") String search,
            @Param("orderBy") String orderBy,
            @Param("tradeStatus") Product.TradeStatus tradeStatus);
    
    //내 상품 수 조회
    int countMyProducts(
            @Param("userId") int userId,
            @Param("category") ProductCategory category,
            @Param("search") String search,
            @Param("tradeStatus") Product.TradeStatus tradeStatus);

    //구매 상태 수정
    int updateProductStatusWithBuyer(@Param("postId") int postId,
                                     @Param("buyerId") int buyerId,
                                     @Param("tradeStatus") Product.TradeStatus tradeStatus);

    //구매자 선택
    List<MyProductDto> selectPurchasedProducts(@Param("userId") int userId,
                                               @Param("offset") int offset,
                                               @Param("limit") int limit,
                                               @Param("category") ProductCategory category,
                                               @Param("search") String search,
                                               @Param("orderBy") String orderBy);

    //구매한 상품수
    int countPurchasedProducts(@Param("userId") int userId,
                               @Param("category") ProductCategory category,
                               @Param("search") String search);
}
