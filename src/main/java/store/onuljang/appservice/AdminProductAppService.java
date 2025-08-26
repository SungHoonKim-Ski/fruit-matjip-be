package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.log.admin_product.AdminProductLogEvent;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.service.AdminService;
import store.onuljang.service.AdminUploadService;
import store.onuljang.service.ProductsService;
import store.onuljang.util.SessionUtil;
import store.onuljang.controller.request.AdminCreateProductRequest;
import store.onuljang.controller.request.AdminUpdateProductDetailsRequest;
import store.onuljang.controller.response.AdminProductDetailResponse;
import store.onuljang.controller.response.AdminProductListItems;
import store.onuljang.repository.entity.Admin;
import store.onuljang.repository.entity.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminProductAppService {
    ProductsService productsService;
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

    @Transactional
    public void updateDetail(long productId, AdminUpdateProductDetailsRequest request) {
        Product product = productsService.findByIdWithDetailImagesWithLock(productId);

        saveProductLog(productId, request.stock(), AdminProductAction.UPDATE);

        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        if (request.stock() != null) product.setStock(request.stock());
        if (request.sellDate() != null) {product.setSellDate(LocalDate.parse(request.sellDate()));}
        if (request.description() != null) product.setDescription(request.description());
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
    }

    @Transactional
    public void delete(long productId) {
        saveProductLog(productId, -1, AdminProductAction.DELETE);

        productsService.findById(productId).delete();
    }

    @Transactional
    public void toggleVisible(long productId) {
        saveProductLog(productId, -1, AdminProductAction.UPDATE);

        productsService.findByIdWithLock(productId).toggleVisible();
    }

    @Transactional
    public void setSoldOut(long productId) {
        saveProductLog(productId, 0, AdminProductAction.UPDATE);

        productsService.findByIdWithLock(productId).soldOut();
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
