package org.itmo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GUIController {

    @GetMapping("/")
    public String home() {
        return "index";
    }


}