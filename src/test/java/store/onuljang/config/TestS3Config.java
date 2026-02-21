package store.onuljang.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import store.onuljang.shared.config.S3Config;

/**
 * 테스트 환경용 S3 Configuration
 * 실제 S3Config를 모킹하여 @Value 주입 없이 테스트 환경에서 동작하도록 설정
 */
@TestConfiguration
public class TestS3Config {

    @Bean
    @Primary
    public S3Config s3Config() {
        S3Config mock = mock(S3Config.class);
        when(mock.getBucket()).thenReturn("test-bucket");
        when(mock.getLogBucket()).thenReturn("test-log-bucket");
        return mock;
    }

    @Bean
    @Primary
    public S3Client s3Client() {
        return mock(S3Client.class);
    }

    @Bean
    @Primary
    public S3Presigner s3Presigner() {
        return mock(S3Presigner.class);
    }
}
