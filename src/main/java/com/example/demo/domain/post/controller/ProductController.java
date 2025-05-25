package com.example.demo.domain.post.controller;

import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.post.dto.CreateProductDto;
import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.dto.UpdateProductDto;
import com.example.demo.domain.post.service.ProductService;
import com.example.demo.domain.stringcode.ProductCategory;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final ChatService chatService;

    // 상품 list 가져오기 (카테고리 필터링 포함)
    @GetMapping
    public String productList(@RequestParam(defaultValue = "1") int page, 
                            @RequestParam(required = false) ProductCategory category, 
                            Model model) {
        int pageSize = 20;
        int offset = (page - 1) * pageSize;

        List<ProductListDto> products;
        int totalCount;
        
        if (category != null) {
            // 카테고리별 상품 조회
            products = productService.getProductsByPageAndCategory(offset, pageSize, category);
            totalCount = productService.getTotalProductCountByCategory(category);
        } else {
            // 전체 상품 조회
            products = productService.getProductsByPage(offset, pageSize);
            totalCount = productService.getTotalProductCount();
        }
        
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedCategoryName", category != null ? category.name() : null);
        model.addAttribute("categoryDisplayName", category != null ? category.getKoreanName() : "전체");
        
        return "product/index";
    }

    // 상품 상세 페이지
    @GetMapping("/{postId}")
    public String productDetail(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser customerUser) {
        ProductDetailDto product = productService.getProductDetailByPostId(postId);
        int chatRooms = chatService.countChatRoom(postId);
        model.addAttribute("product", product);
        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("userId", customerUser.getUserId());
        System.out.println("채팅방 수 : " + chatRooms);
        return "product/detail";
    }

    // 상품 등록 폼
    @GetMapping("/new")
    public String createProductForm(Model model) {
        model.addAttribute("productDto", new CreateProductDto());
        return "product/form";
    }

    // 상품 등록 처리
    @PostMapping
    public String createProduct(@ModelAttribute CreateProductDto createProductDto, @AuthenticationPrincipal CustomerUser loginUser) {
        productService.addProduct(createProductDto, loginUser);
        return "redirect:/product";
    }

    // 상품 수정 폼
    @GetMapping("/{postId}/edit")
    public String editProductForm(@PathVariable int postId, Model model) {
        UpdateProductDto productDto = productService.getProductByPostId(postId);
        System.out.println("jenkins"); // 디버깅 로그 추가
        model.addAttribute("productDto", productDto);
        return "product/editForm";
    }

    // 상품 수정 처리
    @PostMapping("/{postId}/edit")
    public String updateProduct(@PathVariable int postId, @ModelAttribute UpdateProductDto productDto, 
                              @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                              @RequestParam(value = "deletedImageIds", required = false) String deletedImageIds) {
        productService.updateProduct(postId, productDto, imageFiles, deletedImageIds);
        return "redirect:/product";
    }

    // 상품 삭제
    @PostMapping("/{postId}/delete")
    public String deleteProduct(@PathVariable int postId) {
        productService.deleteProduct(postId);
        return "redirect:/product";
    }
}
