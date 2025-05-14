package com.example.demo.domain.user.controller;

import com.example.demo.common.security.service.UserServiceImpl;
import com.example.demo.domain.user.dto.TestDto;
import com.example.demo.domain.user.model.UserRole;
import com.example.demo.domain.user.model.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.ZoneId;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class TestController {

    private final UserServiceImpl userServiceImpl;


        @GetMapping("/join")
        public String showJoinPage() {
            return "join"; // templates/join.html 렌더링
        }

        @GetMapping("/login")
        public String login() {
            return "login"; // templates/join.html 렌더링
        }
    @PostMapping("/join")
    public String CreateUser(@ModelAttribute TestDto testDto) {
        Users user = new Users();
        user.setEmail(testDto.getEmail());
        user.setPassword(testDto.getPassword());
        user.setName(testDto.getName());       // 추가
        user.setPhone(testDto.getPhone());     // 추가
        user.setNickname(testDto.getNickname());
        user.setAddress(testDto.getAddress());

        // 이메일 인증 상태를 true로 설정 테스트용
        user.setEmailVerified(true);

        // LocalDate를 Date로 변환
        if (testDto.getBirthDate() != null) {
            Date birthDate = Date.from(testDto.getBirthDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            user.setBirthDate(birthDate);
        }

        user.setReceiveMail(testDto.isReceiveMail());

        // 나머지 코드는 동일
        UserRole role = new UserRole();
        role.setRoleName("ROLE_USER");

        List<UserRole> roleList = new ArrayList<>();
        roleList.add(role);
        user.setRoleList(roleList);

        try {
            userServiceImpl.join(user);
        } catch (Exception e) {
            System.out.println("회원가입 오류: " + e.getMessage());
        }
        return "redirect:/login";
    }

}
