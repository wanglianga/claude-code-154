package com.community.market.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.community.market.common.ApiResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenStore tokenStore;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 公开接口（居民端、登录、静态资源）不校验
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/api/public")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.startsWith("/h2-console")) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            CurrentUser.SessionUser user = tokenStore.resolve(auth.substring(7));
            if (user != null) {
                CurrentUser.set(user);
                return true;
            }
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.fail("未登录或登录已失效")));
        response.getWriter().flush();
        return false;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUser.clear();
    }
}
