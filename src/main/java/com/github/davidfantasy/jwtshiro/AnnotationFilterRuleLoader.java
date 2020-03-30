package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import com.github.davidfantasy.jwtshiro.annotation.RequiresPerms;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于注解的方式获取权限规则和URL的映射关系
 */
public class AnnotationFilterRuleLoader {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationFilterRuleLoader.class);

    private AbstractShiroFilter shiroFilter;

    private RequestMappingHandlerMapping handlerMapping;
    
    private static final Pattern pathVarUrlPattern = Pattern.compile("\\{\\w+\\}");

    protected AnnotationFilterRuleLoader(AbstractShiroFilter shiroFilter, RequestMappingHandlerMapping handlerMapping) {
        this.shiroFilter = shiroFilter;
        this.handlerMapping = handlerMapping;
    }

    public void refreshRuleMapping() {
        Map<RequestMappingInfo, HandlerMethod> infos = handlerMapping.getHandlerMethods();
        Map<String, String> urlRules = Maps.newHashMap();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : infos.entrySet()) {
            Method urlMethod = entry.getValue().getMethod();
            Class<?> urlBeanClass = entry.getValue().getBeanType();
            String url = entry.getKey().getPatternsCondition().getPatterns().toString();
            url = url.substring(1, url.length() - 1);
            Logical logical = parseLogical(urlBeanClass, urlMethod);
            List<String> beanPerm = parsePermFromClass(urlBeanClass);
            List<String> methodPerm = parsePermFromMethod(urlMethod);
            List<String> perms = this.concatPerms(beanPerm, methodPerm);
            if (perms == null || perms.isEmpty()) {
                continue;
            }
            //替换url中的参数，否则此类型的url不会正确的授权
            Matcher pathVarUrlMatcher = pathVarUrlPattern.matcher(url);
            if (pathVarUrlMatcher.find()) {
                url = pathVarUrlMatcher.replaceAll("**");
            }
            if (urlRules.containsKey(url)) {
                logger.warn("{}链接权限重复定义，仅最新的权限设置有效:{}", url, urlRules.get(url));
            }
            if (perms.contains("anon")) {
                urlRules.put(url, "anon");
            } else if (logical == Logical.AND) {
                urlRules.put(url, "jwtAuthc,jwtPerms" + perms.toString());
            } else if (logical == Logical.OR) {
                urlRules.put(url, "jwtAuthc,jwtAnyPerms" + perms.toString());
            }
        }
        updateShiroPermissionDefines(urlRules);
    }

    private void updateShiroPermissionDefines(Map<String, String> urlRules) {
        // 获取过滤管理器
        PathMatchingFilterChainResolver filterChainResolver = (PathMatchingFilterChainResolver) shiroFilter
                .getFilterChainResolver();
        DefaultFilterChainManager manager = (DefaultFilterChainManager) filterChainResolver.getFilterChainManager();
        // 清空初始权限配置
        manager.getFilterChains().clear();
        // 重新构建生成
        logger.info("开始设置shiro的链接权限...");
        for (Map.Entry<String, String> entry : urlRules.entrySet()) {
            logger.info(entry.toString());
            String url = entry.getKey();
            String chainDefinition = entry.getValue().trim().replace(" ", "");
            manager.createChain(url, chainDefinition);
        }
        //默认剩余的URL都需要登录才能访问，此项必须最后再进行设置，否则会导致剩余链接的规则失效
        //shiro按顺序进行匹配的，如果URL匹配上了一个规则就导致剩余的规则失效
        manager.createChain("/**", "jwtAuthc");
    }

    private List<String> parsePermFromClass(Class<?> beanClass) {
        RequiresPerms rp = beanClass.getAnnotation(RequiresPerms.class);
        AlowAnonymous anon = beanClass.getAnnotation(AlowAnonymous.class);
        if (rp != null && anon != null) {
            throw new IllegalStateException("不能同时标注RequiresPerms和AlowAnonymous");
        } else if (rp != null) {
            return Arrays.asList(rp.value());
        } else if (anon != null) {
            return new ArrayList<>();
        } else {
            return null;
        }
    }

    private List<String> parsePermFromMethod(Method urlMethod) {
        RequiresPerms rp = urlMethod.getAnnotation(RequiresPerms.class);
        AlowAnonymous anon = urlMethod.getAnnotation(AlowAnonymous.class);
        if (rp != null && anon != null) {
            throw new IllegalStateException("不能同时标注RequiresPerms和AlowAnonymous");
        } else if (rp != null) {
            return Arrays.asList(rp.value());
        } else if (anon != null) {
            return new ArrayList<>();
        } else {
            return null;
        }
    }

    private List<String> concatPerms(List<String> parentPerm, List<String> childPerm) {
        if (parentPerm == null && childPerm == null) {
            return null;
        } else if (parentPerm != null && parentPerm.isEmpty() && childPerm == null) {
            return Lists.newArrayList("anon");
        } else if (childPerm != null && childPerm.isEmpty()) {
            return Lists.newArrayList("anon");
        } else if (childPerm != null) {
            return childPerm;
        } else {
            return parentPerm;
        }
    }

    private Logical parseLogical(Class<?> urlBeanClass, Method urlMethod) {
        RequiresPerms rp = urlMethod.getAnnotation(RequiresPerms.class);
        if (rp == null) {
            rp = urlBeanClass.getAnnotation(RequiresPerms.class);
        }
        if (rp == null) {
            return Logical.AND;
        }
        return rp.logical();
    }

}
