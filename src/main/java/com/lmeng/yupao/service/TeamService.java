package com.lmeng.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lmeng.yupao.common.BaseResponse;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.dto.TeamQuery;
import com.lmeng.yupao.model.request.TeamJoinRequest;
import com.lmeng.yupao.model.request.TeamUpdateRequest;
import com.lmeng.yupao.model.vo.TeamUserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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

    /**
     * 列出队伍信息
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 修改队伍信息
     * @param teamUpdateRequest
     * @param loginUser
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);
}
