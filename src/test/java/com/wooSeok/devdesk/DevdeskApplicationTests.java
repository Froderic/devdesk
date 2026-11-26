package com.wooSeok.devdesk;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = "app.seed.enabled=false")
@Import(TestcontainersConfiguration.class)
class DevdeskApplicationTests {

	@Test
	void contextLoads() {
	}

}
