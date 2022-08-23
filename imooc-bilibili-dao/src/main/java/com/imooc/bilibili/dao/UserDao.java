package com.imooc.bilibili.dao;

import com.imooc.bilibili.domain.RefreshTokenDetail;
import com.imooc.bilibili.domain.User;
import com.imooc.bilibili.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface UserDao {
    User getUserByPhone(String phone);

    User getUserByEmail(String phone);

    User getUserById(Long id);

    UserInfo getUserInfoByUserId(Long id);

    Integer addUser(User user);

    Integer addUserInfo(UserInfo userInfo);

    Integer updateUserInfos(UserInfo userInfo);

    Integer updateUsers(User user);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    Integer pageCountUserInfos(Map<String, Object> params);

    List<UserInfo> pageListUserInfos(Map<String, Object> params);

    Integer deleteRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId);

    Integer addRefreshToken(@Param("refreshToken") String refreshToken, @Param("userId") Long userId, @Param("createTime") Date date);

    RefreshTokenDetail getRefreshTokenDetail(String refreshToken);
}
