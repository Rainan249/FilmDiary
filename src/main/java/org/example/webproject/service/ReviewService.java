package org.example.webproject.service;

import org.example.webproject.dto.ReviewRequest;
import org.example.webproject.entity.Review;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${database.url:jdbc:sqlite:register.db}")
    private String dbUrl;

    @PostConstruct
    public void init() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS reviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    movie_id INTEGER NOT NULL UNIQUE,
                    title TEXT NOT NULL,
                    poster_path TEXT,
                    tmdb_rating REAL,
                    release_date TEXT,
                    user_rating INTEGER,
                    content TEXT,
                    created_at TEXT NOT NULL,
                    updated_at TEXT NOT NULL
                )
            """;
            stmt.executeUpdate(sql);
            log.info("影评表初始化完成");
        } catch (SQLException e) {
            log.error("创建影评表失败", e);
        }
    }

    public Review getByMovieId(Long movieId) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reviews WHERE movie_id = ?")) {
            stmt.setLong(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("查询影评失败", e);
        }
        return null;
    }

    public List<Review> getAll() {
        List<Review> reviews = new ArrayList<>();
        log.info("查询所有影评，数据库URL: {}", dbUrl);
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reviews ORDER BY updated_at DESC")) {
            while (rs.next()) {
                reviews.add(mapRow(rs));
            }
            log.info("查询到 {} 条影评", reviews.size());
        } catch (SQLException e) {
            log.error("查询影评失败", e);
        }
        return reviews;
    }

    public Review getById(Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM reviews WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("查询影评失败", e);
        }
        return null;
    }

    public Review add(ReviewRequest request) {
        String now = LocalDateTime.now().format(FORMATTER);

        // 检查是否已有该电影的影评
        Review existing = getByMovieId(request.getMovieId());
        if (existing != null) {
            // 已有影评，更新它
            update(existing.getId(), request);
            return getById(existing.getId());
        }

        String insertSql = """
            INSERT INTO reviews (movie_id, title, poster_path, tmdb_rating, release_date, user_rating, content, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, request.getMovieId());
            stmt.setString(2, request.getTitle());
            stmt.setString(3, request.getPosterPath());
            stmt.setObject(4, request.getTmdbRating());
            stmt.setString(5, request.getReleaseDate());
            stmt.setObject(6, request.getUserRating());
            stmt.setString(7, request.getContent());
            stmt.setString(8, now);
            stmt.setString(9, now);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return getById(keys.getLong(1));
            }
        } catch (SQLException e) {
            log.error("添加影评失败", e);
        }
        return null;
    }

    public boolean update(Long id, ReviewRequest request) {
        String now = LocalDateTime.now().format(FORMATTER);
        String updateSql = """
            UPDATE reviews SET user_rating = ?, content = ?, updated_at = ? WHERE id = ?
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setObject(1, request.getUserRating());
            stmt.setString(2, request.getContent());
            stmt.setString(3, now);
            stmt.setLong(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("更新影评失败", e);
        }
        return false;
    }

    public boolean delete(Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM reviews WHERE id = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("删除影评失败", e);
        }
        return false;
    }

    public boolean deleteByMovieId(Long movieId) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM reviews WHERE movie_id = ?")) {
            stmt.setLong(1, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("删除影评失败", e);
        }
        return false;
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setMovieId(rs.getLong("movie_id"));
        review.setTitle(rs.getString("title"));
        review.setPosterPath(rs.getString("poster_path"));
        review.setTmdbRating(rs.getDouble("tmdb_rating"));
        review.setReleaseDate(rs.getString("release_date"));
        review.setUserRating(rs.getInt("user_rating"));
        review.setContent(rs.getString("content"));
        review.setCreatedAt(rs.getString("created_at"));
        review.setUpdatedAt(rs.getString("updated_at"));
        return review;
    }
}
