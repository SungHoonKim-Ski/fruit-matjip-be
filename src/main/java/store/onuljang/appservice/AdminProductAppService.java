package store.onuljang.appservice;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.repository.entity.enums.AdminProductAction;
import store.onuljang.repository.entity.log.AdminProductLog;
import store.onuljang.service.AdminProductLogService;
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
    AdminProductLogService adminProductLogService;

    @Transactional
    public Long save(AdminCreateProductRequest request) {
        Admin admin = adminService.findById(SessionUtil.getAdminId());

        long productId = productsService.save(AdminCreateProductRequest.toEntity(request, admin));

        saveProductLog(productId, AdminProductAction.CREATE);

        return productId;
    }

    @Transactional(readOnly = true)
    public AdminProductListItems getAll() {
        List<Product> entities = productsService.findAll();

        return AdminProductListItems.from(entities);
    }

    @Transactional(readOnly = true)
    public AdminProductDetailResponse getDetail(long productId) {
        Product product = productsService.findByIdWithDetailImages(productId);

        return AdminProductDetailResponse.from(product);
    }

    @Transactional
    public void updateDetail(long productId, AdminUpdateProductDetailsRequest request) {
        Product product = productsService.findByIdWithDetailImages(productId);

        saveProductLog(productId, AdminProductAction.UPDATE);

        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        if (request.stockChange() != null) product.addStock(product.getStock() + request.stockChange());
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
        saveProductLog(productId, AdminProductAction.DELETE);

        productsService.findById(productId).delete();
    }

    @Transactional
    public void toggleVisible(long productId) {
        saveProductLog(productId, AdminProductAction.UPDATE);

        productsService.findByIdWithLock(productId).toggleVisible();
    }

    @Transactional
    public void setSoldOut(long productId) {
        saveProductLog(productId, AdminProductAction.UPDATE);

        productsService.findByIdWithLock(productId).soldOut();
    }

    @Transactional
    public void saveProductLog(long productId, AdminProductAction action) {
        adminProductLogService.save(
            AdminProductLog.builder()
                .adminId(SessionUtil.getAdminId())
                .productId(productId)
                .action(action)
                .build()
        );
    }
}
