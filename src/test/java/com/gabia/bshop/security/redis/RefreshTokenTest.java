package com.gabia.bshop.security.redis;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

class RefreshTokenTest {

	@Test
	void 유효기간이_지난_경우를_판별한다() {
		// given
		RefreshToken token = new RefreshToken("refreshToken", 1L,
			LocalDateTime.now().minusDays(1));

		// when, then
		assertThat(token.isExpired()).isTrue();
	}
}
