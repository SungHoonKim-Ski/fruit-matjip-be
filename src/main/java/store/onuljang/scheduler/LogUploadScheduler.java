package store.onuljang.scheduler;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import store.onuljang.config.S3Config;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import store.onuljang.util.TimeUtil;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogUploadScheduler {
    static String LOG_HOME = "/var/log/onuljang";
    static DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    S3Client s3;
    S3Config s3Config;
    Environment env;

    /**
     * 매일 00:12 (KST)에 어제 날짜 로그 파일(app-all/app-warn)을
     * s3://{bucket}/logs/{dev|prod}/... 경로로 업로드 후 로컬 삭제
     */
    @Scheduled(cron = "0 12 0 * * *", zone = "Asia/Seoul")
    public void uploadYesterdayLogs() {
        LocalDate y = LocalDate.now(TimeUtil.KST).minusDays(1);
        String date = DATE.format(y);
        String stage = resolveStage();

        // app-all 로그
        uploadIfExists(
            String.format("%s/app-all.%s.log.gz", LOG_HOME, date),
            String.format("logs/%s/all/%d/%02d/%02d/app-all-%s.log.gz",
                stage, y.getYear(), y.getMonthValue(), y.getDayOfMonth(), date)
        );

        // app-warn 로그
        uploadIfExists(
            String.format("%s/app-warn.%s.log.gz", LOG_HOME, date),
            String.format("logs/%s/warn/%d/%02d/%02d/app-warn-%s.log.gz",
                stage, y.getYear(), y.getMonthValue(), y.getDayOfMonth(), date)
        );
    }

    private String resolveStage() {
        for (String p : env.getActiveProfiles()) {
            if ("dev".equalsIgnoreCase(p)) return "dev";
            if ("prod".equalsIgnoreCase(p) || "production".equalsIgnoreCase(p)) return "prod";
        }
        return "dev";
    }

    private void uploadIfExists(String localPath, String s3Key) {
        File f = new File(localPath);
        if (!f.exists()) {
            log.info("[LogUpload] no file: {}", localPath);
            return;
        }
        try {
            PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(s3Config.getLogBucket())
                .key(s3Key)
                .build();

            s3.putObject(putReq, RequestBody.fromFile(f.toPath()));

            log.info("[LogUpload] uploaded: {} -> s3://{}/{}", localPath, s3Config.getLogBucket(), s3Key);

            if (f.delete()) {
                log.info("[LogUpload] local deleted: {}", localPath);
            } else {
                log.warn("[LogUpload] local delete failed: {}", localPath);
            }
        } catch (Exception e) {
            log.error("[LogUpload] failed: {} -> s3://{}/{} : {}", localPath, s3Config.getLogBucket(), s3Key, e.getMessage(), e);
        }
    }
}