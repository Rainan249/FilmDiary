package org.example.webproject.service;

import org.example.webproject.dto.WatchRecordRequest;
import org.example.webproject.entity.WatchRecord;
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
public class WatchRecordService {

    private static final Logger log = LoggerFactory.getLogger(WatchRecordService.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${database.url:jdbc:sqlite:film_db}")
    private String dbUrl;

    private final ReviewService reviewService;

    public WatchRecordService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostConstruct
    public void init() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS watch_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    movie_id INTEGER NOT NULL UNIQUE,
                    title TEXT NOT NULL,
                    poster_path TEXT,
                    tmdb_rating REAL,
                    release_date TEXT,
                    overview TEXT,
                    status TEXT NOT NULL DEFAULT 'wishlist',
                    created_at TEXT NOT NULL
                )
            """;
            stmt.executeUpdate(sql);
            log.info("观影记录表初始化完成");
        } catch (SQLException e) {
            log.error("创建观影记录表失败", e);
        }
    }

    public List<WatchRecord> getAll() {
        List<WatchRecord> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM watch_records ORDER BY created_at DESC")) {
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("查询观影记录失败", e);
        }
        return records;
    }

    public List<WatchRecord> getByStatus(String status) {
        List<WatchRecord> records = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM watch_records WHERE status = ? ORDER BY created_at DESC")) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                records.add(mapRow(rs));
            }
        } catch (SQLException e) {
            log.error("查询观影记录失败", e);
        }
        return records;
    }

    public WatchRecord getById(Long id) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM watch_records WHERE id = ?")) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("查询观影记录失败", e);
        }
        return null;
    }

    public WatchRecord getByMovieId(Long movieId) {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM watch_records WHERE movie_id = ?")) {
            stmt.setLong(1, movieId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            log.error("查询观影记录失败", e);
        }
        return null;
    }

    public WatchRecord add(WatchRecordRequest request) {
        // 检查是否已存在
        WatchRecord existing = getByMovieId(request.getMovieId());
        if (existing != null) {
            // 如果已存在，更新状态
            updateStatus(existing.getId(), request.getStatus());
            return getByMovieId(request.getMovieId());
        }

        String status = request.getStatus() != null ? request.getStatus() : "wishlist";
        String createdAt = LocalDateTime.now().format(FORMATTER);

        String insertSql = """
            INSERT INTO watch_records (movie_id, title, poster_path, tmdb_rating, release_date, overview, status, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, request.getMovieId());
            stmt.setString(2, request.getTitle());
            stmt.setString(3, request.getPosterPath());
            stmt.setObject(4, request.getTmdbRating());
            stmt.setString(5, request.getReleaseDate());
            stmt.setString(6, request.getOverview());
            stmt.setString(7, status);
            stmt.setString(8, createdAt);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return getByMovieId(request.getMovieId());
            }
        } catch (SQLException e) {
            log.error("添加观影记录失败", e);
        }
        return null;
    }

    public boolean updateStatus(Long id, String status) {
        String updateSql = "UPDATE watch_records SET status = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("更新观影记录状态失败", e);
        }
        return false;
    }

    public boolean delete(Long id) {
        // 先获取记录，以便知道 movieId
        WatchRecord record = getById(id);
        if (record != null) {
            // 删除对应的影评
            reviewService.deleteByMovieId(record.getMovieId());
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM watch_records WHERE id = ?")) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            log.error("删除观影记录失败", e);
        }
        return false;
    }

    public int[] getCountByStatus() {
        int watched = 0, wishlist = 0;
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) as cnt FROM watch_records GROUP BY status")) {
            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("cnt");
                if ("watched".equals(status)) watched = count;
                else if ("wishlist".equals(status)) wishlist = count;
            }
        } catch (SQLException e) {
            log.error("统计观影记录失败", e);
        }
        return new int[]{watched, wishlist};
    }

    private WatchRecord mapRow(ResultSet rs) throws SQLException {
        WatchRecord record = new WatchRecord();
        record.setId(rs.getLong("id"));
        record.setMovieId(rs.getLong("movie_id"));
        record.setTitle(rs.getString("title"));
        record.setPosterPath(rs.getString("poster_path"));
        record.setTmdbRating(rs.getDouble("tmdb_rating"));
        record.setReleaseDate(rs.getString("release_date"));
        record.setOverview(rs.getString("overview"));
        record.setStatus(rs.getString("status"));
        record.setCreatedAt(rs.getString("created_at"));
        return record;
    }
}
