package com.sky.config;

import com.sky.interceptor.AgentJwtTokenInterceptor;
import com.sky.interceptor.JwtTokenAdminInterceptor;
import com.sky.interceptor.JwtTokenUserInterceptor;
import com.sky.interceptor.JwtTokenAgentInterceptor;
import com.sky.json.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.*;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import java.time.format.DateTimeFormatter;

import java.util.List;

/**
 * 配置类，注册web层相关组件
 */
@Configuration
@Slf4j
public class WebMvcConfiguration extends WebMvcConfigurationSupport {

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;
    
    @Autowired
    private JwtTokenAgentInterceptor jwtTokenAgentInterceptor;
    
    /**
     * 注册自定义拦截器
     *
     * @param registry
     */
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("开始注册自定义拦截器...");
        
        // 管理员接口拦截器
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/employee/login");

        // 普通用户接口拦截器
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/user/**")
                .addPathPatterns("/orders/**")  // 添加统一的订单API路径
                .addPathPatterns("/agent/credit/**")  // 添加代理商信用额度接口，使用用户拦截器处理
                .excludePathPatterns("/user/login")
                .excludePathPatterns("/user/register")
                .excludePathPatterns("/user/shop/status")
                // 微信登录相关接口
                .excludePathPatterns("/user/wechat/qrcode-url")
                // 旅游相关API不需要身份验证
                .excludePathPatterns("/user/day-tours")
                .excludePathPatterns("/user/day-tours/*")
                .excludePathPatterns("/user/day-tours/*/themes")
                .excludePathPatterns("/user/day-tours/*/schedules")
                .excludePathPatterns("/user/group-tours")
                .excludePathPatterns("/user/group-tours/*")
                .excludePathPatterns("/user/tours")
                .excludePathPatterns("/user/tours/*")
                .excludePathPatterns("/user/tours/hot")
                .excludePathPatterns("/user/tours/recommended")
                .excludePathPatterns("/user/tours/search")
                .excludePathPatterns("/user/day-tours/themes")
                .excludePathPatterns("/user/group-tours/themes")
                .excludePathPatterns("/user/tours/suitable-for-options")
                .excludePathPatterns("/regions")
                // 酒店价格和价格计算API也不需要身份验证
                .excludePathPatterns("/user/bookings/hotel-prices")
                //.excludePathPatterns("/user/bookings/tour/calculate-price")  // 注释掉这行，让JWT拦截器处理价格计算API
                // 静态资源和Swagger文档
                .excludePathPatterns("/")
                .excludePathPatterns("/error")
                .excludePathPatterns("/doc.html")
                .excludePathPatterns("/webjars/**")
                .excludePathPatterns("/swagger-resources/**")
                .excludePathPatterns("/v2/api-docs");
                
        // 代理商接口拦截器
        registry.addInterceptor(jwtTokenAgentInterceptor)
                .addPathPatterns("/agent/**")
                .excludePathPatterns("/agent/login")
                .excludePathPatterns("/agent/discount-rate")
                .excludePathPatterns("/agent/calculate-tour-discount")
                .excludePathPatterns("/agent/credit/**");  // 排除credit接口，让用户拦截器处理
    }
    
    /**
     * 添加跨域支持
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("配置跨域支持...");
        registry.addMapping("/**")  // 所有接口
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:3000", "*")  // 明确指定允许的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许所有头
                .allowCredentials(false)  // 允许携带凭证
                .maxAge(3600);  // 预检请求的有效期，单位为秒
    }

    /**
     * 通过knife4j生成接口文档
     * @return
     */
    @Bean
    public Docket docket() {
        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("苍穹外卖项目接口文档")
                .version("2.0")
                .description("苍穹外卖项目接口文档")
                .build();
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.controller"))
                .paths(PathSelectors.any())
                .build()
                .useDefaultResponseMessages(false) // 禁用默认响应消息
                .enableUrlTemplating(false); // 禁用URL模板
        return docket;
    }

    /**
     * 设置静态资源映射
     * @param registry
     */
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }


    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        converters.add(0,converter);
    }

    /**
     * 配置跨域
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        // 初始化cors配置对象
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许所有来源
        configuration.addAllowedOriginPattern("*");
        // 设置不允许携带cookie（当允许所有来源时）
        configuration.setAllowCredentials(false);
        // 设置允许的请求方式
        configuration.addAllowedMethod("*");
        // 设置允许的请求头
        configuration.addAllowedHeader("*");
        
        // 为折扣计算相关接口单独配置跨域
        configuration.addExposedHeader("Access-Control-Allow-Origin");
        configuration.addExposedHeader("Access-Control-Allow-Methods");
        configuration.addExposedHeader("Access-Control-Allow-Headers");
        configuration.addExposedHeader("Access-Control-Allow-Credentials");
        
        // 初始化cors配置源对象
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        configurationSource.registerCorsConfiguration("/**", configuration);
        
        // 返回corsFilter实例
        return new CorsFilter(configurationSource);
    }

    /**
     * 添加格式化器和转换器
     * @param registry
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 配置日期时间格式化
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setDateFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        registrar.setDateTimeFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        registrar.registerFormatters(registry);
    }
}
