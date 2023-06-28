package com.lmeng.yupao;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//让spring扫描mapper包下的类
@MapperScan("com.lmeng.yupao.mapper")
@SpringBootApplication
public class yupaoApplication {

    public static void main(String[] args) {
        SpringApplication.run(yupaoApplication.class, args);
    }

}
