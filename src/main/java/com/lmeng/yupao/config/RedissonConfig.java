package com.lmeng.yupao.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/8
 */

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String port;

    private String host;

    @Bean
    public RedissonClient redissonClient() {
        //1.创建配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        //2.创建实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
