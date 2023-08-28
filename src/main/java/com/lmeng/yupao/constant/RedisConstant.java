package com.lmeng.yupao.constant;

/**
 * @learner lmeng
 * @date 2023/8/28
 * @des
 */
public interface RedisConstant {
    /**
     * 用户列表redis的key
     */
    String RECOMMEND_KEY = "yupao:user:recommend";

    /**
     * 用户列表redis的key的过期时间
     */
    Long RECOMMEND_KEY_TTL = 30L;

    /**
     * 用户更新的key
     */
    String RECOMMEND_UPDATE_KEY = "yupao:user:update";


}
