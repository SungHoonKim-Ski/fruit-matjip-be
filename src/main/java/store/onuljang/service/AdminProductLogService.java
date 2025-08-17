package store.onuljang.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.onuljang.config.S3Config;
import store.onuljang.controller.response.PresignedUrlResponse;
import store.onuljang.repository.AdminProductLogRepository;
import store.onuljang.repository.entity.log.AdminProductLog;

import java.net.URL;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Slf4j
public class AdminProductLogService {
    AdminProductLogRepository adminProductLogRepository;

    @Transactional
    public long save(AdminProductLog adminProductLog) {
        return adminProductLogRepository.save(adminProductLog).getId();
    }
}