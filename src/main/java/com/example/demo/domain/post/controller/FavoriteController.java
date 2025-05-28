package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.GeneralPostDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.service.FavoriteService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/favorite")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggleFavorite(
            @RequestParam int postId,
            @AuthenticationPrincipal CustomerUser loginUser){

        if (loginUser == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        boolean favorited = favoriteService.toggleFavorite(postId, loginUser.getUserId());
        int favoriteCount = favoriteService.getFavoriteCount(postId);

        return Map.of(
                "favorited", favorited,
                "count", favoriteCount
        );
    }

    @GetMapping("/favoritedList")
    public String myFavorites(@RequestParam(defaultValue="1") int page,
                              Model model,
                              @AuthenticationPrincipal CustomerUser loginUser){
        if (loginUser == null) {
            return "redirect:/login";
        }

        int pageSize = 20;
        int offset = (page - 1) * pageSize;

        // 1. 찜한 상품 가져오기
        List<ProductListDto> favoritedProducts = favoriteService.getFavoritedProductsPaged(loginUser.getUserId(), offset, pageSize);

        // 2. 찜한 상품 전체 개수
        int totalCount = favoriteService.getFavoritedProductsCountByUserId(loginUser.getUserId());

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;

        // 3. 모델 설정
        model.addAttribute("favoritedProducts", favoritedProducts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("categoryDisplayName", "내가 찜한 물품게시글");
        model.addAttribute("selectedCategory", null);
        model.addAttribute("selectedCategoryName", null);

        return "product/myFavoritedList";
    }
}
