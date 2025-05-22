package com.example.demo.domain.post.model;

import lombok.Data;

@Data
public class Product {
    private int postId;
    private Category category; // 물품 카테고리
    private int price; // 가격
    private TradeStatus tradeStatus; // 거래상태(거래가능, 거래중, 거래완료)
    private ProductCondition condition; // 상품상태(상, 중, 하)

    public enum Category {
        CLOTHING("의류"), FOOD("식료품"), BABY("유아용품");
        private final String displayName;
        Category(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    public enum TradeStatus {
        AVAILABLE("거래가능"), IN_PROGRESS("거래중"), COMPLETED("거래완료");
        private final String displayName;
        TradeStatus(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }
    public enum ProductCondition {
        HIGH("상"), MEDIUM("중"), LOW("하");
        private final String displayName;
        ProductCondition(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

}
