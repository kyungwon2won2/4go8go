package com.example.demo.domain.post.DTO;

import com.example.demo.domain.post.model.Product;
import lombok.Data;

@Data
public class CreateProductDto {
    private int postId;
    private int userId;
    private Product.Category category;
    private int price;
    private Product.TradeStatus tradeStatus;
    private Product.ProductCondition condition;
    // location(거래위치)는 user.address에서 가져오므로 별도 필드 불필요
}
