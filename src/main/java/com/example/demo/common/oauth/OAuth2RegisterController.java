package com.example.demo.common.oauth;

import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
@RequestMapping("/oauth2/signup")
@RequiredArgsConstructor
public class OAuth2RegisterController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/additional-info")
    public String additionalInfoForm(HttpSession session, Model model) {
        // 세션에서 OAuth2 사용자 정보 가져오기
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");

        if (attributes == null) {
            return "redirect:/login";
        }

        // 모델에 OAuth2 사용자 정보 추가
        model.addAttribute("email", attributes.getEmail());
        model.addAttribute("name", attributes.getName());

        return "oauth2/additional-info";
    }

    @PostMapping("/additional-info")
    public String processAdditionalInfo(@RequestParam String phone,
                                        @RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String address,
                                        @RequestParam String password,
                                        @RequestParam String confirmPassword,
                                        HttpSession session) {

        // 비밀번호 검증
        if (!password.equals(confirmPassword)) {
            return "redirect:/oauth2/signup/additional-info?error=passwordMismatch";
        }

        // 세션에서 OAuth2 사용자 정보 가져오기
        OAuthAttributes attributes = (OAuthAttributes) session.getAttribute("oauthAttributes");
        String registrationId = (String) session.getAttribute("registrationId");

        if (attributes == null || registrationId == null) {
            return "redirect:/login";
        }

        try {
            // 새 사용자 생성
            Users newUser = new Users();
            newUser.setEmail(attributes.getEmail());
            newUser.setName(attributes.getName());
            newUser.setPhone(phone);
            newUser.setNickname(nickname != null ? nickname : attributes.getName());
            newUser.setAddress(address);
            newUser.setSocialType(registrationId.toUpperCase());
            newUser.setSocialId(attributes.getOauth2UserInfo().getId());
            newUser.setReceiveMail(true); // 기본값

            // 비밀번호 암호화하여 저장
            String encodedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encodedPassword);

            UserRole role = new UserRole();
            role.setRoleName("ROLE_USER");

            List<UserRole> roleList = new ArrayList<>();
            roleList.add(role);
            newUser.setRoleList(roleList);

            userMapper.join(newUser);

            UserRole userRole = new UserRole();
            userRole.setUserId(newUser.getUserId());
            userRole.setRoleName("ROLE_USER");
            userMapper.insertAuth(userRole);

            // 세션에서 임시 데이터 제거
            session.removeAttribute("oauthAttributes");
            session.removeAttribute("registrationId");
            session.removeAttribute("requireAdditionalInfo");

            return "redirect:/";
        } catch (Exception e) {
            log.error("소셜 사용자 등록 중 오류 발생", e);
            return "redirect:/login?error=registration";
        }
    }
}