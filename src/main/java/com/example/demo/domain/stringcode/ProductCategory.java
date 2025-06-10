package com.example.demo.domain.stringcode;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ProductCategory {
    ELECTRONICS("전자제품"),
    CLOTHING("의류"),
    FURNITURE("가구"),
    BOOKS("도서"),
    SPORTS("스포츠/레저"),
    VEHICLES("차량/오토바이"),
    MUSICAL_INSTRUMENTS("악기"),
    HOUSEHOLD_ITEMS("생활용품"),
    GAMES_TOYS("게임/완구"),
    BEAUTY("뷰티/미용"),
    PET_SUPPLIES("반려동물용품"),
    BABY_KIDS("유아/아동용품"),
    FOOD("식품"),
    PLANTS("식물"),
    ANTIQUES("골동품/수집품"),
    OTHERS("기타");
    
    private final String koreanName;
    
    ProductCategory(String koreanName) {
        this.koreanName = koreanName;
    }
    
    public String getKoreanName() {
        return koreanName;
    }
    
    public String getEnglishName() {
        return this.name();
    }
    
    /**
     * 한글명으로 ProductCategory를 찾는 메서드
     * @param koreanName 한글 카테고리명
     * @return 해당하는 ProductCategory, 없으면 null
     */
    public static ProductCategory fromKoreanName(String koreanName) {
        return Arrays.stream(values())
                .filter(category -> category.koreanName.equals(koreanName))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 모든 카테고리의 한글명 리스트를 반환
     * @return 한글 카테고리명 리스트
     */
    public static List<String> getKoreanNames() {
        return Arrays.stream(values())
                .map(ProductCategory::getKoreanName)
                .collect(Collectors.toList());
    }
    
    /**
     * Thymeleaf에서 사용하기 위한 모든 카테고리 반환
     * @return ProductCategory 배열
     */
    public static ProductCategory[] getAllCategories() {
        return values();
    }
    
    @Override
    public String toString() {
        return koreanName;
    }
}