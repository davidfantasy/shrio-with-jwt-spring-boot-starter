package com.github.davidfantasy.jwtshiro;

import com.github.davidfantasy.jwtshiro.annotation.AlowAnonymous;
import com.github.davidfantasy.jwtshiro.annotation.RequiresPerms;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.shiro.web.filter.mgt.DefaultFilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
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

    public static final String BASE_AUTH_PERM_NAME = "base-auth";

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
            String beanPerm = parsePermFromClass(urlBeanClass);
            String methodPerm = parsePermFromMethod(urlMethod);
            String perm = this.concatPerms(beanPerm, methodPerm);
            if (perm == null) {
                continue;
            }
            //替换url中的参数，否则此类型的url不会正确的授权
            Matcher pathVarUrlMatcher = pathVarUrlPattern.matcher(url);
            if (pathVarUrlMatcher.find()) {
                url = pathVarUrlMatcher.replaceAll("**");
            }
            //将映射关系保存后可用于其它需要使用的模块
            PermUtil.addUrlMapping(url, perm);
            if (urlRules.containsKey(url)) {
                logger.warn("{}链接权限重复定义，仅最新的权限设置有效:{}", url, urlRules.get(url));
            }
            if ("anon".equals(perm)) {
                urlRules.put(url, "anon");
            } else {
                urlRules.put(url, "jwtAuthc,jwtPerm[" + perm + "]");
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

    private String parsePermFromClass(Class<?> beanClass) {
        RequiresPerms rp = beanClass.getAnnotation(RequiresPerms.class);
        AlowAnonymous anon = beanClass.getAnnotation(AlowAnonymous.class);
        if (rp != null && anon != null) {
            throw new IllegalStateException("不能同时标注RequiresPerms和AlowAnonymous");
        } else if (rp != null) {
            return rp.value().trim();
        } else if (anon != null) {
            return "";
        } else {
            return null;
        }
    }

    private String parsePermFromMethod(Method urlMethod) {
        RequiresPerms rp = urlMethod.getAnnotation(RequiresPerms.class);
        AlowAnonymous anon = urlMethod.getAnnotation(AlowAnonymous.class);
        if (rp != null && anon != null) {
            throw new IllegalStateException("不能同时标注RequiresPerms和AlowAnonymous");
        } else if (rp != null) {
            return rp.value().trim();
        } else if (anon != null) {
            return "";
        } else {
            return null;
        }
    }

    private String concatPerms(String parentPerm, String childPerm) {
        if (parentPerm == null && childPerm == null) {
            return null;
        } else if ("".equals(parentPerm) && childPerm == null) {
            return "anon";
        } else if ("".equals(childPerm)) {
            return "anon";
        } else {
            String perm = "";
            if (Strings.isNullOrEmpty(parentPerm)) {
                perm = childPerm;
            } else if (Strings.isNullOrEmpty(childPerm)) {
                //默认如果只定义了bean级别的perm, 则自动添加一个权限后缀名，用于定义此链接之下的所有默认权限名，避免直接使用根权限名
                perm = parentPerm + ":" + BASE_AUTH_PERM_NAME;
            } else {
                perm = parentPerm.concat(":").concat(childPerm);
            }
            return perm;
        }
    }

}
