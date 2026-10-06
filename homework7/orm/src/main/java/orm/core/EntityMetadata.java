package orm.core;

import orm.annotation.Column;
import orm.annotation.Id;
import orm.annotation.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityMetadata {
    private final String tableName;
    private final Field idField;
    private final String idColumnName;
    private final List<Field> columns;
    private final List<String> columnNames;
    private final List<Boolean> columnNullable;

    public EntityMetadata(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new OrmException("Класс " + clazz.getName() + " не помечен @Table");
        }
        this.tableName = table.name().isEmpty() ?  clazz.getSimpleName().toLowerCase() : table.name();

        Field foundIdField = null;
        String foundIdColumnName = null;
        List<Field> foundColumns = new ArrayList<>();
        List<String> foundColumnNames = new ArrayList<>();

        for (var field : clazz.getDeclaredFields()){
            if (field.isAnnotationPresent(Id.class)) {
                foundIdField = field;
                if (field.isAnnotationPresent(Column.class)) {
                    Column col = field.getAnnotation(Column.class);
                    foundIdColumnName = col.name().isEmpty() ? field.getName() : col.name();
                } else {
                    foundIdColumnName = field.getName();
                }
            }  else if (field.isAnnotationPresent(Column.class)) {
                Column col = field.getAnnotation(Column.class);
                String colName = col.name().isEmpty() ? field.getName() : col.name();
                foundColumns.add(field);
                foundColumnNames.add(colName);
            }
        }
        if (foundIdField == null) {
            throw new OrmException("Не найдено поле с @Id в классе " + clazz.getName());
        }

        List<Boolean> nullableFlags = new ArrayList<>();
        for (Field field : foundColumns) {
            Column col = field.getAnnotation(Column.class);
            nullableFlags.add(col.nullable());
        }
        this.columnNullable = nullableFlags;
        this.idField = foundIdField;
        this.idColumnName = foundIdColumnName;
        this.columns = foundColumns;
        this.columnNames = foundColumnNames;
    }

    public String getTableName() { return tableName; }
    public Field getIdField() { return idField; }
    public String getIdColumnName() { return idColumnName; }
    public List<Field> getColumns() { return columns; }
    public List<String> getColumnNames() { return columnNames; }
    public List<Boolean> getColumnNullable() {
        return columnNullable;
    }
}
