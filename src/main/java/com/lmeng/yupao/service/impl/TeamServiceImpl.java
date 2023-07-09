package com.lmeng.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.mapper.TeamMapper;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.domain.UserTeam;
import com.lmeng.yupao.model.enums.TeamStatusEnum;
import com.lmeng.yupao.service.TeamService;
import com.lmeng.yupao.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author 26816
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-07-08 16:34:03
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{

    @Resource
    private UserTeamService userTeamService;

    @Override
    public long addTeam(Team team, User loginUser) {

        //1.请求参数是否为空
        if(team == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        //2.未登录不允许创建
        if(loginUser == null) {
            throw new BaseException(ErrorCode.NOT_LOGIN);
        }
        Long userId = loginUser.getId();
        //3.校验信息
        //3.1队伍人数 >1 且 <20
        Integer teamMaxNum = team.getMaxNum();
        if(teamMaxNum <= 1 || teamMaxNum >= 20) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍人数要大于1小于20");
        }
        //3.2队伍标题字数<=20
        if(team.getName().length() < 1 || team.getName().length() > 20) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍标题字数小于等于20");
        }
        //3.3描述信息小于512
        String teamDescription = team.getDescription();
        if(StringUtils.isNotBlank(teamDescription) && teamDescription.length() > 512) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"描述信息过长");
        }
        //3.4队伍状态status是否公开（Int），不传默认为0（公开）
        Integer status = Optional.ofNullable(team.getStatus()).orElse(0);
        //根据status的值获取对应的枚举值
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(teamStatusEnum == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍状态错误");
        }
        //3.5如果status是加密状态，密码一定要有且长度在4-8之间
        String password = team.getPassword();
        if(teamStatusEnum == TeamStatusEnum.SECRET) {
            if(StringUtils.isBlank(password) || password.length() < 4 || password.length() > 8) {
                throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍密码不规范");
            }
        }
        //3.6队伍超时时间不能大于当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"过期时间要在当前时间之后");
        }
        //3.7每个用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        if(count(queryWrapper) >= 5) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        //4.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean save = this.save(team);
        Long teamId = team.getId();
        if(!save || team == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        //5.插入用户 => 队伍关系到关系表中
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean result = userTeamService.save(userTeam);
        if(!result) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return teamId;
    }
}




