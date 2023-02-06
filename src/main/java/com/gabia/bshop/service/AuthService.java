package com.gabia.bshop.service;

import com.gabia.bshop.dto.response.AdminLoginResponse;
import com.gabia.bshop.dto.response.HiworksProfileResponse;
import com.gabia.bshop.dto.response.IssuedTokensResponse;
import com.gabia.bshop.dto.response.LoginResult;
import com.gabia.bshop.entity.Member;
import com.gabia.bshop.entity.enumtype.MemberRole;
import com.gabia.bshop.exception.UnAuthorizedException;
import com.gabia.bshop.mapper.HiworksProfileMapper;
import com.gabia.bshop.security.redis.RefreshToken;
import com.gabia.bshop.security.redis.RefreshTokenService;
import com.gabia.bshop.repository.MemberRepository;
import com.gabia.bshop.security.client.HiworksOauthClient;
import com.gabia.bshop.security.provider.JwtProvider;
import com.gabia.bshop.security.provider.RefreshTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final HiworksOauthClient hiworksOauthClient;
    private final HiworksOauthClient adminHiworksOauthClient;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenProvider refreshTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public AuthService(@Qualifier("normal") final HiworksOauthClient hiworksOauthClient,
            @Qualifier("admin") final HiworksOauthClient adminHiworksOauthClient,
            final MemberRepository memberRepository,
            final JwtProvider jwtProvider, final RefreshTokenProvider refreshTokenProvider,
            final RefreshTokenService refreshTokenService) {
        this.hiworksOauthClient = hiworksOauthClient;
        this.adminHiworksOauthClient = adminHiworksOauthClient;
        this.memberRepository = memberRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenProvider = refreshTokenProvider;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public LoginResult login(final String authCode) {
        final HiworksProfileResponse hiworksProfileResponse = getHiworksProfileResponse(authCode);
        final Member member = addOrUpdateMember(hiworksProfileResponse);
        final Long memberId = member.getId();
        final String applicationAccessToken = jwtProvider.createAccessToken(memberId,
                member.getRole());
        final RefreshToken refreshToken = refreshTokenProvider.createToken(memberId);
        refreshTokenService.save(refreshToken);
        return new LoginResult(refreshToken.refreshToken(), applicationAccessToken, member);
    }

    public AdminLoginResponse loginAdmin(final String authCode
    ) {
        final HiworksProfileResponse hiworksProfileResponse = getAdminHiworksProfileResponse(authCode);
        final Member member = memberRepository.findByHiworksId(hiworksProfileResponse.hiworksId())
                .orElseThrow(() -> new EntityNotFoundException("관리자로 등록되지 않으셨습니다."));
        if (!member.isAdmin()) {
            throw new UnAuthorizedException("관리자로 등록된 사용자가 아닙니다.");
        }
        final String applicationAccessToken = jwtProvider.createAccessToken(member.getId(), member.getRole());
        return new AdminLoginResponse(applicationAccessToken);
    }

    @Transactional
    public IssuedTokensResponse issueAccessToken(final String refreshTokenValue) {
        final RefreshToken refreshToken = refreshTokenService.findToken(refreshTokenValue);
        checkExpired(refreshTokenValue, refreshToken);
        final Long memberId = refreshToken.memberId();
        final MemberRole memberRole = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원을 찾을 수 없습니다."))
                .getRole();
        final String newAccessToken = jwtProvider.createAccessToken(memberId, memberRole);
        final RefreshToken newRefreshToken = refreshTokenProvider.createToken(memberId);
        refreshTokenService.save(newRefreshToken);
        refreshTokenService.delete(refreshTokenValue);
        return IssuedTokensResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.refreshToken())
                .build();
    }

    private HiworksProfileResponse getHiworksProfileResponse(final String code) {
        final String hiworksAccessToken = hiworksOauthClient.getAccessToken(code);
        return hiworksOauthClient.getProfile(hiworksAccessToken);
    }

    private HiworksProfileResponse getAdminHiworksProfileResponse(final String code) {
        final String hiworksAccessToken = adminHiworksOauthClient.getAccessToken(code);
        return hiworksOauthClient.getProfile(hiworksAccessToken);
    }

    private Member addOrUpdateMember(final HiworksProfileResponse hiworksProfileResponse) {
        final Member requestedMember = HiworksProfileMapper.INSTANCE.toEntity(hiworksProfileResponse);
        final Member member = memberRepository.findByHiworksId(hiworksProfileResponse.hiworksId())
                .orElseGet(() -> memberRepository.save(requestedMember));
        member.update(requestedMember);
        return member;
    }

    private void checkExpired(final String refreshToken, final RefreshToken tokenInfo) {
        if (tokenInfo.isExpired()) {
            refreshTokenService.delete(refreshToken);
            throw new UnAuthorizedException("리프레시 토큰이 만료됐습니다.");
        }
    }
}
