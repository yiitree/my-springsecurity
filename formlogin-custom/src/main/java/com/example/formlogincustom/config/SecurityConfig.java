package com.example.formlogincustom.config;

import com.example.formlogincustom.config.handle.*;
import com.example.formlogincustom.service.MyUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.annotation.Resource;
import javax.sql.DataSource;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Resource
    private DataSource dataSource;
    @Resource
    private MyUserDetailsService myUserDetailsService;
    @Resource
    private MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Resource
    private MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    @Resource
    private MyAuthenticationEntryPoint myAuthenticationEntryPoint;
    @Resource
    private MyAccessDeniedHandler myAccessDeniedHandler;
    @Resource
    private MyExpiredSessionStrategy myExpiredSessionStrategy;
    @Resource
    private MyLogoutSuccessHandler myLogoutSuccessHandler;
    @Resource
    private MyInvalidSessionStrategy myInvalidSessionStrategy;
    @Resource
    private PersistentTokenRepository persistentTokenRepository;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            //禁用跨站csrf攻击防御，后面的章节会专门讲解
            .csrf().disable()

            // 登录
            .formLogin()
                // 登录表单form中action的地址，也就是处理认证请求的路径
                .loginProcessingUrl("/login")
                //登录表单form中action的地址，也就是处理认证请求的路径
                .usernameParameter("uname")
                //form中密码输入框input的name名，不修改的话默认是password
                .passwordParameter("pword")
                // 登录和失败处理方式
                .successHandler(myAuthenticationSuccessHandler)
                .failureHandler(myAuthenticationFailureHandler)

            // 退出
            .and().logout()
                .logoutUrl("/signout")
                // 要删除cookie名称
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler(myLogoutSuccessHandler)

            // 权限处理
            .and().exceptionHandling()
                // 没有权限访问
                .accessDeniedHandler(myAccessDeniedHandler)
                // 没有登录就访问
                .authenticationEntryPoint(myAuthenticationEntryPoint)

            .and().authorizeRequests()
                // 无需登录即可访问
                .antMatchers("/login.html","/login","/invalidSession.html", "/kaptcha","/smscode","/smslogin").permitAll()
                // 登陆之后即可访问
                .antMatchers("/index").authenticated()
                // 需要相关角色才可访问
                .anyRequest().access("@myRBACService.hasPermission(request,authentication)")

            // 记住我 checkbox勾选框name属性的值目前必须是“remember-me” 默认效果是：2周
            .and().rememberMe()
                // 把登录信息保存到数据库，重启不会影响
                .tokenRepository(persistentTokenRepository)
                // from表单中checkbox的name属性要对应的更改。如果不设置默认是remember-me。
                .rememberMeParameter("remember-me-new")
                // 保存在浏览器端的cookie的名称，如果不设置默认也是remember-me。
                .rememberMeCookieName("remember-me-cookie")
                // 单位秒
                .tokenValiditySeconds(2 * 24 * 60 * 60)

            .and().sessionManagement()
                // 登录过期，需要重新登录
//                .invalidSessionStrategy(myInvalidSessionStrategy)
                .invalidSessionUrl("/invalidSession.html")
                // 同一个账号最大登录数
                .maximumSessions(1)
                // session保护策略：
                // true表示已经登录就不予许再次登录，
                // false表示允许再次登录但是之前的登录账户会被踢下线
                .maxSessionsPreventsLogin(false)
                // session被下线(超时)之后的处理策略。
                .expiredSessionStrategy(myExpiredSessionStrategy);
    }

    /**
     * 自定义用户登录，并且设置加密加密方式，这样获取密码的时候就不需要再手动转化了
     * @param auth
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 加密方式
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * 白名单
     * @param web
     */
    @Override
    public void configure(WebSecurity web) {
        //将项目中静态资源路径开放出来 --- 这里的白名单是不用经过过滤器，一般存放静态资源的，controller白名单放在上面.permitAll()
        web.ignoring().antMatchers( "/css/**", "/fonts/**", "/img/**", "/js/**");
    }

    /**
     * 把登录信息保存到数据库中，重启项目也不会影响登录
     * 主要是用在记住我功能，保持登录时间可以长时间有效，防止重启一下，就需要重新登录
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl tokenRepository = new JdbcTokenRepositoryImpl();
        tokenRepository.setDataSource(dataSource);
        return tokenRepository;
    }

}
