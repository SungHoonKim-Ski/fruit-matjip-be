package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.AdminProductBulkUpdateSellDateRequest;
import store.onuljang.controller.request.AdminProductUpdateOrder;
import store.onuljang.controller.response.ProductKeywordResponse;
import store.onuljang.event.admin_product.AdminProductLogEvent;
import store.onuljang.repository.entity.ProductKeyword;
import store.onuljang.repository.entity.ProductOrder;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.service.*;
import store.onuljang.util.SessionUtil;
import store.onuljang.controller.request.AdminCreateProductRequest;
import store.onuljang.controller.request.AdminUpdateProductDetailsRequest;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminProductAppService {
    ProductsService productsService;
    ProductKeywordService productKeywordService;
    ProductOrderService productOrderService;
    AdminUploadService adminUploadService;
    AdminService adminService;
    ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long save(AdminCreateProductRequest request) {
        Admin admin = adminService.findById(SessionUtil.getAdminId());

        long productId = productsService.save(AdminCreateProductRequest.toEntity(request, admin));

        saveProductLog(productId, request.stock(), AdminProductAction.CREATE);

        return productId;
    }

    @Transactional
    public void updateDetail(long productId, AdminUpdateProductDetailsRequest request) {
        Product product = productsService.findByIdWithDetailImagesWithLock(productId);

        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.sellDate() != null) {product.setSellDate(LocalDate.parse(request.sellDate()));}
        if (request.description() != null) product.setDescription(request.description());
        if (request.updateSellTime() != null && request.updateSellTime()) product.setSellTime(request.sellTime());

        List<String> removeKey = new ArrayList<>();

        if (request.productUrl() != null) {
            removeKey.add(product.getProductUrl());
            product.setProductUrl(request.productUrl());
        }

        if (request.detailUrls() != null) {
            removeKey = product.replaceDetailImagesInOrder(request.detailUrls());
        }

        if (!removeKey.isEmpty()) {
            adminUploadService.softDeleteAllImages(removeKey);
        }

        saveProductLog(productId, request.stock(), AdminProductAction.UPDATE);
    }

    @Transactional
    public void delete(long productId) {
        productsService.findById(productId).delete();

        saveProductLog(productId, -1, AdminProductAction.DELETE);
    }

    @Transactional
    public void toggleVisible(long productId) {
        productsService.findByIdWithLock(productId).toggleVisible();

        saveProductLog(productId, -2, AdminProductAction.UPDATE);
    }

    @Transactional
    public void toggleSelfPick(long productId) {
        productsService.findByIdWithLock(productId).toggleSelfPick();

        saveProductLog(productId, -3, AdminProductAction.UPDATE);
    }

    @Transactional
    public int bulkUpdateSellDate(AdminProductBulkUpdateSellDateRequest request) {
        List<Product> products = productsService.findAllByIdIn(request.productIds());
        if (products.isEmpty() || products.size() != request.productIds().size()) {
            throw new IllegalArgumentException("선택한 제품이 존재하지 않습니다.");
        }

        int rows = productsService.bulkUpdateSellDateIdIn(
            products.stream().map(BaseEntity::getId).toList(), request.sellDate()
        );

        if (rows != products.size()) {
            throw new RuntimeException("제품 판매일 bulk update row 불일치");
        }
        return rows;
    }

    @Transactional
    public int updateOrder(AdminProductUpdateOrder request) {
        List<Product> products = productsService.findAllByIdIn(request.productIds());
        if (products.isEmpty() || products.size() != request.productIds().size()) {
            throw new IllegalArgumentException("선택한 제품이 존재하지 않습니다.");
        }

        Set<LocalDate> sellDates = products.stream()
                .map(Product::getSellDate)
                .collect(Collectors.toSet());

        if (sellDates.size() != 1) {
            throw new IllegalArgumentException("선택한 제품들의 판매일이 일치하지 않습니다.");
        }

        LocalDate sellDate = sellDates.iterator().next();
        Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, p -> p));

        productOrderService.deleteAllBySellDate(sellDate);
        List<ProductOrder> orders = new ArrayList<>(products.size());
        int idx = 1;
        for (Long id : request.productIds()) {
            Product product = productMap.get(id);

            ProductOrder productOrder = ProductOrder.builder()
                .product(product)
                .sellDate(sellDate)
                .orderIndex(idx++)
                .build();

            product.setProductOrder(productOrder);

            orders.add(productOrder);
        }

        return productOrderService.saveAll(orders);
    }

    @Transactional
    public void saveKeyword(String keyword) {
        if (productKeywordService.existKeyword(keyword)) {
            throw new IllegalArgumentException("이미 존재하는 키워드");
        }

        productKeywordService.save(
            ProductKeyword.builder()
                .name(keyword)
                .build()
        );
    }

    @Transactional
    public void updateKeywords(List<String> keywords) {
        productKeywordService.deleteAllWithFlush();

        productKeywordService.saveAll(
            keywords.stream().map(
            keyword -> ProductKeyword.builder()
                .name(keyword)
                .build()).toList()
        );
    }

    @Transactional
    public void deleteKeyword(String keywords) {
        productKeywordService.delete(keywords);
    }

    @Transactional(readOnly = true)
    public AdminProductListItems getAll() {
        List<Product> entities = productsService.findAllOrderBySellDateDesc();

        return AdminProductListItems.from(entities);
    }

    @Transactional(readOnly = true)
    public AdminProductDetailResponse getDetail(long productId) {
        Product product = productsService.findByIdWithDetailImages(productId);

        return AdminProductDetailResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductKeywordResponse getProductKeywords() {
        List<ProductKeyword> keywords = productKeywordService.findAll();

        return ProductKeywordResponse.of(keywords);
    }

    private void saveProductLog(long productId, Integer quantity, AdminProductAction action) {
        eventPublisher.publishEvent(
            AdminProductLogEvent.builder()
                .adminId(SessionUtil.getAdminId())
                .productId(productId)
                .quantity(quantity)
                .action(action)
                .build());
    }
}
