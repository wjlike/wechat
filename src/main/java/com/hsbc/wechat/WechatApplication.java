package com.hsbc.wechat;

import com.hsbc.wechat.config.BussinessConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;

@SpringBootApplication
@EnableScheduling
public class WechatApplication {

	public static void main(String[] args) {
		System.setProperty("jasypt.encryptor.password", BussinessConfig.JASYPT_ENCRYPTOR_PASSWORD);
		SpringApplication.run(WechatApplication.class, args);
		new HashMap<String,Object>();
	}

}
