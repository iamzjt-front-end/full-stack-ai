package com.waylau.rednote.service.impl;

import com.waylau.rednote.common.ExceptionType;
import com.waylau.rednote.dto.UserRegistrationDto;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.repository.UserRepository;
import com.waylau.rednote.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * UserServiceImpl 用户服务
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/16
 **/
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.findByPhone(phone).isPresent();
    }

    @Override
    public boolean verifyCode(String phone, String verificationCode) {
        // 实际项目中会验证验证码逻辑。
        // 模拟验证码校验成功。简化处理，仅返回true
        return true;
    }

    @Override
    public void registerUser(UserRegistrationDto registrationDto) {
        // 创建新用户
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPhone(registrationDto.getPhone());

        // 加密密码
        /*user.setPassword(registrationDto.getPassword());*/
        String encodedPassword = passwordEncoder.encode(registrationDto.getPassword());
        user.setPassword(encodedPassword);

        // 保存用户
        userRepository.save(user);
    }

    @Override
    public boolean verifyPassword(String username, String password) {
        boolean isMatch = false;

        // 获取已有加密密码
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String encodedPassword = user.getPassword();

            // 验证密码是否与加密后的密码匹配
            isMatch = passwordEncoder.matches(password, encodedPassword);
        }
        return isMatch;
    }

    @Override
    public User getCurrentUser() {
        // 从安全上下文中获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 获取当前用户名
            String username = authentication.getName();
            // 根据用户名查询用户
            Optional<User> optionalUser = userRepository.findByUsername(username);
            if (optionalUser.isPresent()) {
                return optionalUser.get();
            }
        }

        throw new UsernameNotFoundException(ExceptionType.USERNAME_NOT_FOUND);
    }

    @Override
    public User updateUser(User currentUser) {
        return userRepository.save(currentUser);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(ExceptionType.USERNAME_NOT_FOUND));

        // 加密密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

    @Override
    public Optional<User> findByUserId(Long userId) {
        return userRepository.findByUserId(userId);
    }
}
