package com.example.victorsport.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebViewController {

    @GetMapping({"/", "/shop"})
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping({"/cart", "/gio-hang"})
    public String cart() {
        return "forward:/cart.html";
    }

    @GetMapping({"/checkout", "/thanh-toan"})
    public String checkout() {
        return "forward:/checkout.html";
    }

    @GetMapping({"/tra-cuu", "/tra-cuu-don-hang"})
    public String orderTracking() {
        return "forward:/order-success.html";
    }
}
