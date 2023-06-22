package com.example.springboottest.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MyController {

    @RequestMapping("/")
    public String index() {
        return "index";
    }

//    @PostMapping("/submit")
//    public String submitForm(@RequestParam("url") String url) {
//        System.out.println("Entered name: " + url);
//        return "redirect:/";
//    }

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("saxeli", name);
        return "greeting";
    }

}
