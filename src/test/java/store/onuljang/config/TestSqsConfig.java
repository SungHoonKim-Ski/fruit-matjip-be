package store.onuljang.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import software.amazon.awssdk.services.sqs.SqsClient;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSqsConfig {

    @Bean
    @Primary
    public SqsClient sqsClient() {
        return mock(SqsClient.class);
    }
}
