package com.example.formlogincustom.config.handle;

import com.example.formlogincustom.exception.AjaxResponse;
import com.example.formlogincustom.exception.CustomException;
import com.example.formlogincustom.exception.CustomExceptionType;
import com.example.formlogincustom.mapper.MyUserDetailsServiceMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.moki.ratelimitj.core.limiter.request.RequestLimitRule;
import es.moki.ratelimitj.core.limiter.request.RequestRateLimiter;
import es.moki.ratelimitj.inmemory.request.InMemorySlidingWindowRequestRateLimiter;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 登录失败处理 --- 增加限流，登录失败多次锁定
 */
@Component
public class MyAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private String loginType = "JSON";
    private static ObjectMapper objectMapper = new ObjectMapper();

    //引入MyUserDetailsServiceMapper
    @Resource
    MyUserDetailsServiceMapper myUserDetailsServiceMapper;

    //规则定义：1分钟之内5次机会，第6次失败就触发限流行为（禁止访问）
    Set<RequestLimitRule> rules =
            Collections.singleton(RequestLimitRule.of(60, TimeUnit.SECONDS,5));
    RequestRateLimiter limiter = new InMemorySlidingWindowRequestRateLimiter(rules);


    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        //从request或request.getSession中获取登录用户名
        String userId = request.getParameter("uname");

        //默认提示信息
        String errorMsg;
        //账户被锁定了
        if(exception instanceof LockedException){
            errorMsg = "您已经多次登陆失败，账户已被锁定，请稍后再试！";
        }else if(exception instanceof SessionAuthenticationException){
            errorMsg = exception.getMessage();
        }else{
            errorMsg = "请检查您的用户名和密码输入是否正确";
        }

        //每次登陆失败计数器加1，并判断该用户是否已经到了触发了锁定规则
        boolean reachLimit = limiter.overLimitWhenIncremented(userId);
        //如果触发了锁定规则，修改数据库 accountNonLocked字段锁定用户
        if(reachLimit){
            myUserDetailsServiceMapper.updateLockedByUserId(userId);
            errorMsg = "您多次登陆失败，账户已被锁定，请稍后再试！";
        }

        if (loginType.equalsIgnoreCase("JSON")) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            AjaxResponse.error(
                                    new CustomException(
                                            CustomExceptionType.USER_INPUT_ERROR,
                                            errorMsg))));
        } else {
            response.setContentType("text/html;charset=UTF-8");
            super.onAuthenticationFailure(request, response, exception);
        }

    }
}
