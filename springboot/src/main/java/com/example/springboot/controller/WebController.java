package com.example.springboot.controller;

import com.example.springboot.exception.CustomerException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


@RestController
public class WebController {

    @GetMapping("/hello")
    public  String hello(){

        return "hello";
    }



}
