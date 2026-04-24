package com.waylau.spring.jdbc;

import com.waylau.spring.jdbc.config.AppConfig;
import com.waylau.spring.jdbc.dao.UserDao;
import com.waylau.spring.jdbc.entity.User;
import com.waylau.spring.jdbc.service.TxService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

/**
 * JdbcTemplate访问数据库的示例
 */
public class App {
    public static void main(String[] args) {
        // 创建Spring应用的上下文
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // 获取bean
        TxService txService = context.getBean("txRequiredServiceImpl", TxService.class);
        //TxService txService = context.getBean("txRequiresNewServiceImpl", TxService.class);
        //TxService txService = context.getBean("txNestedServiceImpl", TxService.class);

        try {
            txService.outer();
        } catch (Exception e) {
            System.out.println("Exception " + e.toString());
        }

        // 查询数据库中的数据
        txService.printAllUsers();
    }
}
