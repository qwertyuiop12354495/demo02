package com.example.activity.common.auth;

import com.example.activity.common.exception.BusinessException;
import com.example.activity.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        AuthUser authUser;
        try {
            authUser = jwtTokenProvider.parseToken(token);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        AuthContext.set(authUser);
        enforceRoleRequirements(handlerMethod);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        AuthContext.clear();
    }

    private void enforceRoleRequirements(HandlerMethod handlerMethod) {
        if (requiresLegacyAdmin(handlerMethod)) {
            RoleGuard.requireLegacyAdmin();
            return;
        }

        RequireRole requireRole = findAnnotation(handlerMethod, RequireRole.class);
        if (requireRole != null) {
            RoleGuard.requireRole(requireRole.value());
            return;
        }

        RequireAnyRole requireAnyRole = findAnnotation(handlerMethod, RequireAnyRole.class);
        if (requireAnyRole != null) {
            RoleGuard.requireAnyRole(requireAnyRole.value());
        }
    }

    private boolean requiresLegacyAdmin(HandlerMethod handlerMethod) {
        if (findAnnotation(handlerMethod, RequireAdmin.class) != null) {
            return true;
        }
        return handlerMethod.getBeanType().isAnnotationPresent(RequireAdmin.class);
    }

    private <A extends java.lang.annotation.Annotation> A findAnnotation(HandlerMethod handlerMethod, Class<A> type) {
        A methodAnnotation = handlerMethod.getMethodAnnotation(type);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return handlerMethod.getBeanType().getAnnotation(type);
    }
}
