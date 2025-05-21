package com.example.demo.domain.post.controller;

import com.example.demo.domain.post.DTO.CreateProductDto;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.post.model.Post;
import com.example.demo.domain.post.service.ProductService;
import com.example.demo.domain.user.model.CustomerUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/product")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // 상품 목록
    @GetMapping("/list")
    public String productList(Model model) {
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "product/index";
    }

    // 상품 등록 폼
    @GetMapping("/new")
    public String createProductForm(Model model) {
        model.addAttribute("productDto", new CreateProductDto());
        return "product/form";
    }

    // 상품 등록 처리
    @PostMapping
    public String createProduct(@ModelAttribute CreateProductDto productDto, @AuthenticationPrincipal CustomerUser loginUser) {
        productService.addProduct(productDto, loginUser);
        return "redirect:/product/list";
    }

    // 상품 수정 폼
    @GetMapping("/edit/{postId}")
    public String editProductForm(@PathVariable int postId, Model model) {
        Product product = productService.getProductByPostId(postId);
        model.addAttribute("product", product);
        return "product/editForm";
    }

    // 상품 수정 처리
    @PostMapping("/edit/{postId}")
    public String updateProduct(@PathVariable int postId, @ModelAttribute CreateProductDto productDto) {
        productService.updateProduct(postId, productDto);
        return "redirect:/product/list";
    }

    // 상품 삭제
    @PostMapping("/delete/{postId}")
    public String deleteProduct(@PathVariable int postId) {
        productService.deleteProduct(postId);
        return "redirect:/product/list";
    }
}
