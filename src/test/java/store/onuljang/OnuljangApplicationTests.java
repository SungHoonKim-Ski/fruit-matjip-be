package store.onuljang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import store.onuljang.config.TestS3Config;
import store.onuljang.config.TestSqsConfig;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Import({TestS3Config.class, TestSqsConfig.class})
class OnuljangApplicationTests {

    @Test
    void contextLoads() {
    }

}
