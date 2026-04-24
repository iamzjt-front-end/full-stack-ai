package com.waylau.servlet.interfaces;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waylau.servlet.model.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * UserServlet User Servlet
 *
 * @author <a href="https://waylau.com">Way Lau</a>
 * @version 2025/08/10
 **/
@WebServlet("/users")
public class UserServlet extends HttpServlet {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 设置内容类型为JSON
        response.setContentType("application/json");

        // 设置字符编码
        response.setCharacterEncoding("UTF-8");

        // 构造用户模型对象
        User user = new User(1, "Way Lau");

        // 将对象转为JSON字符串
        String json = objectMapper.writeValueAsString(user);

        // 写出JSON字符串
        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }
}
