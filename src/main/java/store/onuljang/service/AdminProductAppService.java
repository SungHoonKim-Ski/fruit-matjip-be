package store.onuljang.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.component.SessionUtil;
import store.onuljang.controller.request.AdminCreateProductRequest;
import store.onuljang.repository.entity.Admin;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class AdminProductAppService {
    ProductsService productsService;
    AdminUploadService adminUploadService;
    AdminService adminService;

    @Transactional
    public Long saveAndMoveTempImage(AdminCreateProductRequest request) {
        String tempImageUrl = request.imageUrl();
        if (!tempImageUrl.startsWith("images/temp/")) {
            throw new IllegalArgumentException("잘못된 temp 경로: " + tempImageUrl);
        }

        String imageUrl = moveImage(tempImageUrl);

        Admin admin = adminService.findById(SessionUtil.getAdminId());

        return productsService.save(AdminCreateProductRequest.toEntity(request, imageUrl, admin));
    }

    private String moveImage(String tempUrl) {
        String destKey = "images/" + tempUrl.substring("images/temp/".length());

        adminUploadService.move(tempUrl, destKey);

        return destKey;
    }
}
