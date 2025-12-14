package com.jartiste.smartshop.application.service;

import com.jartiste.smartshop.presentation.dto.request.LoginRequest;
import com.jartiste.smartshop.presentation.dto.response.AuthResponse;
import jakarta.servlet.http.HttpSession;

public interface IAuthService {
    AuthResponse login(LoginRequest request, HttpSession session);
    void logout(HttpSession session);
}
