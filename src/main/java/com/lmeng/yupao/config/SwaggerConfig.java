package com.lmeng.yupao.config;

import com.fasterxml.jackson.databind.annotation.JsonAppend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/6/29
 */
@Configuration
@EnableOpenApi
@Profile({"dev","test"})
public class SwaggerConfig {
    /**
     * 创建API应用
     * apiInfo() 增加API相关信息
     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
     * 指定扫描的包路径来定义指定要建立API的目录。
     * @return
     */
    @Bean
    public Docket coreApiConfig(){
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(adminApiInfo())
                .select()
                //标注控制器的位置
                .apis(RequestHandlerSelectors.basePackage("com.lmeng.yupao.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo adminApiInfo(){
        return new ApiInfoBuilder()
                .title("yupao伙伴匹配")
                .description("yupao接口文档")
                .version("1.0")
                .contact(new Contact("yupao","http://baidu.com","728831102@qq.com"))
                .build();
    }
}

