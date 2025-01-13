package com.xtyu.toolapi.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author zhangyifeng
 * @date 2022/03/24
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class MyBatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor interceptor() {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        //分页插件
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        //禁止全表删除和更新插件
        BlockAttackInnerInterceptor blockAttackInnerInterceptor = new BlockAttackInnerInterceptor();
        ArrayList<InnerInterceptor> list = new ArrayList<>();
        Collections.addAll(list,paginationInnerInterceptor,blockAttackInnerInterceptor);
        mybatisPlusInterceptor.setInterceptors(list);
        return mybatisPlusInterceptor;
    }

    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.setUseDeprecatedExecutor(false);
    }

//    @Bean
//    public ConfigurationCustomizer mybatisConfigurationCustomizer() {
//        return configuration -> {
//            // 注册自定义 TypeHandler
//            configuration.getTypeHandlerRegistry().register(LocalDateTime.class, LongDataTimeHandler.class);
//        };
//    }
}
