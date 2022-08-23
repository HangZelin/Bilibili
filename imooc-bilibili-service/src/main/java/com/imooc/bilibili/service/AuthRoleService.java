package com.imooc.bilibili.service;

import com.imooc.bilibili.dao.AuthRoleDao;
import com.imooc.bilibili.domain.auth.AuthRole;
import com.imooc.bilibili.domain.auth.AuthRoleElementOperation;
import com.imooc.bilibili.domain.auth.AuthRoleMenu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AuthRoleService {

    @Autowired
    AuthRoleElementOperationService authRoleElementOperationService;

    @Autowired
    AuthRoleMenuService authRoleMenuService;

    @Autowired
    AuthRoleDao authRoleDao;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleElementOperationList) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleElementOperationList);
    }

    public AuthRole getRoleByCode(String code) {
        return authRoleDao.getRoleByCode(code);
    }
}
