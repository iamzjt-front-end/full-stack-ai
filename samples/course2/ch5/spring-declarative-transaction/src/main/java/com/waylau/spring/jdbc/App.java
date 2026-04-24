package com.waylau.spring.jdbc;

import com.waylau.spring.jdbc.config.AppConfig;
import com.waylau.spring.jdbc.dao.UserDao;
import com.waylau.spring.jdbc.entity.User;
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

        // 获取UserDao bean
        UserDao userDao = context.getBean(UserDao.class);

        // 创建新用户
        User newUser = new User("Charlie", "charlie@waylau.com");
        User saveUser = null;

        try {
            saveUser = userDao.create(newUser);
        } catch (Exception e) {
            System.out.println("Exception " + e.toString());
        }

        System.out.println("Created user: " + saveUser);

        // 查询所有用户
        System.out.println("\nAll users: ");
        List<User> users = userDao.findAll();
        users.forEach(System.out::println);
    }
}
