package store.onuljang;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import store.onuljang.config.TestS3Config;

@SpringBootTest
@org.springframework.test.context.ActiveProfiles("test")
@Import(TestS3Config.class)
class OnuljangApplicationTests {

    @Test
    void contextLoads() {
    }

}
