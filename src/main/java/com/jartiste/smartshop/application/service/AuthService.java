package com.jartiste.smartshop.application.service;


import com.jartiste.smartshop.domain.entity.Client;
import com.jartiste.smartshop.domain.entity.User;
import com.jartiste.smartshop.domain.enums.CustomerTier;
import com.jartiste.smartshop.domain.exception.ResourceNotFound;
import com.jartiste.smartshop.domain.exception.UsernameOrPasswordIncorrect;
import com.jartiste.smartshop.domain.repository.UserRepository;
import com.jartiste.smartshop.infrastructure.util.PasswordUtil;
import com.jartiste.smartshop.presentation.dto.request.LoginRequest;
import com.jartiste.smartshop.presentation.dto.response.AuthResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordUtil passwordUtil;

    public AuthResponse login(LoginRequest request, HttpSession session) {
        User user = this.userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFound("User not Found"));

        if(!passwordUtil.checkPassword(request.password(), user.getPassword())) {
            throw new UsernameOrPasswordIncorrect("username or password Incorrect");
        }

        session.setAttribute("USER_ID", user.getId());
        session.setAttribute("USER_ROLE", user.getRole());

        CustomerTier tier = null;

        if(user instanceof Client client) {
            tier = client.getTier();
        }

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole().name())
                .tier(tier)
                .message("LoggedIn SuccessFully")
                .build();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
