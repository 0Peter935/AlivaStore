package com.store.erp.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/login")
    public String mostrarLogin() {
        return "Login";
    }

    @GetMapping("/")
    public String mostrarHome() {
        return "Home";
    }

}
