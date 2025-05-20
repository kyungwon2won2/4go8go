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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

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

        // 생년월일 정보 확인 및 추가 (있는 경우)
        Map<String, Object> attr = attributes.getAttributes();
        String registrationId = (String) session.getAttribute("registrationId");

        // 소셜 로그인 제공자별로 생년월일 정보 추출 시도
        if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attr.get("response");
            if (response != null && response.containsKey("birthyear") && response.containsKey("birthday")) {
                String birthYear = (String) response.get("birthyear");
                String birthday = (String) response.get("birthday");
                // 예: 1990-01-01 형태로 변환
                model.addAttribute("birthDate", birthYear + "-" + birthday.replace(".", "-"));
            }
        } else if ("kakao".equals(registrationId)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attr.get("kakao_account");
            if (kakaoAccount != null && kakaoAccount.containsKey("birthday")) {
                // 카카오는 생일만 제공하는 경우가 많음
                String birthday = (String) kakaoAccount.get("birthday");
                // 형식에 맞게 처리
                if (birthday != null) {
                    model.addAttribute("birthDate", "2000-" + birthday.substring(0, 2) + "-" + birthday.substring(2, 4));
                }
            }
        }

        return "oauth2/additional-info";
    }

    @PostMapping("/additional-info")
    public String processAdditionalInfo(@RequestParam String phone,
                                        @RequestParam(required = false) String nickname,
                                        @RequestParam(required = false) String address,
                                        @RequestParam String birthDate,
                                        HttpSession session) {
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

            // 생년월일 설정
            if (birthDate != null && !birthDate.isEmpty()) {
                LocalDate localDate = LocalDate.parse(birthDate);
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                newUser.setBirthDate(date);
            }

            // 임시 비밀번호 생성 (소셜 로그인이므로 실제로는 사용되지 않음)
            String tempPassword = UUID.randomUUID().toString();
            String encodedPassword = passwordEncoder.encode(tempPassword);
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