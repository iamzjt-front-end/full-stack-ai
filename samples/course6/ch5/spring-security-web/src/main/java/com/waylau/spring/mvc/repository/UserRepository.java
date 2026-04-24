package com.waylau.spring.mvc.repository;

import com.waylau.spring.mvc.model.User;
import org.springframework.data.repository.CrudRepository;

/**
 * UserRepository 用户资源库
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/13
 **/
public interface UserRepository extends CrudRepository<User, Long> {
}
