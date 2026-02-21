package store.onuljang.courier.appservice;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierProductCreateRequest;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductUpdateRequest;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.shop.admin.dto.PresignedUrlRequest;
import store.onuljang.shop.admin.dto.PresignedUrlResponse;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.admin.service.AdminService;
import store.onuljang.shop.admin.service.AdminUploadService;
import store.onuljang.shop.admin.util.SessionUtil;
import store.onuljang.shop.product.entity.ProductCategory;
import store.onuljang.shop.product.service.ProductCategoryService;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierAdminProductAppService {

    CourierProductService courierProductService;
    ProductCategoryService productCategoryService;
    AdminUploadService adminUploadService;
    AdminService adminService;

    public CourierProductListResponse getProducts() {
        List<CourierProduct> products = courierProductService.findAllNotDeleted();
        return CourierProductListResponse.from(products);
    }

    public CourierProductResponse getProduct(Long id) {
        CourierProduct product = courierProductService.findByIdWithDetailImages(id);
        return CourierProductResponse.from(product);
    }

    @Transactional
    public CourierProductResponse createProduct(CourierProductCreateRequest request) {
        Admin admin = adminService.findById(SessionUtil.getAdminId());

        CourierProduct product = CourierProduct.builder()
                .name(request.name())
                .productUrl(request.productUrl())
                .price(request.price())
                .stock(request.stock())
                .weightGram(request.weightGram())
                .description(request.description())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .registeredAdmin(admin)
                .build();

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            Set<ProductCategory> categories = resolveCategories(request.categoryIds());
            product.updateCategories(categories);
        }

        courierProductService.save(product);

        if (request.detailImageUrls() != null && !request.detailImageUrls().isEmpty()) {
            product.replaceDetailImages(request.detailImageUrls());
        }

        return CourierProductResponse.from(product);
    }

    @Transactional
    public CourierProductResponse updateProduct(Long id, CourierProductUpdateRequest request) {
        CourierProduct product = courierProductService.findByIdWithDetailImages(id);

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.productUrl() != null) {
            product.setProductUrl(request.productUrl());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
        }
        if (request.stock() != null) {
            product.setStock(request.stock());
        }
        if (request.weightGram() != null) {
            product.setWeightGram(request.weightGram());
        }
        if (request.description() != null) {
            product.setDescription(request.description());
        }
        if (request.sortOrder() != null) {
            product.setSortOrder(request.sortOrder());
        }
        if (request.visible() != null) {
            if (request.visible()) {
                if (!product.getVisible()) {
                    product.toggleVisible();
                }
            } else {
                if (product.getVisible()) {
                    product.toggleVisible();
                }
            }
        }
        if (request.categoryIds() != null) {
            Set<ProductCategory> categories = resolveCategories(request.categoryIds());
            product.updateCategories(categories);
        }
        if (request.detailImageUrls() != null) {
            product.replaceDetailImages(request.detailImageUrls());
        }

        return CourierProductResponse.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        courierProductService.findById(id).softDelete();
    }

    @Transactional
    public void toggleVisible(Long id) {
        courierProductService.findByIdWithLock(id).toggleVisible();
    }

    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        return adminUploadService.issueImageUrl(request.fileName(), request.contentType());
    }

    private Set<ProductCategory> resolveCategories(List<Long> categoryIds) {
        Set<ProductCategory> categories = new HashSet<>();
        for (Long categoryId : categoryIds) {
            ProductCategory category = productCategoryService
                    .findById(categoryId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryId));
            categories.add(category);
        }
        return categories;
    }
}
