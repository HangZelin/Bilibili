package com.imooc.bilibili.service;

import com.alibaba.fastjson.JSONObject;
import com.imooc.bilibili.dao.UserDao;
import com.imooc.bilibili.domain.PageResult;
import com.imooc.bilibili.domain.RefreshTokenDetail;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import com.imooc.bilibili.domain.constant.UserConstant;
import com.imooc.bilibili.domain.exception.ConditionException;
import com.imooc.bilibili.service.util.MD5Util;
import com.imooc.bilibili.service.util.RSAUtil;
import com.imooc.bilibili.service.util.TokenUtil;
import com.mysql.cj.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAuthService userAuthService;
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new ConditionException("手机号不能为空!");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new ConditionException("该手机号已经注册!");
        }
        Date now = new Date();
        String salt = String.valueOf(now.getTime());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");

        user.setSalt(salt);
        user.setPassword(md5Password);
        user.setCreateTime(now);
        userDao.addUser(user);
        // 添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setCreateTime(now);
        userDao.addUserInfo(userInfo);

        //添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }


    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }


    /***
     * 这里有一个小问题，前端发送过来的请求应该是要么用手机号登录，要么用邮箱登录，所以不存在手机号邮箱同时不为空的情况。
     * 但是如果注册的时候，我只注册了手机号没有邮箱，那么登录的时候我如果给了邮箱的话那是不是应该报错呢？
     * 不知道同时在这个login方法中写手机号和邮箱会不会设计不太好，不过目前没遇到什么大问题。
     * 报错的情况:
     * 1.邮箱和手机号同时为空，这样的话登录必然会出错。
     * 2.这里给了邮箱或者手机号，但是没办法查询到用户，也就是邮箱手机号映射到的用户都为空
     * 3.邮箱和手机号没有映射到同一个用户。但是此时需要注意可能注册的时候本来就没有提供邮箱。
     *   这个情况没有写因为登录时前端应该要求只能用手机号或者邮箱进行登录，不能同时使用。
     */
    public String login(User user) throws Exception {
        //检查手机号
        String phone = user.getPhone();
        //检查邮箱号
        String email = user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new ConditionException("手机号或邮箱不能为空!");
        }
        User dbUserByPhone = this.getUserByPhone(phone);
        User dbUserByEmail = this.getUserByEmail(email);
        User dbUser;
        //判断用户是否存在
        if (dbUserByEmail == null && dbUserByPhone == null) {
            throw new ConditionException("当前用户不存在!");
        }
        if (dbUserByEmail != null) {
            dbUser = dbUserByEmail;
        } else {
            dbUser = dbUserByPhone;
        }
        //解密密码
        String password = user.getPassword();
        String rowPassword;
        try {
            rowPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rowPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误!");
        }

        //生成用户令牌
        return TokenUtil.generateToken(dbUser.getId());
    }

    public User getUserInfo(Long userId) {
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        //进行了一个user和userinfo的数据查询整合
        user.setUserInfo(userInfo);
        return user;
    }

    /**
     * 注意更新用户时需要做的事情，修改密码。
     * 首先需要获取用户id, 检查是否存在该用户。
     * 继续进行密码的修改，将RSA解码先得到，获得原始密码，接着做MD5 utf8字符集的签名，最后重新设置密码。
     */
    public void updateUsers(User user) throws Exception {
        Long id = user.getId();
        User dbUser = getUserInfo(id);
        if (dbUser == null) {
            throw new ConditionException("用户不存在!");
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            String md5Password = MD5Util.sign(rawPassword, user.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        user.setUpdateTime(new Date());
        userDao.updateUsers(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(new Date());
        userDao.updateUserInfos(userInfo);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userDao.getUserInfoByUserIds(userIdList);
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        Integer no = params.getInteger("no");
        Integer size = params.getInteger("size");
        // starting page
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        Integer total = userDao.pageCountUserInfos(params);
        List<UserInfo> list = new ArrayList<>();
        if (total > 0) {
            // 真正的分页查询
            list = userDao.pageListUserInfos(params);
        }
        return new PageResult<>(total, list);
    }

    public Map<String, Object> loginForDts(User user) throws Exception {
        //检查手机号
        String phone = user.getPhone() == null ? "": user.getPhone();
        //检查邮箱号
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new ConditionException("手机号或邮箱不能为空!");
        }
        User dbUserByPhone = this.getUserByPhone(phone);
        User dbUserByEmail = this.getUserByEmail(email);
        User dbUser;
        //判断用户是否存在
        if (dbUserByEmail == null && dbUserByPhone == null) {
            throw new ConditionException("当前用户不存在!");
        }
        if (dbUserByEmail != null) {
            dbUser = dbUserByEmail;
        } else {
            dbUser = dbUserByPhone;
        }
        //解密密码
        String password = user.getPassword();
        String rowPassword;
        try {
            rowPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new ConditionException("密码解密失败!");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rowPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new ConditionException("密码错误!");
        }

        Long userId = dbUser.getId();
        //生成用户令牌, 刷新令牌
        String accessToken = TokenUtil.generateToken(userId);
        String refreshToken = TokenUtil.generateRefreshToken(userId);

        //保存refresh token到数据库
        userDao.deleteRefreshToken(refreshToken, userId);
        userDao.addRefreshToken(refreshToken, userId, new Date());
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", refreshToken);
        result.put("refreshToken", refreshToken);
        return result;
    }

    public void logout(String refreshToken, Long userId) {
        userDao.deleteRefreshToken(refreshToken, userId);
    }

    public String refreshAccessToken(String refreshToken) throws Exception {
        RefreshTokenDetail refreshTokenDetail = userDao.getRefreshTokenDetail(refreshToken);
        if (StringUtils.isNullOrEmpty(refreshToken)) {
            throw new ConditionException("555", "token过期!");
        }
        Long userId = refreshTokenDetail.getUserId();
        return TokenUtil.generateToken(userId);
    }
}
