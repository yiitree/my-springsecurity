package com.example.formlogincustom.service;

import com.example.formlogincustom.domain.MyUserDetails;
import com.example.formlogincustom.mapper.MyUserDetailsServiceMapper;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 动态登录验证
 * UserDetailsService为springsecurity自带的用户登录判断
 */
@Component
public class MyUserDetailsService implements UserDetailsService {

    @Resource // 和@Autowird类似，Resource根据类名称注入
    MyUserDetailsServiceMapper myUserDetailsServiceMapper;

    /**
     * springsecurity自动调用loadUserByUsername进行登录判断
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

//        String password; //密码 1
//        String username;  //用户名 1
//        boolean enabled;  //账号是否可用 1
//        boolean accountNonLocked;   //是否没被锁定 1 但是比如accountNonLocked字段用于登录多次错误锁定，但我们一般不会在表里存是否锁定，而是存一个锁定时间字段。
//        Collection<? extends GrantedAuthority> authorities;  //用户的权限集合 1
//        boolean accountNonExpired;   //是否没过期
//        boolean credentialsNonExpired;  //认证是否没过期

        // 加载基础用户信息 username,password,enabled
        MyUserDetails myUserDetails = myUserDetailsServiceMapper.findByUserName(username);

        // 加载用户角色列表 role_code --- admin
        List<String> roleCodes = myUserDetailsServiceMapper.findRoleByUserName(username);
        // 通过用户角色列表加载用户的资源权限列表 url --- /sys_user
        List<String> authorities = myUserDetailsServiceMapper.findAuthorityByRoleCodes(roleCodes);
        // 角色是一个特殊的权限，ROLE_前缀
        roleCodes = roleCodes.stream().map(rc -> "ROLE_" +rc).collect(Collectors.toList());
        authorities.addAll(roleCodes);

        // 角色标识 + 权限标识 --- [/sys_user, /sys_log, ROLE_admin]
        myUserDetails.setAuthorities(
                AuthorityUtils.commaSeparatedStringToAuthorityList(
                        String.join(",",authorities)
                )
        );

        // 是否没过期
        myUserDetails.setAccountNonExpired(true);
        // 认证是否没过期
        myUserDetails.setCredentialsNonExpired(true);

        return myUserDetails;
    }
}
