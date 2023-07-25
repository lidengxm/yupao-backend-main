package com.lmeng.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lmeng.yupao.common.ErrorCode;
import com.lmeng.yupao.constant.UserConstant;
import com.lmeng.yupao.exceeption.BaseException;
import com.lmeng.yupao.model.domain.User;
import com.lmeng.yupao.model.request.UpdateTagRequest;
import com.lmeng.yupao.model.vo.UserVO;
import com.lmeng.yupao.service.UserService;
import com.lmeng.yupao.mapper.UserMapper;
import com.lmeng.yupao.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.lmeng.yupao.constant.UserConstant.ADDMIN_ROLE;
import static com.lmeng.yupao.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author lmeng
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{

    @Resource
    private UserMapper userMapper;

    //盐值，混淆密码
    private final String SALT = "my";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @return
     */
    @Override
    public long userRegister(String username, String userAccount, String userPassword, String checkPassword) {
        //1.校验
        if(StringUtils.isAnyBlank(username,userAccount,userPassword,checkPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"参数不能为空");
        }
        if(userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户账号不能小于4位或大于15位");
        }
        if (!StringUtils.isAnyBlank(username) && username.length() > 20) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "昵称不能超过20个字符");
        }
        if(userPassword.length() < 8 && checkPassword.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"密码最少8位");
        }

        //2.校验账号中不能有特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号中不能含有特殊字符");
        }

        //3.校验密码和二次密码是否一致
        if(!userPassword.equals(checkPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }

        //4.校验账号不能重复（放在校验账号密码之后，可以省去账号密码不符合要求时的查询数据库操作）
        QueryWrapper<User> queryWrapper  = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号重复");
        }

        //5.加密
        String SecondPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(SecondPassword);
        //新注册的用户上传默认头像
        user.setAvatarUrl("https://alylmengbucket.oss-cn-nanjing.aliyuncs.com/pictures/202307091458096.webp");
        boolean flag = this.save(user);
        if(!flag) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"注册失败");
        }
        return user.getId();
    }

    /**
     * 用户登录
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.校验账号和密码是否合法
        if(StringUtils.isAnyBlank(userAccount,userPassword)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号或密码为空");
        }
        if(userAccount.length() < 4) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号长度过短");
        }
        if(userPassword.length() < 8) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"密码长度过短");
        }

        //账号中不能有特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"账号中含特殊字符");
        }

        //2.加密
        String SecondPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //根据账号密码查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword", SecondPassword);
        User user = userMapper.selectOne(queryWrapper);
        //用户不存在
        if(user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BaseException(ErrorCode.NULL_ERROR,"用户不存在");
        }

        //3.用户脱敏
        User safetyUser = getSafetyUser(user);

        //4.记录用户的登录状态
        String redisKey = String.format(USER_LOGIN_STATE + safetyUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(redisKey, safetyUser, 30, TimeUnit.MINUTES);
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param orignUser
     * @return
     */
    @Override
    public User getSafetyUser(User orignUser) {
        if(orignUser == null) {
            throw new BaseException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        User safetyUser = new User();
        safetyUser.setId(orignUser.getId());
        safetyUser.setUsername(orignUser.getUsername());
        safetyUser.setUserAccount(orignUser.getUserAccount());
        safetyUser.setAvatarUrl(orignUser.getAvatarUrl());
        safetyUser.setGender(orignUser.getGender());
        safetyUser.setEmail(orignUser.getEmail());
        safetyUser.setUserStatus(0);
        safetyUser.setPlanetCode(orignUser.getPlanetCode());
        safetyUser.setUserRole(orignUser.getUserRole());
        safetyUser.setPhone(orignUser.getPhone());
        safetyUser.setCreateTime(new Date());
        safetyUser.setTags(orignUser.getTags());
        safetyUser.setProfile(orignUser.getProfile());
        return safetyUser;
    }

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public Boolean userLogout(String id, HttpServletRequest request) {
        // 移除登录态
        String redisKey = String.format(USER_LOGIN_STATE + id);
        log.info(redisKey);
        Boolean delete = redisTemplate.delete(redisKey);
        return delete;
    }


    /**
     * 修改用户
     * @param user
     * @param loginUser
     * @return
     */
    @Override
    public int updateUser(User user, User loginUser) {
        long userId = user.getId();
        if (userId <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BaseException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BaseException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 根据标签搜索用户 SQL查询
     * @param tagNameList
     * @return
     */
    @Deprecated
    public List<User> searchByTagsBySQL(List<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        //SQL查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //拼接and查询 AND (tags LIKE ? AND tags LIKE ?)
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags",tagName);
        }
        //对查询条件进行查询并返回List集合
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map((this::getSafetyUser)).collect(Collectors.toList());
    }

    /**
     * 根据标签搜索用户 内存查询
     * @param tagNameList
     * @return
     */
    //@Override
    public List<User> searchByTags(Set<String> tagNameList) {
        if(CollectionUtils.isEmpty(tagNameList)) {
            throw new BaseException(ErrorCode.PARAMS_ERROR);
        }
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中判断是否有包含要求的标签
        //改成并发，stream改成paralleStream并行流，但用的公共线程池不安全
        return userList.stream().filter(user -> {
            String tagStr = user.getTags();
            //如果用户的标签字符串为空就直接返回false，用户不满足过滤条件
            if(StringUtils.isBlank(tagStr)) {
                return false;
            }
            //将JSON类型字符串转换成Set<String>类型的临时标签名列表
            Set<String> tempTagNameList = gson.fromJson(tagStr, new TypeToken<Set<String>>() {}.getType());
            //对得到的标签名列表进行非空判断，为空就给默认值(空的HashSet)
            tempTagNameList = Optional.ofNullable(tempTagNameList).orElse(new HashSet<>());
            //tempTagNameList集合中每一个元素首字母转换为大写
            tempTagNameList.stream().map(StringUtils::capitalize).collect(Collectors.toList());
            //遍历标签名列表，并判断是否包含当前标签名
            for (String tagName : tagNameList) {
                if(!tempTagNameList.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        //先鉴权，仅管理员可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if(user == null || user.getUserRole() != ADDMIN_ROLE) {
            return false;
        }
        return true;
    }

    /**
     * 是否为管理员
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getUserRole() == UserConstant.ADMIN_ROLE;
    }


    @Override
    public List<User> matchUsers(long num, User loginUser) {
        //1.构造查询条件，查找
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);

        //2.将标签JSON格式转化为List<String>造型师
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagsList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        List<Pair<User, Long>> list = new ArrayList<>();
        //用户列表的下标和相似度集合
        //SortedMap<Integer,Long> indexDistanceMap = new TreeMap<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签或者是当前用户自己就跳过查询
            if(StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            //将JSON格式的标签列表转为String集合形式
            List<String> userTagsList = gson.fromJson(userTags,new TypeToken<List<String>>() {
            }.getType());
            long distance = AlgorithmUtils.minDistance(tagsList, userTagsList);
            list.add(new Pair<>(user,distance));
        }
        //按照编辑距离从小到大
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num).collect(Collectors.toList());
        //原本顺序的userId列表
        List<Long> userIdList = topUserPairList.stream()
                .map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        //根据id列表查询，找出所有
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        //把id提取出来作为查询的条件
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        //根据 userIdList 中的元素顺序，从 userIdUserListMap 中获取对应的用户列表，并将每个列表的第一个用户添加到 finalUserList中
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    /**
     * 格式化字符串
     * @param key
     * @return
     */
    @Override
    public String redisFormat(Long key) {
        return String.format("yupao:user:search:%s", key);
    }

    /**
     * 流处理
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTagById(UpdateTagRequest updateTag, User currentUser) {
        long id = updateTag.getId();
        if (id <= 0) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "该用户不存在");
        }
        Set<String> newTags = updateTag.getTagList();
        if (newTags.size() > 12) {
            throw new BaseException(ErrorCode.PARAMS_ERROR, "最多设置12个标签");
        }
        if (!isAdmin(currentUser) && id != currentUser.getId()) {
            throw new BaseException(ErrorCode.NO_AUTH, "无权限");
        }
        User user = userMapper.selectById(id);
        Gson gson = new Gson();
        Set<String> oldTags = gson.fromJson(user.getTags(), new TypeToken<Set<String>>() {
        }.getType());
        Set<String> oldTagsCapitalize = toCapitalize(oldTags);
        Set<String> newTagsCapitalize = toCapitalize(newTags);

        // 添加 newTagsCapitalize 中 oldTagsCapitalize 中不存在的元素
        oldTagsCapitalize.addAll(newTagsCapitalize.stream().filter(tag -> !oldTagsCapitalize.contains(tag)).collect(Collectors.toSet()));
        // 移除 oldTagsCapitalize 中 newTagsCapitalize 中不存在的元素
        oldTagsCapitalize.removeAll(oldTagsCapitalize.stream().filter(tag -> !newTagsCapitalize.contains(tag)).collect(Collectors.toSet()));
        String tagsJson = gson.toJson(oldTagsCapitalize);
        user.setTags(tagsJson);
        return userMapper.updateById(user);
    }

    @Override
    public UserVO getUserById(Long userId, Long loginUserId) {
        User user = this.getById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * String类型集合首字母大写
     *
     * @param oldSet 原集合
     * @return 首字母大写的集合
     */
    private Set<String> toCapitalize(Set<String> oldSet) {
        return oldSet.stream().map(StringUtils::capitalize).collect(Collectors.toSet());
    }

}




