package com.lmeng.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.model.domain.User;

/**
* @author 26816
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2023-07-08 16:34:03
*/
public interface TeamService extends IService<Team> {


    /**
     * 添加队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
