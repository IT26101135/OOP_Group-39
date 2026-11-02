package com.drivingschool.common.config;

import com.drivingschool.auth.interceptor.AccessInterceptor;
import com.drivingschool.auth.interceptor.NavigationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AccessInterceptor accessInterceptor;
    private final NavigationInterceptor navigationInterceptor;

    public WebConfig(
            AccessInterceptor accessInterceptor,
            NavigationInterceptor navigationInterceptor) {

        this.accessInterceptor = accessInterceptor;
        this.navigationInterceptor = navigationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor)
                .addPathPatterns("/**")
                .order(0);

        registry.addInterceptor(navigationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/error"
                )
                .order(1);
    }
}