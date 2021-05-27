package com.example.formlogincustom.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 动态资源鉴权规则
 */
@Component("myRBACService")
public class MyRBACService {

    /**
     * 判断某用户是否具有该request资源的访问权限
     */
    public boolean hasPermission(HttpServletRequest request, Authentication authentication){

        // 用户的urls（即资源访问路径、资源唯一标识）
        Object principal = authentication.getPrincipal();

        // 能够和request.getRequestURI()请求资源路径相匹配
        if(principal instanceof UserDetails){
            UserDetails userDetails = ((UserDetails)principal);
            List<GrantedAuthority> authorityList =
                    AuthorityUtils.commaSeparatedStringToAuthorityList(request.getRequestURI());
            return userDetails.getAuthorities().contains(authorityList.get(0));

        }
        return false;
    }
}
