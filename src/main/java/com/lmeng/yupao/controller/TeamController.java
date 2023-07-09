package com.lmeng.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.common.ResultUtils;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.dto.TeamQuery;
import com.lmeng.yupao.model.request.TeamRequest;
import com.lmeng.yupao.service.TeamService;
import com.lmeng.yupao.service.UserService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/8
 */
@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:3000"} )
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addTeam(@RequestBody TeamRequest teamRequest, HttpServletRequest request) {
        if(teamRequest == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        //这里不需要再判断当前登录用户是否为空，在getLoginUser方法中已经验证过了
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamRequest,team);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id) {
        if(id <= 0) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        boolean result = teamService.removeById(id);
        if(!result) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team) {
        if(team == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        boolean result = teamService.updateById(team);
        if(!result) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam( Long id) {
        if(id <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"请求参数错误");
        }
        Team team = teamService.getById(id);
        if(team == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求数据不存在");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery) {
        if(teamQuery == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(team,teamQuery);
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPages(TeamQuery teamQuery) {
        if(teamQuery == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery,team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }
}
