package com.hsbc.wechat.controller;

import com.hsbc.wechat.service.WeChatContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkWeChatController {

    @Autowired
    WeChatContentService weChatContentService;
    @GetMapping("/test/{seq}")
    public void contextLoads(@PathVariable("seq") long seq) {
        weChatContentService.test(seq);
    }
}
