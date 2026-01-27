package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.controller.request.*;
import store.onuljang.controller.response.ProductCategoryResponse;
import store.onuljang.event.admin_product.AdminProductLogEvent;
import store.onuljang.repository.entity.ProductCategory;
import store.onuljang.repository.entity.ProductOrder;
import store.onuljang.repository.entity.base.BaseEntity;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.service.*;
import store.onuljang.util.SessionUtil;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminProductAppService {
    ProductsService productsService;
    ProductCategoryService productCategoryService;
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

        if (request.name() != null)
            product.setName(request.name());
        if (request.price() != null)
            product.setPrice(request.price());
        if (request.stock() != null)
            product.setStock(request.stock());
        if (request.sellDate() != null && !request.sellDate().isEmpty()) {
            product.setSellDate(LocalDate.parse(request.sellDate()));
        }
        if (request.description() != null)
            product.setDescription(request.description());
        if (request.updateSellTime() != null && request.updateSellTime())
            product.setSellTime(request.sellTime());

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
    @Deprecated
    public void toggleSelfPick(long productId) {
        productsService.findByIdWithLock(productId).toggleSelfPick();

        saveProductLog(productId, -3, AdminProductAction.UPDATE);
    }

    @Transactional
    public void toggleDeliveryAvailable(long productId) {
        productsService.findByIdWithLock(productId).toggleDeliveryAvailable();

        saveProductLog(productId, -4, AdminProductAction.UPDATE);
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
    public int updateOrder(AdminProductUpdateOrderRequest request) {
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
    public void saveCategory(AdminCreateCategoryRequest request) {
        if (productCategoryService.existsByName(request.name())) {
            throw new IllegalArgumentException("이미 존재하는 카테고리");
        }

        productCategoryService
                .save(ProductCategory.builder().name(request.name()).imageUrl(request.imageUrl()).build());
    }

    @Transactional
    public void updateCategory(Long categoryId, AdminUpdateCategoryRequest request) {
        ProductCategory category = productCategoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        if (request.name() != null) {
            category.setName(request.name());
        }
        if (request.imageUrl() != null) {
            category.setImageUrl(request.imageUrl());
        }
    }

    @Transactional
    public void deleteCategory(String categoryName) {
        productCategoryService.delete(categoryName);
    }

    @Transactional
    public void updateCategoryProducts(Long categoryId, AdminCategoryProductsRequest request) {
        ProductCategory category = productCategoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        category.getProducts().clear();
        category.getProducts().addAll(productsService.findAllByIdIn(request.productIds()));
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
    public ProductCategoryResponse getProductCategories() {
        List<ProductCategory> categories = productCategoryService.findAll();

        return ProductCategoryResponse.of(categories);
    }

    @Transactional
    public void addCategoryToProduct(Long productId, Long categoryId) {
        Product product = productsService.findByIdWithLock(productId);
        ProductCategory category = productCategoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        // 카테고리(주인) 쪽 컬렉션에 추가해야 DB에 반영됨
        category.getProducts().add(product);
    }

    @Transactional
    public void removeCategoryFromProduct(Long productId, Long categoryId) {
        Product product = productsService.findByIdWithLock(productId);
        ProductCategory category = productCategoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리"));

        category.getProducts().remove(product);
    }


    @Transactional
    public void updateCategorySortOrders(AdminUpdateCategoryListRequest request) {
        List<AdminUpdateCategoryListRequest.CategoryItemRequest> list = request.categories();
        for (int i = 0; i < list.size(); i++) {
            AdminUpdateCategoryListRequest.CategoryItemRequest item = list.get(i);
            if (item.id() != null) {
                ProductCategory category = productCategoryService.findById(item.id())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리 ID: " + item.id()));

                category.setSortOrder(i);
            }
        }
    }

    private void saveProductLog(long productId, Integer quantity, AdminProductAction action) {
        eventPublisher.publishEvent(AdminProductLogEvent.builder().adminId(SessionUtil.getAdminId())
                .productId(productId)
                .quantity(quantity)
                .action(action)
                .build());
    }
}
