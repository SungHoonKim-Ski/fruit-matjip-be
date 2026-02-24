package store.onuljang.courier.appservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.courier.dto.CourierProductCreateRequest;
import store.onuljang.courier.dto.CourierProductListResponse;
import store.onuljang.courier.dto.CourierProductResponse;
import store.onuljang.courier.dto.CourierProductUpdateRequest;
import store.onuljang.courier.dto.OptionGroupRequest;
import store.onuljang.courier.dto.OptionRequest;
import store.onuljang.courier.entity.CourierProduct;
import store.onuljang.courier.entity.CourierProductCategoryMapping;
import store.onuljang.courier.entity.CourierProductOption;
import store.onuljang.courier.entity.CourierProductOptionGroup;
import store.onuljang.courier.repository.CourierProductCategoryMappingRepository;
import store.onuljang.courier.repository.CourierProductRepository;
import store.onuljang.courier.service.CourierProductService;
import store.onuljang.shop.admin.dto.PresignedUrlRequest;
import store.onuljang.courier.dto.CourierCategoryResponse;
import store.onuljang.courier.entity.CourierProductCategory;
import store.onuljang.courier.service.CourierProductCategoryService;
import store.onuljang.shop.admin.dto.PresignedUrlResponse;
import store.onuljang.shop.admin.entity.Admin;
import store.onuljang.shop.admin.service.AdminService;
import store.onuljang.shop.admin.service.AdminUploadService;
import store.onuljang.shop.admin.util.SessionUtil;

@Service
@Transactional(readOnly = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CourierAdminProductAppService {

    CourierProductService courierProductService;
    CourierProductCategoryService courierProductCategoryService;
    AdminUploadService adminUploadService;
    AdminService adminService;
    CourierProductCategoryMappingRepository courierProductCategoryMappingRepository;
    CourierProductRepository courierProductRepository;

    public CourierProductListResponse getProducts() {
        List<CourierProduct> products = courierProductService.findAll();
        return CourierProductListResponse.from(products);
    }

    public CourierProductResponse getProduct(Long id) {
        CourierProduct product = courierProductService.findById(id);
        return CourierProductResponse.from(product);
    }

    @Transactional
    public CourierProductResponse createProduct(CourierProductCreateRequest request) {
        Admin admin = adminService.findById(SessionUtil.getAdminId());

        CourierProduct product = CourierProduct.builder()
                .name(request.name())
                .productUrl(request.productUrl())
                .price(request.price())
                .weightGram(request.weightGram())
                .description(request.description())
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : courierProductRepository.findMinSortOrder() - 1)
                .registeredAdmin(admin)
                .build();

        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            Set<CourierProductCategory> categories = resolveCategories(request.categoryIds());
            product.updateCategories(categories);
        }

        product.setShippingFee(request.shippingFee());
        if (request.combinedShippingQuantity() != null) {
            product.setCombinedShippingQuantity(request.combinedShippingQuantity());
        }

        courierProductService.save(product);

        if (request.optionGroups() != null && !request.optionGroups().isEmpty()) {
            List<CourierProductOptionGroup> groups = buildOptionGroups(product, request.optionGroups());
            product.replaceOptionGroups(groups);
        }

        return CourierProductResponse.from(product);
    }

    @Transactional
    public CourierProductResponse updateProduct(Long id, CourierProductUpdateRequest request) {
        CourierProduct product = courierProductService.findById(id);

        if (request.name() != null) {
            product.setName(request.name());
        }
        if (request.productUrl() != null) {
            product.setProductUrl(request.productUrl());
        }
        if (request.price() != null) {
            product.setPrice(request.price());
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
        if (request.soldOut() != null) {
            if (request.soldOut()) {
                if (!Boolean.TRUE.equals(product.getSoldOut())) {
                    product.toggleSoldOut();
                }
            } else {
                if (Boolean.TRUE.equals(product.getSoldOut())) {
                    product.toggleSoldOut();
                }
            }
        }
        if (request.categoryIds() != null) {
            Set<CourierProductCategory> categories = resolveCategories(request.categoryIds());
            product.updateCategories(categories);
        }

        if (request.shippingFee() != null) {
            product.setShippingFee(request.shippingFee());
        }
        if (request.combinedShippingQuantity() != null) {
            product.setCombinedShippingQuantity(request.combinedShippingQuantity());
        }

        if (request.optionGroups() != null) {
            List<CourierProductOptionGroup> groups = buildOptionGroups(product, request.optionGroups());
            product.replaceOptionGroups(groups);
        }

        return CourierProductResponse.from(product);
    }

    @Transactional
    public int updateOrder(List<Long> productIds) {
        List<CourierProduct> products = courierProductService.findAllByIdIn(productIds);
        if (products.isEmpty() || products.size() != productIds.size()) {
            throw new IllegalArgumentException("선택한 상품이 존재하지 않습니다.");
        }

        Map<Long, CourierProduct> productMap =
                products.stream().collect(Collectors.toMap(CourierProduct::getId, p -> p));

        int idx = 0;
        for (Long id : productIds) {
            CourierProduct product = productMap.get(id);
            product.setSortOrder(idx++);
        }
        return idx;
    }

    @Transactional
    public int updateCategoryOrder(Long categoryId, List<Long> productIds) {
        List<CourierProductCategoryMapping> mappings =
                courierProductCategoryMappingRepository.findByCategoryIdOrderBySortOrder(categoryId);

        Map<Long, CourierProductCategoryMapping> mappingMap = mappings.stream()
                .collect(Collectors.toMap(m -> m.getCourierProduct().getId(), m -> m));

        int idx = 0;
        for (Long productId : productIds) {
            CourierProductCategoryMapping mapping = mappingMap.get(productId);
            if (mapping != null) {
                mapping.setSortOrder(idx++);
            }
        }
        return idx;
    }

    @Transactional
    public void deleteProduct(Long id) {
        courierProductService.findById(id).softDelete();
    }

    @Transactional
    public void toggleVisible(Long id) {
        courierProductService.findByIdWithLock(id).toggleVisible();
    }

    @Transactional
    public void toggleSoldOut(Long id) {
        courierProductService.findByIdWithLock(id).toggleSoldOut();
    }

    @Transactional
    public void toggleRecommend(Long productId) {
        CourierProduct product = courierProductService.findById(productId);
        product.toggleRecommended();
    }

    @Transactional
    public int updateRecommendOrder(List<Long> productIds) {
        List<CourierProduct> products = courierProductService.findAllByIdIn(productIds);
        Map<Long, CourierProduct> productMap = products.stream()
                .collect(Collectors.toMap(CourierProduct::getId, p -> p));

        int idx = 0;
        for (Long id : productIds) {
            CourierProduct product = productMap.get(id);
            if (product != null) {
                product.setRecommendOrder(idx++);
            }
        }
        return idx;
    }

    public CourierCategoryResponse getCategories() {
        List<CourierProductCategory> categories =
                courierProductCategoryService.findAllOrderBySortOrder();
        return CourierCategoryResponse.of(categories);
    }

    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        return adminUploadService.issueImageUrl(request.fileName(), request.contentType());
    }

    private List<CourierProductOptionGroup> buildOptionGroups(
            CourierProduct product, List<OptionGroupRequest> requests) {
        List<CourierProductOptionGroup> groups = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            OptionGroupRequest gr = requests.get(i);
            CourierProductOptionGroup group = CourierProductOptionGroup.builder()
                    .courierProduct(product)
                    .name(gr.name())
                    .required(gr.required() != null ? gr.required() : true)
                    .sortOrder(gr.sortOrder() != null ? gr.sortOrder() : i)
                    .build();

            for (int j = 0; j < gr.options().size(); j++) {
                OptionRequest or = gr.options().get(j);
                group.addOption(CourierProductOption.builder()
                        .optionGroup(group)
                        .name(or.name())
                        .additionalPrice(or.additionalPrice() != null ? or.additionalPrice() : BigDecimal.ZERO)
                        .sortOrder(or.sortOrder() != null ? or.sortOrder() : j)
                        .stock(or.stock())
                        .build());
            }
            groups.add(group);
        }
        return groups;
    }

    private Set<CourierProductCategory> resolveCategories(List<Long> categoryIds) {
        Set<CourierProductCategory> categories = new HashSet<>();
        for (Long categoryId : categoryIds) {
            CourierProductCategory category = courierProductCategoryService
                    .findById(categoryId)
                    .orElseThrow(
                            () -> new IllegalArgumentException("존재하지 않는 카테고리: " + categoryId));
            categories.add(category);
        }
        return categories;
    }
}
