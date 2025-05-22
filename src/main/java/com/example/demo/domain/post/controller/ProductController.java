package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.dto.CreateProductDto;
import com.example.demo.domain.post.dto.ProductDetailDto;
import com.example.demo.domain.post.dto.ProductListDto;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.domain.post.service.ProductService;
import com.example.demo.domain.user.model.CustomerUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 상품 list 가져오기
    @GetMapping
    public String productList(@RequestParam(defaultValue = "1") int page, Model model) {
        int pageSize = 4;
        int offset = (page - 1) * pageSize;

        List<ProductListDto> products = productService.getProductsByPage(offset, pageSize);
        int totalCount = productService.getTotalProductCount();
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;

        model.addAttribute("products", products);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasNext", hasNext);
        model.addAttribute("hasPrev", hasPrev);
        return "product/index";
    }

    // 상품 상세 페이지
    @GetMapping("/{postId}")
    public String productDetail(@PathVariable int postId, Model model) {
        ProductDetailDto product = productService.getProductDetailByPostId(postId);
        model.addAttribute("product", product);
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
        Product product = productService.getProductByPostId(postId);
        model.addAttribute("product", product);
        return "product/editForm";
    }

    // 상품 수정 처리
    @PostMapping("/{postId}/edit")
    public String updateProduct(@PathVariable int postId, @ModelAttribute CreateProductDto productDto) {
        productService.updateProduct(postId, productDto);
        return "redirect:/product";
    }

    // 상품 삭제
    @PostMapping("/{postId}/delete")
    public String deleteProduct(@PathVariable int postId) {
        productService.deleteProduct(postId);
        return "redirect:/product";
    }
}
