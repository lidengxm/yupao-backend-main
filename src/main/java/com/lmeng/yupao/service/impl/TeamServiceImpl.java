package com.lmeng.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.domain.Team;
import com.lmeng.yupao.mapper.TeamMapper;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.domain.UserTeam;
import com.lmeng.yupao.model.dto.TeamQuery;
import com.lmeng.yupao.model.enums.TeamStatusEnum;
import com.lmeng.yupao.model.request.TeamExitRequest;
import com.lmeng.yupao.model.request.TeamJoinRequest;
import com.lmeng.yupao.model.request.TeamUpdateRequest;
import com.lmeng.yupao.model.vo.TeamUserVO;
import com.lmeng.yupao.model.vo.UserVO;
import com.lmeng.yupao.service.TeamService;
import com.lmeng.yupao.service.UserService;
import com.lmeng.yupao.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
* @author 26816
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2023-07-08 16:34:03
*/
@Slf4j
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements TeamService{
    @Resource
    private UserService userService;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private RedissonClient redissonClient;

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
            if(StringUtils.isBlank(password) || password.length() < 3 || password.length() > 8) {
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


    @Override
    public List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        //组合查询条件
        if(teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            //查询idList中的id
            List<Long> idList = teamQuery.getIdList();
            if(CollectionUtils.isNotEmpty(idList)) {
                queryWrapper.in("id",idList);
            }
            String searchText = teamQuery.getSearchText();
            //获取查询关键词，根据查询关键词进行查询
            if(StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("name",searchText).or().
                        like("description",searchText));
            }
            String name = teamQuery.getName();
            //根据队伍名称进行查询
            if(StringUtils.isNotBlank(name)) {
                queryWrapper.eq("name",name);
            }
            String description = teamQuery.getDescription();
            //根据队伍描述进行查询
            if(StringUtils.isNotBlank(description)) {
                queryWrapper.eq("description",description);
            }
            Integer maxNum = teamQuery.getMaxNum();
            //根据队伍最大人数查询
            if(maxNum != null && maxNum > 1 && maxNum < 20) {
                queryWrapper.eq("maxNum",maxNum);
            }
            Long userId = teamQuery.getUserId();
            //根据创建队伍人查询
            if(userId != null && userId > 0) {
                queryWrapper.eq("userId",userId);
            }

            Integer status = teamQuery.getStatus();
            //获取队伍状态，如果没有指定队伍状态默认为公开
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if(teamStatusEnum == null) {
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            //如果不是管理员用户而且队伍是私有的就抛出异常，没有权限
            if(!isAdmin && teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
                throw new BaseException(ErrorCode.NO_AUTH);
            }
            queryWrapper.eq("status",teamStatusEnum.getValue());
        }

        //不展示过期的队伍 expireTime is not null && expireTime > now()
        queryWrapper.and(qw -> qw.gt("expireTime",new Date()).isNotNull("expireTime"));

        //关联查询已加入队伍的用户的信息
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        List<TeamUserVO> teamUserVOList = new ArrayList<>();
        //关联查询创建人的信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if(userId == null) {
                continue;
            }
            TeamUserVO teamUserVO = new TeamUserVO();
            BeanUtils.copyProperties(team,teamUserVO);
            User user = userService.getById(userId);
            //脱敏用户信息
            if(user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user,userVO);
                teamUserVO.setCreateUser(userVO);
            }
            teamUserVOList.add(teamUserVO);
        }
        return teamUserVOList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        //1.判断请求参数是否为空，不为空则校验队伍Id
        if(teamUpdateRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamUpdateRequest.getId();
        Team oldTeam = this.getTeamByTeamId(teamId);

        //2.只有管理员和创建队伍人才能修改
        if(!oldTeam.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BaseException(ErrorCode.NO_AUTH,"管理员和创建人才能修改");
        }
        //3.如果队伍是加密状态，密码不能为空
        Integer status = teamUpdateRequest.getStatus();
        if(TeamStatusEnum.getEnumByValue(status) == TeamStatusEnum.SECRET) {
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BaseException(ErrorCode.NULL_ERROR,"加密状态的队伍密码不能为空");
            }
        }
        Team updateTeam = new Team();
        //4.保存更新
        BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
        return this.updateById(updateTeam);
    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if(teamJoinRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        Long teamId = teamJoinRequest.getTeamId();
        //根据队伍id获取队伍信息
        Team team = this.getTeamByTeamId(teamId);

        //1.不能加入已过期的队伍
        Date expireTime = team.getExpireTime();
        if(expireTime != null && expireTime.before(new Date())) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍已过期，无法加入");
        }

        //2.私有队伍不允许加入
        Integer status = team.getStatus();
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(teamStatusEnum)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"私有队伍不允许加入！");
        }
        String password = teamJoinRequest.getPassword();
        //3.加密队伍需要密码匹配
        if(TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if(StringUtils.isBlank(password)) {
                throw new BaseException(ErrorCode.PARAMS_ERROR,"加密队伍，必须输入队伍密码");
            }
            if(!password.equals(team.getPassword())) {
                throw new BaseException(ErrorCode.PARAMS_ERROR,"密码错误");
            }
        }

        //分布式锁保证只能有同时加入一次队伍
        RLock lock = redissonClient.getLock("yupao:joinTeam:lock");
        try {
            int tryLockCount = 0;
            while(true) {
                //线程每尝试加锁一次次数就++
                tryLockCount ++;
                //如果当前线程获得了锁
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    //4.用户最多加入和创建5个队伍，小tip，需要查询数据库放在逻辑下面减少查询时间
                    Long userId = loginUser.getId();
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    //select count(*) from user_team where userId = ?
                    userTeamQueryWrapper.eq("userId", userId);
                    //查询该用户已加入多少队伍（通过userId字段查询user_team表）
                    int hasJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasJoinTeam > 5) {
                        throw new BaseException(ErrorCode.PARAMS_ERROR, "用户最多加入或创建5个队伍");
                    }

                    //5.不能加入重复已加入的队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", userId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    int hasUserJoinTeam = userTeamService.count(userTeamQueryWrapper);
                    if (hasUserJoinTeam > 0) {
                        throw new BaseException(ErrorCode.PARAMS_ERROR, "用户不能重复加入队伍");
                    }

                    //6.用户不能加入已满的队伍
                    long teamHasJoinNum = this.getJoinTeamNum(teamId);
                    if (teamHasJoinNum == team.getMaxNum()) {
                        throw new BaseException(ErrorCode.PARAMS_ERROR, "该队伍已满！");
                    }

                    //7.修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setTeamId(teamId);
                    userTeam.setUserId(userId);
                    userTeam.setJoinTime(new Date());
                    //8.返回结果
                    return userTeamService.save(userTeam);
                }
                //如果一个线程尝试了3次加锁都失败了，就取消任务
                if (tryLockCount >= 3) {
                    throw new BaseException(ErrorCode.PARAMS_ERROR,"Task cancelled due to multiple failed lock attempts");
                }
            }
        } catch (InterruptedException e) {
            log.info("doPreCacheJob error",e);
            return false;
        } finally {
            //执行完任务一定要释放锁（先检查是否是当前线程加的锁）
            System.out.println("unLock: "+Thread.currentThread().getId());
            if(lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean exitTeam(TeamExitRequest teamExitRequest, User loginUser) {
        //1.校验退出队伍参数
        if (teamExitRequest == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }

        //2.校验队伍是否存在
        Long teamId = teamExitRequest.getTeamId();
        //判断队伍是否存在，不仅要判空还要判断是否小于0（避免缓存穿透）
        Team team = this.getTeamByTeamId(teamId);

        //3.校验用户是否已加入队伍
        Long userId = loginUser.getId();
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("teamId", teamId);
        int joinTeamCount = userTeamService.count(queryWrapper);
        if (joinTeamCount == 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "用户未加入该队伍！");
        }

        //4.用户退出队伍的时候，如果队伍只剩一人，队伍解散，并将用户从队伍移除
        long remainTeamNum = this.getJoinTeamNum(teamId);
        if (remainTeamNum == 1) {
            //删除队伍表中队伍的数据
            this.removeById(teamId);
            //删除队伍和所有加入队伍的关系
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            return userTeamService.remove(userTeamQueryWrapper);
        } else {
            //5.1用户退出的时候还有其他人，队长退出队伍
            //如果当前登录用户是队伍队长
            if (team.getUserId().equals(userId)) {
                //把队伍队长转交给第二早加入的用户（加入时间 || id最小）
                //1.查询已加入队伍的所有用户和加入时间
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                //2.只查询两条
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BaseException(ErrorCode.PARAMS_ERROR);
                }
                UserTeam userTeam = userTeamList.get(1);
                Long secondJoinId = userTeam.getUserId();
                //将队长转交
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(secondJoinId);
                //更新队伍表的队长id信息（userId）
                boolean update = this.updateById(updateTeam);
                if (!update) {
                    throw new BaseException(ErrorCode.PARAMS_ERROR, "更新队伍队长失败！");
                }
                //删除之前队长在队伍-用户表的数据
                return userTeamService.remove(queryWrapper);
            }
            //5.2用户退出的时候还有其他人，其他人退出队伍
            //删除之前队长在队伍-用户表的数据
            QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
            userTeamQueryWrapper.eq("teamId", teamId);
            userTeamQueryWrapper.eq("userId",userId);
            return userTeamService.remove(userTeamQueryWrapper);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(long teamId, User loginUser) {
        //1.校验队伍是否存在
        Team team = this.getTeamByTeamId(teamId);
        //2.校验用户是否是队长
        Long userId = loginUser.getId();
        //如果不是队长无权限解散队伍
        if(!team.getUserId().equals(userId)) {
            throw new BaseException(ErrorCode.NO_AUTH,"用户非队长，无权限解散队伍！");
        }
        //3.移除队伍-用户关联信息
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        boolean remove = userTeamService.remove(userTeamQueryWrapper);
        if(!remove) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"删除队伍-用户表关联信息失败");
        }
        //4.删除队伍表中的队伍信息
        return this.removeById(teamId);
    }

    /**
     * 根据队伍id获取队伍信息
     * @param teamId
     * @return
     */
    public Team getTeamByTeamId(Long teamId) {
        if(teamId == null || teamId <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍ID不能为空或者小于0");
        }
        Team team = this.getById(teamId);
        if(team == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"队伍不存在");
        }
        return team;
    }

    /**
     * 获取该队伍当前加入人数（根据teamId查询数据行）
     * @param teamId
     * @return
     */
    public long getJoinTeamNum(Long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId",teamId);
        int joinTeamNum = userTeamService.count(userTeamQueryWrapper);
        return joinTeamNum;
    }

}




