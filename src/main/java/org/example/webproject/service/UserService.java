package org.example.webproject.service;

import org.example.webproject.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.*;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Value("${database.url:jdbc:sqlite:register.db}")
    private String dbUrl;

    @PostConstruct
    public void init() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                )
            """;
            stmt.executeUpdate(sql);

            // 检查是否没有任何用户，自动创建默认用户
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO users (username, password) VALUES (?, ?)"
                );
                insert.setString(1, "root");
                insert.setString(2, "123456");
                insert.executeUpdate();
                log.info("已创建默认用户: root/123456，请登录后修改密码");
            }
        } catch (SQLException e) {
            log.error("数据库初始化失败", e);
        }
    }

    public LoginResponse login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return LoginResponse.failure("请输入账号和密码");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ? AND password = ?"
             )) {
            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return LoginResponse.success(username.trim());
            } else {
                return LoginResponse.failure("账号或密码错误");
            }
        } catch (SQLException e) {
            log.error("登录查询失败", e);
            return LoginResponse.failure("系统错误，请稍后重试");
        }
    }
}
