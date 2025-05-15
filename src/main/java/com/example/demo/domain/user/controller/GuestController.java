package com.example.demo.domain.user.controller;

import com.example.demo.domain.user.dto.CreateUserDTO;
import com.example.demo.domain.user.service.GuestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class GuestController {

    private final GuestService guestService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/join")
    public String join() {
        return "join";
    }

    @PostMapping("/join")
    public String CreateUser(@ModelAttribute CreateUserDTO dto) throws Exception {

        guestService.createUser(dto);

        return "index";
    }
}