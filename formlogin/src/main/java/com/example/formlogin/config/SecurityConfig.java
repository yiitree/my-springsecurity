package com.example.formlogin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable() //禁用跨站csrf攻击防御，后面的章节会专门讲解
            .formLogin()
                //一旦用户的请求没有权限就跳转到这个页面
                .loginPage("/login.html")
                //登录表单form中action的地址，也就是处理认证请求的路径
                .loginProcessingUrl("/login")
                //登录表单form中用户名输入框input的name名，不修改的话默认是username
                .usernameParameter("username")
                //form中密码输入框input的name名，不修改的话默认是password
                .passwordParameter("password")
                //登录认证成功后默认转跳的路径，"/"在spring boot应用里面作为资源访问的时候比较特殊，它就是“/index.html”.所以defaultSuccessUrl登录成功之后就跳转到index.html
                .defaultSuccessUrl("/index.html")
            .and()
                .authorizeRequests()
                // 不需要通过登录验证就可以被访问的资源路径
                .antMatchers("/login.html","/login").permitAll()
                // user角色和admin角色都可以访问
                .antMatchers("/","/biz1","/biz2").hasAnyAuthority("ROLE_user","ROLE_admin")
                // admin角色可以访问，和hasAnyAuthority一样，只是写法不一样，springsecurity会自动补充“ROLE_”
                .antMatchers("/syslog","/sysuser").hasAnyRole("admin")
                // 自定义权限
                //.antMatchers("/syslog").hasAuthority("sys:log")
                //.antMatchers("/sysuser").hasAuthority("sys:user")
                .anyRequest().authenticated();
    }


    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                // 把登录信息保存在内存中
            .inMemoryAuthentication()
                // 添加用户：用户名|密码|角色
                .withUser("user")
                .password(passwordEncoder().encode("123456"))
                .roles("user")
            .and()
                // 添加用户
                .withUser("admin")
                .password(passwordEncoder().encode("123456"))
                //.authorities("sys:log","sys:user")
                .roles("admin")
            .and()
                //配置BCrypt加密
                .passwordEncoder(passwordEncoder());
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

}
