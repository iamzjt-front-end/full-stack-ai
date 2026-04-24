package com.waylau.rednote.service.impl;

import com.waylau.rednote.common.ExceptionType;
import com.waylau.rednote.common.Role;
import com.waylau.rednote.dto.UserRegistrationDto;
import com.waylau.rednote.entity.User;
import com.waylau.rednote.repository.UserRepository;
import com.waylau.rednote.service.UserService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostConstruct
    public void initAdminUser() {
        // 查询数据库是否存在管理员用户
        Optional<User> optionalAdminUser = findByUsername(adminUsername);
        User adminUser;
        if (!optionalAdminUser.isPresent()) {
            // 不存在，则创建管理员用户
            adminUser = new User();
            adminUser.setUsername(adminUsername);
        } else {
            // 存在，则获取管理员用户
            adminUser = optionalAdminUser.get();
        }

        // 明文密码加密
        String encodedPassword = passwordEncoder.encode(adminPassword);
        adminUser.setPassword(encodedPassword);

        adminUser.setRole(Role.ADMIN);

        updateUser(adminUser);
    }

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

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }

    @Override
    public Page<User> getAllUsers(int page, int size) {
        // 构造Pageable对象，按照用户ID倒序排序
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("userId").descending());
        return userRepository.findAll(pageable);
    }

    @Override
    public void updateUserByAdmin(User oldUser, User user) {
        // 更新基本信息
        oldUser.setPhone(user.getPhone());

        // 更新密码前先判定是否需要更新
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(user.getPassword());
            oldUser.setPassword(encodedPassword);
        }

        userRepository.save(oldUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}
