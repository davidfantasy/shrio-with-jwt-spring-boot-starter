package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.shiro.JWTAuthFilter;
import com.github.davidfantasy.jwtshiro.shiro.JWTPermFilter;
import com.github.davidfantasy.jwtshiro.shiro.JWTShiroRealm;
import com.google.common.collect.Maps;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import java.util.Map;

/**
 * TODO:对于存在PathVariable的链接支持还存在问题，例如：/test/{var}
 */
@Configuration
@ConditionalOnBean(JWTUserAuthService.class)
@EnableConfigurationProperties({JWTShiroProperties.class})
public class JWTShiroAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(JWTShiroAutoConfiguration.class);

    @Autowired
    private JWTShiroProperties prop;

    @PostConstruct
    public void afterConstruct() {
        logger.info("开始配置jwt-shiro，当前的默认配置为：" + prop);
        if (prop.getMaxIdleMinute() < prop.getMaxAliveMinute()) {
            throw new IllegalArgumentException("accessToken的maxIdleMinute必须大于maxAliveMinute，请检查配置");
        }
    }

    @Bean("securityManager")
    public DefaultWebSecurityManager getManager(JWTShiroRealm shiroRealm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(shiroRealm);
        /*
         * 关闭shiro自带的session，详情见文档
         * http://shiro.apache.org/session-management.html#SessionManagement-StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);
        return manager;
    }

    @Bean("shiroFilter")
    public ShiroFilterFactoryBean factory(DefaultWebSecurityManager securityManager, JWTUserAuthService userAuthService) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        Map<String, Filter> filters = Maps.newHashMap();
        filters.put("jwtAuthc", new JWTAuthFilter(prop.getHeaderKeyOfToken(), userAuthService));
        filters.put("jwtPerm", new JWTPermFilter(userAuthService));
        factoryBean.setFilters(filters);
        factoryBean.setSecurityManager(securityManager);
        //此处暂时不去设置映射关系，等到ServletContext环境启动后再去刷新
        //factoryBean.setFilterChainDefinitionMap(ruleLoader.getRules());
        return factoryBean;
    }

    @Bean("shiroRealm")
    public JWTShiroRealm shiroRealm(JWTUserAuthService userAuthService, JWTHelper jwtHelper) {
        return new JWTShiroRealm(userAuthService, jwtHelper);
    }

    @Bean
    public FilterRegistrationBean delegatingFilterProxy(AbstractShiroFilter shiroFilter) {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(shiroFilter);
        String[] patterns = prop.getUrlPattern().split(",");
        /*
         * 重要：filter和spring inteceptor的url pattern匹配规则是不一致的，差异在于对于*的
         * 处理，filter使用/*匹配本级及所有子路径；而spring inteceptor/*只能匹配本机目录，匹配子目录需要写成/**
         * 此处对urlPattern做必要的兼容性处理，避免需要配置两个参数
         */
        for (String urlPattern : patterns) {
            if (urlPattern.endsWith("**")) {
                urlPattern = urlPattern.substring(0, urlPattern.length() - 1);
            }
            logger.info("添加shiro filter匹配URL规则：" + urlPattern);
            filterRegistrationBean.addUrlPatterns(urlPattern);
        }
        return filterRegistrationBean;
    }

    @Bean
    public JWTHelper getJWTHelper() {
        return new JWTHelper(prop);
    }

    @Bean
    @ConditionalOnProperty(prefix = JWTShiroProperties.JWT_SHIRO_PREFIX, name = "enable-auto-refresh-token", havingValue = "true")
    public TokenRefreshInterceptor tokenRefreshInterceptor(JWTHelper helper, JWTUserAuthService userLoader) {
        return new TokenRefreshInterceptor(userLoader, helper, prop.getHeaderKeyOfToken());
    }

    @Configuration
    @ConditionalOnProperty(prefix = JWTShiroProperties.JWT_SHIRO_PREFIX, name = "enable-auto-refresh-token", havingValue = "true")
    public static class JWTWebMvcConfigurer implements WebMvcConfigurer {

        @Autowired
        private TokenRefreshInterceptor tokenRefreshInterceptor;

        @Autowired
        private JWTShiroProperties prop;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            InterceptorRegistration reg = registry.addInterceptor(tokenRefreshInterceptor);
            String[] patterns = prop.getUrlPattern().split(",");
            logger.info("启用token自动刷新机制，已注册TokenRefreshInterceptor");
            for (String urlPattern : patterns) {
                logger.info("TokenRefreshInterceptor匹配URL规则：" + urlPattern);
                reg.addPathPatterns(urlPattern);
            }
        }

        @Override
        public void addCorsMappings(CorsRegistry registry) {
            //允许访问header中的与token相关属性
            String[] urls = prop.getUrlPattern().split(",");
            for (String url : urls) {
                registry.addMapping(url).exposedHeaders(prop.getHeaderKeyOfToken());
            }
        }
    }

    /**
     * 必须要等到servletContext初始化完成后才能去获取RequestMappingHandlerMapping
     */
    @Configuration
    @AutoConfigureAfter(WebMvcAutoConfiguration.class)
    public static class FilterRuleLoaderConfigurer {

        private AnnotationFilterRuleLoader ruleLoder;

        @Autowired
        private AbstractShiroFilter shiroFilter;

        @Autowired
        private RequestMappingHandlerMapping handlerMapping;

        @PostConstruct
        public void init() {
            ruleLoder = new AnnotationFilterRuleLoader(shiroFilter, handlerMapping);
            ruleLoder.refreshRuleMapping();
        }
    }

}
