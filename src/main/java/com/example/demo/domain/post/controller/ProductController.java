package com.example.demo.domain.post.controller;

import com.example.demo.domain.chat.dto.ChatParticipantDto;
import com.example.demo.domain.chat.service.ChatService;
import com.example.demo.domain.post.dto.*;
import com.example.demo.domain.post.model.Product;
import com.example.demo.domain.post.service.MyProductService;
import com.example.demo.domain.post.service.FavoriteService;
import com.example.demo.domain.post.service.ImageUploadService;
import com.example.demo.domain.post.service.ProductService;
import com.example.demo.domain.stringcode.ProductCategory;
import com.example.demo.domain.user.model.CustomerUser;
import com.example.demo.domain.user.model.Users;
import com.example.demo.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final FavoriteService favoriteService;
    private final ChatService chatService;
    private final MyProductService myProductService;
    private final UserService userService;

    // 상품 list 가져오기 (카테고리 필터링 및 검색 포함)
    @GetMapping("/product")
    public String productList(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) ProductCategory category,
                            @RequestParam(required = false) String search,
                            Model model) {
        int pageSize = 20;
        int offset = (page - 1) * pageSize;

        List<ProductListDto> products;
        int totalCount;

        // 검색어가 있는 경우
        if (search != null && !search.trim().isEmpty()) {
            products = productService.getProductsBySearch(offset, pageSize, search.trim());
            totalCount = productService.getTotalProductCountBySearch(search.trim());
            model.addAttribute("searchKeyword", search.trim());
        }
        // 카테고리 필터가 있는 경우
        else if (category != null) {
            products = productService.getProductsByPageAndCategory(offset, pageSize, category);
            totalCount = productService.getTotalProductCountByCategory(category);
        }
        // 전체 상품 조회
        else {
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

     //내가 등록한 상품 관리 페이지
    @GetMapping("/user/product/my")
    public String myProducts(@RequestParam(defaultValue = "1") int page,
                             @RequestParam(required = false) ProductCategory category,
                             @RequestParam(required = false) String search,
                             @RequestParam(required = false) String sort,
                             @RequestParam(required = false) String status,
                             @AuthenticationPrincipal CustomerUser loginUser,
                             Model model) {
        int pageSize = 12;
        int offset = (page - 1) * pageSize;

        List<MyProductDto> products;
        int totalCount;

        if ("purchased".equals(status)) {
            // 구매한 상품 조회
            products = myProductService.getPurchasedProducts(
                    loginUser.getUserId(), offset, pageSize, category, search, sort);
            totalCount = myProductService.getPurchasedProductsCount(
                    loginUser.getUserId(), category, search);
        } else {
            // 기존 로직 (내가 등록한 상품)
            products = myProductService.getMyProducts(
                    loginUser.getUserId(), offset, pageSize, category, search, sort, status);
            totalCount = myProductService.getMyProductsCount(
                    loginUser.getUserId(), category, search, status);
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
        model.addAttribute("selectedSort", sort);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchKeyword", search);

        return "product/myproducts";
    }

    // 상품 상세 페이지
    @GetMapping("/product/{postId}")
    public String productDetail(@PathVariable int postId, Model model, @AuthenticationPrincipal CustomerUser loginUser) {
        ProductDetailDto product = productService.getProductDetailByPostId(postId);
        int chatRooms = chatService.countChatRoom(postId);
        Users writer = userService.getUserById(product.getUserId());

        model.addAttribute("product", product);

        int favoriteCount = favoriteService.getFavoriteCount(postId);
        model.addAttribute("favoriteCount", favoriteCount);

        boolean hasFavorited = false;
        if(loginUser != null){
            hasFavorited = favoriteService.hasFavorited(postId, loginUser.getUserId());
        }
        model.addAttribute("hasFavorited", hasFavorited);

        model.addAttribute("chatRooms", chatRooms);
        model.addAttribute("rating", writer.getRating());
        return "product/detail";
    }

    // 상품 등록 폼
    @GetMapping("/user/product/new")
    public String createProductForm(Model model) {
        model.addAttribute("productDto", new CreateProductDto());
        return "product/form";
    }

    // 상품 등록 처리
    @PostMapping("/user/product")
    public String createProduct(@ModelAttribute CreateProductDto createProductDto, @AuthenticationPrincipal CustomerUser loginUser) {
        productService.addProduct(createProductDto, loginUser);
        return "redirect:/product";
    }

    // 상품 수정 폼
    @GetMapping("/user/product/{postId}/edit")
    public String editProductForm(@PathVariable int postId, Model model) {
        UpdateProductDto productDto = productService.getProductByPostId(postId);
        model.addAttribute("productDto", productDto);
        return "product/editForm";
    }

    // 상품 수정 처리
    @PostMapping("/user/product/{postId}/edit")
    public String updateProduct(@PathVariable int postId, @ModelAttribute UpdateProductDto productDto,
                              @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                              @RequestParam(value = "deletedImageIds", required = false) String deletedImageIds) {
        productService.updateProduct(postId, productDto, imageFiles, deletedImageIds);
        return "redirect:/product";
    }

    // 상품 삭제
    @PostMapping("/user/product/{postId}/delete")
    public String deleteProduct(@PathVariable int postId) {
        productService.deleteProduct(postId);
        return "redirect:/product";
    }

    //상품 상태(판패여부) 업데이트
    @PostMapping("/user/product/{postId}/status")
    @ResponseBody
    public ResponseEntity<?> updateProductStatus(@PathVariable int postId,
                                                 @RequestBody Map<String, String> statusRequest) {

        // 상태 값 가져오기
        String statusStr = statusRequest.get("status");
        if (statusStr == null || statusStr.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "상태 값이 없습니다."));
        }

        // 상태 값 변환
        Product.TradeStatus status;
        try {
            status = Product.TradeStatus.valueOf(statusStr);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "유효하지 않은 상태 값입니다."));
        }

        // 상품 상태 업데이트
        boolean updated = productService.updateProductStatus(postId, status);

        if (updated) {
            return ResponseEntity.ok(Map.of("success", true, "message", "상품 상태가 변경되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "상품 상태 변경에 실패했습니다."));
        }
    }

    //나의 상품보기에서 바로 상품 삭제
    @PostMapping("/user/product/{postId}/my")
    @ResponseBody
    public ResponseEntity<?> deleteProductInMy(@PathVariable int postId) {

        try {
            productService.deleteProduct(postId);
            return ResponseEntity.ok(Map.of("success", true, "message", "상품이 삭제되었습니다."));
        } catch (Exception e) {
            log.error("상품 삭제 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of("error", "상품 삭제에 실패했습니다."));
        }
    }

    // 채팅 참여자 목록 조회
    @GetMapping("/user/product/{postId}/chat-participants")
    @ResponseBody
    public ResponseEntity<?> getChatParticipants(@PathVariable int postId,
                                                 @AuthenticationPrincipal CustomerUser loginUser) {
        try {
            List<ChatParticipantDto> participants = chatService.getChatParticipantsByPostId(postId);
            return ResponseEntity.ok(participants);
        } catch (Exception e) {
            log.error("채팅 참여자 조회 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of("error", "채팅 참여자 조회에 실패했습니다."));
        }
    }

    // AJAX용 API 엔드포인트 추가
    @GetMapping("/user/product/my/api")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> myProductsApi(@RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(required = false) ProductCategory category,
                                                           @RequestParam(required = false) String search,
                                                           @RequestParam(required = false) String sort,
                                                           @RequestParam(required = false) String status,
                                                           @AuthenticationPrincipal CustomerUser loginUser) {
        int pageSize = 12;
        int offset = (page - 1) * pageSize;

        List<MyProductDto> products;
        int totalCount;

        if ("purchased".equals(status)) {
            // 구매한 상품 조회
            products = myProductService.getPurchasedProducts(
                    loginUser.getUserId(), offset, pageSize, category, search, sort);
            totalCount = myProductService.getPurchasedProductsCount(
                    loginUser.getUserId(), category, search);
        } else {
            // 기존 로직 (내가 등록한 상품)
            products = myProductService.getMyProducts(
                    loginUser.getUserId(), offset, pageSize, category, search, sort, status);
            totalCount = myProductService.getMyProductsCount(
                    loginUser.getUserId(), category, search, status);
        }

        int totalPages = (int) Math.ceil((double) totalCount / pageSize);
        boolean hasNext = page < totalPages;
        boolean hasPrev = page > 1;

        // JSON 응답을 위한 Map 구성
        Map<String, Object> result = new HashMap<>();
        result.put("products", products);
        result.put("currentPage", page);
        result.put("totalPages", totalPages);
        result.put("hasNext", hasNext);
        result.put("hasPrev", hasPrev);
        result.put("selectedCategory", category);
        result.put("selectedCategoryName", category != null ? category.name() : null);
        result.put("selectedSort", sort);
        result.put("selectedStatus", status);
        result.put("searchKeyword", search);

        return ResponseEntity.ok(result);
    }

    // 판매완료 처리
    @PostMapping("/user/product/{postId}/complete")
    @ResponseBody
    public ResponseEntity<?> completeTransaction(@PathVariable int postId,
                                                 @RequestParam int buyerId,
                                                 @AuthenticationPrincipal CustomerUser loginUser) {
        try {
            boolean success = productService.completeTransactionWithBuyer(postId, buyerId, loginUser.getUserId());

            if (success) {
                return ResponseEntity.ok(Map.of("success", true, "message", "판매가 완료되었습니다."));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "판매완료 처리에 실패했습니다."));
            }
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("판매완료 처리 중 오류 발생", e);
            return ResponseEntity.badRequest().body(Map.of("error", "판매완료 처리 중 오류가 발생했습니다."));
        }
    }
}
