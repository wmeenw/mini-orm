package orm.repository;

import orm.annotation.Column;
import orm.core.EntityMetadata;
import orm.core.OrmException;

import java.lang.reflect.Field;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class EntityManager {
    private final Connection connection;
    private final Map<Class<?>, EntityMetadata> cache = new HashMap<>();

    public EntityManager(Connection connection) {
        this.connection = connection;
    }

    private EntityMetadata getMetadata(Class<?> clazz) {
        if (!cache.containsKey(clazz)) {
            cache.put(clazz, new EntityMetadata(clazz));
        }
        return cache.get(clazz);
    }

    public void createTable(Class<?> clazz) {
        EntityMetadata meta = getMetadata(clazz);
        String tableName = meta.getTableName();
        String idColName = meta.getIdColumnName();
        List<Field> colFields = meta.getColumns();
        List<String> colNames = meta.getColumnNames();

        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (\n");
        sql.append("    ").append(idColName).append(" BIGINT AUTO_INCREMENT PRIMARY KEY,\n");

        for (int i = 0; i < colFields.size(); i++) {
            String colName = colNames.get(i);
            Field field = colFields.get(i);
            String sqlType = toSqlType(field.getType());

            Column col = field.getAnnotation(Column.class);
            String notNull = col.nullable() ? "" : " NOT NULL";
            sql.append("    ").append(colName).append(" ").append(sqlType).append(notNull).append(",\n");
        }

        int lastComma = sql.lastIndexOf(",");
        sql.deleteCharAt(lastComma);
        sql.append("\n)");

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql.toString());
        } catch (SQLException e) {
            throw new OrmException("Ошибка создания таблицы", e);
        }
    }

    public Long save(Object entity) {
        EntityMetadata meta = getMetadata(entity.getClass());
        validateNotNull(entity, meta);
        String tableName = meta.getTableName();
        List<Field> colFields = meta.getColumns();
        List<String> colNames = meta.getColumnNames();
        Field idField = meta.getIdField();

        String columns = String.join(", ", colNames);
        String placeholders = "?,".repeat(colNames.size());
        placeholders = placeholders.substring(0, placeholders.length() - 1);

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        try {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < colFields.size(); i++) {
                Field field = colFields.get(i);
                field.setAccessible(true);

                Object value = field.get(entity);
                if (value instanceof java.time.LocalDate) {
                    value = java.sql.Date.valueOf((java.time.LocalDate) value);
                }
                ps.setObject(i + 1, value);
            }
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                Long id = keys.getLong(1);
                idField.setAccessible(true);
                idField.set(entity, id);
                return id;
            }
            throw new OrmException("Не удалось получить сгенерированный id");

        } catch (SQLException e) {
            throw new OrmException("Ошибка сохранения", e);
        } catch (IllegalAccessException e) {
            throw new OrmException("Ошибка доступа к полю", e);
        }
    }

    public int update(Object entity) {
        EntityMetadata meta = getMetadata(entity.getClass());
        validateNotNull(entity, meta);
        String tableName = meta.getTableName();
        List<Field> colFields = meta.getColumns();
        List<String> colNames = meta.getColumnNames();
        Field idField = meta.getIdField();

        List<String> setParts = new ArrayList<>();
        for (String colName : colNames) {
            setParts.add(colName + " = ?");
        }
        String setClause = String.join(", ", setParts);

        String sql = "UPDATE " + tableName + " SET " + setClause + " WHERE " + meta.getIdColumnName() + " = ?";

        try {
            PreparedStatement ps = connection.prepareStatement(sql);
            for (int i = 0; i < colFields.size(); i++) {
                Field field = colFields.get(i);
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value instanceof LocalDate) {
                    value = java.sql.Date.valueOf((LocalDate) value);
                }
                ps.setObject(i + 1, value);
            }
            idField.setAccessible(true);
            ps.setObject(colFields.size() + 1, idField.get(entity));
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("Ошибка обновления", e);
        } catch (IllegalAccessException e) {
            throw new OrmException("Ошибка доступа к полю", e);
        }
    }

    public int delete(Object entity) {
        EntityMetadata meta = getMetadata(entity.getClass());
        String sql = "DELETE FROM " + meta.getTableName() + " WHERE " + meta.getIdColumnName() + " = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            meta.getIdField().setAccessible(true);
            Long id = (Long) meta.getIdField().get(entity);
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("Ошибка delete", e);
        } catch (IllegalAccessException e) {
            throw new OrmException("Ошибка getId", e);
        }
    }

    public int deleteById(Class<?> clazz, Long id) {
        EntityMetadata meta = getMetadata(clazz);
        String sql = "DELETE FROM " + meta.getTableName() + " WHERE " + meta.getIdColumnName() + " = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new OrmException("Ошибка deleteById", e);
        }
    }

    public void saveAll(List<?> entities) {
        if (entities.isEmpty()) return;

        Class<?> firstClass = entities.get(0).getClass();
        for (Object entity : entities) {
            if (entity == null) {
                throw new OrmException("null элемент в saveAll!");
            }
            if (!entity.getClass().equals(firstClass)) {
                throw new OrmException("Все элементы должн ыбыть одного класса!");
            }
        }

        EntityMetadata meta = getMetadata(entities.get(0).getClass());

        for (Object entity : entities) {
            validateNotNull(entity, meta);
        }

        String tableName = meta.getTableName();
        List<String> colNames = meta.getColumnNames();
        List<Field> colFields = meta.getColumns();

        String columns = String.join(", ", colNames);
        String placeholders = "?,".repeat(colNames.size());
        placeholders = placeholders.substring(0, placeholders.length() - 1);

        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";

        try {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql);

            for (Object entity : entities) {
                for (int i = 0; i < colFields.size(); i++) {
                    Field field = colFields.get(i);
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    if (value instanceof LocalDate) {
                        value = java.sql.Date.valueOf((LocalDate) value);
                    }
                    ps.setObject(i + 1, value);
                }
                ps.addBatch();
            }

            ps.executeBatch();
            connection.commit();

        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new OrmException("Ошибка rollback", ex);
            }
            throw new OrmException("Ошибка saveAll", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                throw new OrmException("Ошибка восстановления autoCommit", ex);
            }
        }
    }

    private String toSqlType(Class<?> javaType) {
        if (javaType == String.class) return "VARCHAR(255)";
        if (javaType == int.class || javaType == Integer.class) return "INT";
        if (javaType == long.class || javaType == Long.class) return "BIGINT";
        if (javaType == double.class || javaType == Double.class) return "DOUBLE";
        if (javaType == boolean.class || javaType == Boolean.class) return "BOOLEAN";
        if (javaType == java.time.LocalDate.class) return "DATE";
        throw new OrmException("Неизвестный тип: " + javaType.getName());
    }

    private <T> T mapRow(ResultSet rs, EntityMetadata meta, Class<T> clazz) {
        try {
            T obj = clazz.getDeclaredConstructor().newInstance();

            Field idField = meta.getIdField();
            idField.setAccessible(true);
            idField.set(obj, rs.getLong(meta.getIdColumnName()));

            List<Field> colFields = meta.getColumns();
            List<String> colNames = meta.getColumnNames();

            for (int i = 0; i < colFields.size(); i++) {
                Field field = colFields.get(i);
                String colName = colNames.get(i);
                field.setAccessible(true);

                Object value;
                if (field.getType() == LocalDate.class) {
                    Date date = rs.getDate(colName);
                    value = date != null ? date.toLocalDate() : null;
                } else {
                    value = rs.getObject(colName);
                }

                field.set(obj, value);
            }

            return obj;

        } catch (Exception e) {
            throw new OrmException("Ошибка маппинга строки в объект", e);
        }
    }

    public <T> Optional<T> findById(Class<T> clazz, Long id) {
        EntityMetadata meta = getMetadata(clazz);

        List<String> allCols = new ArrayList<>();
        allCols.add(meta.getIdColumnName());
        allCols.addAll(meta.getColumnNames());

        String selectCols = String.join(", ", allCols);
        String tableName = meta.getTableName();

        String sql = "SELECT " + selectCols + " FROM " + tableName + " WHERE " + meta.getIdColumnName() + " = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                T obj = mapRow(rs, meta, clazz);
                return Optional.of(obj);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new OrmException("Ошибка findById", e);
        }
    }

    public <T> List<T> findAll(Class<T> clazz) {
        EntityMetadata meta = getMetadata(clazz);

        List<String> allCols = new ArrayList<>();
        allCols.add(meta.getIdColumnName());
        allCols.addAll(meta.getColumnNames());

        String selectCols = String.join(", ", allCols);
        String tableName = meta.getTableName();

        String sql = "SELECT " + selectCols + " FROM " + tableName;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<T> result = new ArrayList<>();

            while (rs.next()) {
                T obj = mapRow(rs, meta, clazz);
                result.add(obj);
            }
            return result;
        } catch (SQLException e) {
            throw new OrmException("Ошибка findByAll", e);
        }
    }

    public <T> List<T> findAllWhere(Class<T> clazz, String fieldName, Object value) {
        EntityMetadata meta = getMetadata(clazz);

        Field foundField = null;
        String foundColName = null;

        for (int i = 0; i < meta.getColumns().size(); i++) {
            if (meta.getColumns().get(i).getName().equals(fieldName)) {
                foundField = meta.getColumns().get(i);
                foundColName = meta.getColumnNames().get(i);
                break;
            }
        }

        if (foundField == null) {
            throw new OrmException("Поле не найдено: " + fieldName);
        }

        List<String> allCols = new ArrayList<>();
        allCols.add(meta.getIdColumnName());
        allCols.addAll(meta.getColumnNames());
        String selectCols = String.join(", ", allCols);

        String sql = "SELECT " + selectCols + " FROM " + meta.getTableName() + " WHERE " + foundColName + " = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, value);
            ResultSet rs = ps.executeQuery();

            List<T> result = new ArrayList<>();
            while (rs.next()) {
                result.add(mapRow(rs, meta, clazz));
            }
            return result;

        } catch (SQLException e) {
            throw new OrmException("Ошибка findAllWhere", e);
        }
    }

    public <T> Optional<T> findOneWhere(Class<T> clazz, String fieldName, Object value) {
        List<T> results = findAllWhere(clazz, fieldName, value);

        if (results.size() > 1) {
            throw new OrmException("Найдено больше одной строки для " + fieldName + " = " + value);
        }

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public long count(Class<?> clazz) {
        EntityMetadata meta = getMetadata(clazz);
        String sql = "SELECT COUNT(*) FROM " + meta.getTableName();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new OrmException("Ошибка count", e);
        }
    }

    public boolean existsById(Class<?> clazz, Long id) {
        EntityMetadata meta = getMetadata(clazz);
        String sql = "SELECT 1 FROM " + meta.getTableName() + " WHERE " + meta.getIdColumnName() + " = ? LIMIT 1";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new OrmException("Ошибка existsById", e);
        }
    }

    private void validateNotNull(Object entity, EntityMetadata meta) {
        List<Field> fields = meta.getColumns();
        List<Boolean> nullableFlags = meta.getColumnNullable();

        for (int i = 0; i < fields.size(); i++) {
            if (!nullableFlags.get(i)) {
                Field field = fields.get(i);
                field.setAccessible(true);
                try {
                    Object value = field.get(entity);
                    if (value == null) {
                        throw new OrmException(
                                String.format("Field '%s' is marked @Column(nullable=false) but value is null",
                                        field.getName())
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new OrmException("Cannot access field " + field.getName(), e);
                }
            }
        }
    }
}