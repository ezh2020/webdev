package com.webdev.webdev.controller;

import com.webdev.webdev.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DefaultController {
    @Autowired
    private Environment env;

    @RequestMapping("/getPort")
    public String getPort() {
        String port = env.getProperty("server.port");
        System.out.println("server.port: " + port);
        return "server port: " + port;
    }
}
