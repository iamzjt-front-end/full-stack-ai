package com.waylau.rednote.usermicroservice.domain.repository;

import com.waylau.rednote.usermicroservice.domain.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * UserRepository 用户资源库
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
public interface UserRepository extends Repository<User, Long> {
    /**
     * 保存用户
     *
     * @param user
     * @return
     */
    User save(User user);

    /**
     * 根据手机号查找用户
     *
     * @param phone
     * @return
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据用户名查找用户
     *
     * @param username
     * @return
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户ID查找用户
     *
     * @param userId
     * @return
     */
    Optional<User> findByUserId(Long userId);

    /**
     * 统计用户数
     *
     * @return
     */
    long count();

    /**
     * 分页查询所有用户
     *
     * @param pageable
     * @return
     */
    Page<User> findAll(Pageable pageable);

    /**
     * 根据用户ID删除用户
     *
     * @param userId
     */
    void deleteById(Long userId);
}
