package com.github.sebyplays.jorms.api;

import com.github.sebyplays.jorms.utils.Utilities;
import com.github.sebyplays.jorms.utils.annotations.Column;
import com.github.sebyplays.jorms.utils.annotations.Table;
import com.github.sebyplays.jorms.utils.sql.SQL;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class TableBase {

    @Getter private SQL sql;
    @Getter private Class subclass;
    @Getter private ArrayList<Row> rows = new ArrayList<>();

    public TableBase(SQL sql, Class cl) {
        this.sql = sql;
        this.subclass = cl;
        loadRowsToMemory();
    }

    public TableBase(){}

    public void loadRowsToMemory(){
        Field[] fields = getColumns(false);
        String fieldName = fields[0].isAnnotationPresent(Column.class) && fields[0].getAnnotation(Column.class)
                .name().equals("{nameOfField}") ? fields[0].getName() : fields[0].getAnnotation(Column.class).name();

        ArrayList<String> rows = this.sql.getList(this.getTableName(), fieldName);
        for(String row : rows)
            this.rows.add(new Row(this.sql, this, true).where(fieldName).equals(row));
    }

    public Field[] getColumns(boolean includeDeprecated){
        ArrayList<Field> fields = new ArrayList<>();
        for(Field field : this.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(Column.class) && (!field.isAnnotationPresent(Deprecated.class) || includeDeprecated)){
                fields.add(field);
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public Field[] getDeprecatedFields(){
        ArrayList<Field> fields = new ArrayList<>();
        for(Field field : this.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(Deprecated.class)){
                fields.add(field);
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    public boolean exists(){
        return sql != null ? sql.tableExists(this.getTableName()) : false;
    }

    public void insertEntry(TableBase tableBase) throws IllegalAccessException {
        insertEntry(tableBase, false);
    }

    public void insertEntry(TableBase tableBase, boolean ifNotExists) throws IllegalAccessException {
        insertEntry(tableBase, ifNotExists, true);
    }

    public void insert(TableBase tableBase) throws IllegalAccessException {
        insertEntry(tableBase, false);
    }

    public void insert(TableBase tableBase, boolean ifNotExists) throws IllegalAccessException {
        insertEntry(tableBase, ifNotExists);
    }

    public void insert(TableBase tableBase, boolean ifNotExists, boolean throwException) throws IllegalAccessException {
        insertEntry(tableBase, ifNotExists, throwException);
    }

    public void insertEntry(TableBase tableBase, boolean ifNotExists, boolean throwException) throws IllegalAccessException {
        if(!exists())
            sql.createTable(tableBase);
        if(tableBase.subclass == null)
            tableBase.subclass = tableBase.getClass();
        this.sql.insertEntry(tableBase, ifNotExists, throwException);
    }

    public String getTableName(){
        return ((Table)Utilities.getAnnotation(this.subclass, Table.class)).name();
    }

    public Row getRow(boolean loadWholeRowToMemory) {
        return new Row(this.sql, this, loadWholeRowToMemory);
    }

    public Row getRow() {
        return new Row(this.sql, this, true);
    }

    public Row getRow(String column, String value){
        return new Row().where(column).equals(value);
    }

    public boolean columnExists(String columnName){
        return this.sql.columnExists(this.getTableName(), columnName);
    }

    public Field getPrimaryKey(){
        for(Field field : this.getColumns(false))
            if(field.getAnnotation(Column.class).primaryKey())
                return field;
        return getColumns(false)[0];
    }

    public void drop() {
        if (exists())
            this.sql.dropTable(this.getTableName());
    }
}
