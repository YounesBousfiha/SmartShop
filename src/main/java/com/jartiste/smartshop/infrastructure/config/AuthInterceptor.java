package com.jartiste.smartshop.infrastructure.config;

import com.jartiste.smartshop.domain.enums.UserRole;
import com.jartiste.smartshop.domain.exception.ForbiddenException;
import com.jartiste.smartshop.domain.exception.UnAuthorizedException;
import com.jartiste.smartshop.presentation.annotation.RequireRole;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if(!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if(null == requireRole)  {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        if(null == requireRole) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if(null == session || null == session.getAttribute("USER_ROLE")) {
            throw new UnAuthorizedException("You are not authenticated");
        }

        String roleName = (String) session.getAttribute("USER_ROLE");
        UserRole userRole = UserRole.valueOf(roleName);

        boolean isAllowed = Arrays.asList(requireRole.value()).contains(userRole);

        if(!isAllowed) {
            throw new ForbiddenException("Access Forbidden. Insufficient priviléges");
        }

        return true;
    }
}
