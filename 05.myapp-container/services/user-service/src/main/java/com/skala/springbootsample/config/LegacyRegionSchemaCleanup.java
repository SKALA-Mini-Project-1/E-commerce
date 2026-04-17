package com.skala.springbootsample.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class LegacyRegionSchemaCleanup implements ApplicationRunner {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            String usersTable = resolveTableName(connection, "users");
            if (usersTable == null) {
                return;
            }

            boolean regionColumnExists = columnExists(connection, usersTable, "region_id");
            if (!regionColumnExists) {
                dropTableIfExists("regions");
                return;
            }

            dropForeignKeys(connection, usersTable, "region_id");
            jdbcTemplate.execute("ALTER TABLE " + usersTable + " DROP COLUMN region_id");
            dropTableIfExists("regions");
            log.info("레거시 Region 스키마 정리를 완료했습니다.");
        }
    }

    private void dropForeignKeys(Connection connection, String tableName, String columnName) throws SQLException {
        Set<String> foreignKeys = findForeignKeys(connection, tableName, columnName);
        String databaseProduct = connection.getMetaData().getDatabaseProductName().toLowerCase(Locale.ROOT);

        for (String foreignKey : foreignKeys) {
            if (databaseProduct.contains("mysql") || databaseProduct.contains("mariadb")) {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP FOREIGN KEY " + foreignKey);
            } else {
                jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP CONSTRAINT " + foreignKey);
            }
        }
    }

    private Set<String> findForeignKeys(Connection connection, String tableName, String columnName) throws SQLException {
        Set<String> foreignKeys = new LinkedHashSet<>();
        DatabaseMetaData metaData = connection.getMetaData();

        try (ResultSet resultSet = metaData.getImportedKeys(connection.getCatalog(), null, tableName)) {
            while (resultSet.next()) {
                String fkColumnName = resultSet.getString("FKCOLUMN_NAME");
                if (columnName.equalsIgnoreCase(fkColumnName)) {
                    foreignKeys.add(resultSet.getString("FK_NAME"));
                }
            }
        }

        return foreignKeys;
    }

    private boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, tableName, "%")) {
            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("COLUMN_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String resolveTableName(Connection connection, String targetTableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (targetTableName.equalsIgnoreCase(tableName)) {
                    return tableName;
                }
            }
        }
        return null;
    }

    private void dropTableIfExists(String tableName) {
        jdbcTemplate.execute("DROP TABLE IF EXISTS " + tableName);
    }
}
