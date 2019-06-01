package com.jl.jasypt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author 蒋领
 * @date 2019年05月30日
 */
@Controller
public class MainController {
    @GetMapping("/")
    public String main(){
        return "main";
    }
}
