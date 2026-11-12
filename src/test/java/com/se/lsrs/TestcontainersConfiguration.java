package com.se.lsrs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mysql.MySQLContainer;

/** 테스트용 MySQL 컨테이너 (설계서 5장: Testcontainers(MySQL)). Docker가 없으면 이를 쓰는 테스트는 건너뛴다. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	MySQLContainer mysqlContainer() {
		return new MySQLContainer("mysql:26.10.0");
	}

}
