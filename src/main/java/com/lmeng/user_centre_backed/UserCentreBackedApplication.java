package com.lmeng.user_centre_backed;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//让spring扫描mapper包下的类
@MapperScan("com.lmeng.user_centre_backed.mapper")
@SpringBootApplication
public class UserCentreBackedApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserCentreBackedApplication.class, args);
    }

}
