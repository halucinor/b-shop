package com.gabia.bshop.controller;

import static com.gabia.bshop.exception.ErrorCode.*;
import static com.gabia.bshop.security.provider.RefreshTokenCookieProvider.*;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gabia.bshop.dto.response.AccessTokenResponse;
import com.gabia.bshop.dto.response.AdminLoginResponse;
import com.gabia.bshop.dto.response.IssuedTokensResponse;
import com.gabia.bshop.dto.response.LoginResponse;
import com.gabia.bshop.dto.response.LoginResult;
import com.gabia.bshop.exception.UnAuthorizedRefreshTokenException;
import com.gabia.bshop.security.provider.RefreshTokenCookieProvider;
import com.gabia.bshop.service.AuthService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class AuthController {

	private final AuthService authService;

	private final RefreshTokenCookieProvider refreshTokenCookieProvider;

	@GetMapping("/login")
	public ResponseEntity<LoginResponse> login(@RequestParam("auth_code") final String authCode) {
		final LoginResult loginResult = authService.login(authCode);
		final String refreshToken = loginResult.refreshToken();
		final ResponseCookie cookie = refreshTokenCookieProvider.createCookie(refreshToken);
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, cookie.toString())
			.body(new LoginResponse(loginResult.accessToken(), loginResult.memberResponse()));
	}

	@GetMapping("/login/admin")
	public AdminLoginResponse loginAdmin(@RequestParam("auth_code") final String authCode) {
		return authService.loginAdmin(authCode);
	}

	@GetMapping("/logout")
	public ResponseEntity<Void> logout(
		@CookieValue(value = REFRESH_TOKEN, required = false) final String refreshToken) {
		validateRefreshTokenExists(refreshToken);
		return ResponseEntity.noContent()
			.header(HttpHeaders.SET_COOKIE,
				refreshTokenCookieProvider.createLogoutCookie().toString())
			.build();
	}

	@PostMapping("/accessToken")
	public ResponseEntity<AccessTokenResponse> issueAccessToken(
		@CookieValue(value = REFRESH_TOKEN, required = false) final String refreshToken) {
		validateRefreshTokenExists(refreshToken);
		final IssuedTokensResponse issuedTokensResponse = authService.issueAccessToken(
			refreshToken);
		final ResponseCookie responseCookie = refreshTokenCookieProvider.createCookie(
			issuedTokensResponse.refreshToken());
		return ResponseEntity.ok()
			.header(HttpHeaders.SET_COOKIE, responseCookie.toString())
			.body(new AccessTokenResponse(issuedTokensResponse.accessToken()));
	}

	private void validateRefreshTokenExists(final String refreshToken) {
		if (refreshToken == null) {
			throw new UnAuthorizedRefreshTokenException(REFRESH_TOKEN_NOT_EXIST_EXCEPTION);
		}
	}
}
