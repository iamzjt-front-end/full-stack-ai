package com.waylau.spring.jdbc.service;

/**
 * TxService 事务服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2026/04/06
 **/
public interface TxService {
    /**
     * 外部处理
     */
    void outer();

    /**
     * 内部处理
     */
    void inner();

    /**
     * 打印用户列表
     */
    void printAllUsers();
}
