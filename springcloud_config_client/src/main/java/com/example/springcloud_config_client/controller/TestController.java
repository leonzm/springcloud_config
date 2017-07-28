package com.example.springcloud_config_client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: Leon
 * @CreateDate: 2017/7/28
 * @Description:
 * @Version: 1.0.0
 */
@RefreshScope
@RestController
public class TestController {


    @Value("${from}")
    private String from;
    @Autowired
    private Environment environment;

    @RequestMapping(value = "/from", method = RequestMethod.GET)
    public String from() {
        return this.from;
    }

    @RequestMapping(value = "/from2", method = RequestMethod.GET)
    public String from2() {
        return this.environment.getProperty("from", "undefined");
    }

}
