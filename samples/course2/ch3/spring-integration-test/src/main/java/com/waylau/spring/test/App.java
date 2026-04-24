package com.waylau.spring.test;

import org.springframework.context.annotation.ComponentScan;

/**
 * Hello world 测试示例
 */
@ComponentScan //自动扫描指定报下的全部标有 @Component 的类及子类，注册成bean
public class App {
    public static void main(String[] args) {
    }
}
