package com.se.lsrs.common.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 현재 시각은 주입된 Clock으로만 얻는다 (설계서 2장 Application 계층, NFR 4.2).
 * 테스트에서는 고정 Clock을 주입해 시간을 조작한다.
 */
@Configuration
public class ClockConfig {

	@Bean
	public Clock clock() {
		return Clock.systemDefaultZone();
	}

}
