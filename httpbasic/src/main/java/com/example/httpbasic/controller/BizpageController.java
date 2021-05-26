package com.example.httpbasic.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class BizpageController {

    // 登录
    @PostMapping("/login")
    public String index(String username,String password) {
        return "index";  //index.html
    }

    // 日志管理
    @GetMapping("/syslog")
    public String showOrder() {
        return "syslog"; //syslog.html
    }

    // 用户管理
    @GetMapping("/sysuser")
    public String addOrder() {
        return "sysuser"; //sysuser.html
    }

    // 具体业务一
    @GetMapping("/biz1")
    public String updateOrder() {
        return "biz1";  //biz1.html
    }

    // 具体业务二
    @GetMapping("/biz2")
    public String deleteOrder() {
        return "biz2";  //biz2.html
    }
}
