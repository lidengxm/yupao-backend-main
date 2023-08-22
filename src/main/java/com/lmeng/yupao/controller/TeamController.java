package com.lmeng.yupao.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.model.request.DeleteRequest;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.common.ResultUtils;
import com.lmeng.yupao.exception.BaseException;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.domain.UserTeam;
import com.lmeng.yupao.model.dto.TeamQuery;
import com.lmeng.yupao.model.request.*;
import com.lmeng.yupao.model.vo.TeamUserVO;
import com.lmeng.yupao.service.TeamService;
import com.lmeng.yupao.service.UserService;
import com.lmeng.yupao.service.UserTeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @version 1.0
 * @learner Lmeng
 * @date 2023/7/8
 */
@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = {"http://localhost:3000"} )
@CrossOrigin(origins = {"http://124.220.222.98"} )
@Slf4j
@Api(tags = "队伍管理模块")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    @ApiOperation(value = "创建队伍")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if(teamAddRequest == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        //这里不需要再判断当前登录用户是否为空，在getLoginUser方法中已经验证过了
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team,loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    @ApiOperation(value = "删除队伍")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if(deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(teamId,loginUser);
        if(!result) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"删除队伍失败");
        }
        return ResultUtils.success(true);
    }

    @PostMapping("/update")
    @ApiOperation(value = "更新队伍")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if(teamUpdateRequest == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest,loginUser);
        if(!result) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"更新失败");
        }
        return ResultUtils.success(true);
    }

    @GetMapping("/get")
    @ApiOperation(value = "查看队伍")
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
    @ApiOperation(value = "查看队伍")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        boolean isAdmin = userService.isAdmin(request);
        //1.查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,isAdmin);
        List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        //2.判断当前用户是否已加入队伍（设置hasJoin的值）
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        //注意：这里使用了try-catch是因为在获取当前用户的函数里会判空抛出异常，但事实上用户未登录也应该能查询队伍列表
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId",loginUser.getId());
            userTeamQueryWrapper.in("teamId",teamIdList);
            //根据用户id和队伍id列表查询到的与该用户有关的队伍集合
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //将队伍集合转为teamId集合
            Set<Long> hasJoinTeamList = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                //判断teamId集合中的每一个id是否存在于队伍集合的每一个队伍中并设置判断结果
                boolean hasJoin = hasJoinTeamList.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) { }

        //3.关联查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> teamQueryWrapper = new QueryWrapper<>();
        teamQueryWrapper.in("teamId",teamIdList);
        //根据teamIdList查询出用户-队伍集合
        List<UserTeam> userTeamList = userTeamService.list(teamQueryWrapper);
        //将用户-队伍集合按照teamId进行分组得到map集合
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        //从teamIdUserTeamList获取对应的userTeam列表，得到的列表大小（元素个数）设为已加入人数
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList
                .getOrDefault(team.getId(),new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    @ApiOperation(value = "分页查询队伍")
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

    @PostMapping("/join")
    @ApiOperation(value = "加入队伍")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest,HttpServletRequest request) {
        if(teamJoinRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/exit")
    @ApiOperation(value = "退出队伍")
    public BaseResponse<Boolean> exitTeam(@RequestBody TeamExitRequest teamExitRequest,HttpServletRequest request) {
        if(teamExitRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.exitTeam(teamExitRequest,loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 列举我创建的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/create")
    @ApiOperation(value = "查看我创建的队伍")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        teamQuery.setUserId(loginUser.getId());
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @param request
     * @return
     */
    @GetMapping("/list/my/join")
    @ApiOperation(value = "查看我加入的队伍")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery,HttpServletRequest request) {
        if(teamQuery == null) {
            throw new BaseException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        //开闭原则，只增加代码不修改代码
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);
        //取出不重复的队伍id
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().
                collect(Collectors.groupingBy(UserTeam::getTeamId));
        //将map的键取出来就是id集合
        List<Long> idList = new ArrayList<>(listMap.keySet());
        teamQuery.setIdList(idList);
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery,true);
        return ResultUtils.success(teamList);
    }
}
