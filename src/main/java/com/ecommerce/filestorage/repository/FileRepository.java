package com.ecommerce.filestorage.repository;

import com.ecommerce.filestorage.exceptions.CustomException;
import com.ecommerce.filestorage.model.File;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class FileRepository {
    private final JdbcTemplate jdbcTemplate;

    public FileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<File> fileRowMapper = (rs, rowNum) -> {
        File file = new File();
        file.setId(rs.getString("id"));
        file.setName(rs.getString("name"));
        file.setExtension(rs.getString("extension"));
        file.setSize(rs.getLong("size"));
        file.setData(rs.getBytes("byte"));
        file.setContentType(rs.getString("content_type"));
        file.setCreatedAt(rs.getDate("created_at"));
        file.setUpdatedAt(rs.getDate("updated_at"));
        return file;
    };

    public List<File> findAll() {
        String sql = "SELECT * FROM tb_files";
        return jdbcTemplate.query(sql, fileRowMapper);
    }

    public File findById(String id) {
        String sql = "SELECT * FROM tb_files WHERE id = ?";
        List<File> files = jdbcTemplate.query(sql, fileRowMapper, id);
        return files.isEmpty() ? null : files.getFirst();
    }

    public File save(File file) {
        String generatedId = UUID.randomUUID().toString();
        file.setId(generatedId);

        String sql = "INSERT INTO tb_files " +
                "(id, name, extension, size, byte, content_type, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(
                sql,
                file.getId(),
                file.getName(), //file.getId() + "." + file.getExtension(),
                file.getExtension(),
                file.getSize(),
                file.getData(),
                file.getContentType(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );

        return file;
    }

    public int updateById(String id, File file) {
        String sql = "UPDATE tb_files " +
                "SET name = ?, extension = ?, size = ?, byte = ?, content_type = ?, updated_at = ? " +
                "WHERE id = ?";

        file.setUpdatedAt(new Date());
        return jdbcTemplate.update(
                sql,
                file.getName(), // file.getId() + "." + file.getExtension(),
                file.getExtension(),
                file.getSize(),
                file.getData(),
                file.getContentType(),
                file.getUpdatedAt(),
                id
        );
    }

    public int deleteById(String id) {
        String sql = "DELETE FROM tb_files WHERE id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
