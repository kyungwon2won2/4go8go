package com.example.demo.domain.user.controller;

import com.example.demo.common.security.service.UserServiceImpl;
import com.example.demo.domain.user.dto.TestDto;
import com.example.demo.domain.user.model.UserAuth;
import com.example.demo.domain.user.model.Users;
import com.example.demo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        public String CreateUser(@ModelAttribute TestDto testDto){
                Users user = new Users();
                user.setUserPw(testDto.getUserPw());
                user.setName(testDto.getName());
                user.setEmail(testDto.getEmail());
                user.setRegDate(new Date());
                user.setUpdDate(new Date());
                user.setEnabled(1); // 기본값 활성화로 설정

                // 권한 설정 예시
                UserAuth auth = new UserAuth();
                auth.setUserId(testDto.getUserId());
                auth.setAuth("ROLE_USER");

                List<UserAuth> authList = new ArrayList<>();
                authList.add(auth);
                user.setAuthList(authList);
                int n = 0;
                try {
                    n = userServiceImpl.join(user);
                } catch (Exception e){
                    System.out.println("ddd : " + e.getMessage());
                }
                return "index";
            }



    }
