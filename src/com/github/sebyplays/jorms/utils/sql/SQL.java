package com.github.sebyplays.jorms.utils.sql;

import com.github.sebyplays.jorms.api.TableBase;
import com.github.sebyplays.jorms.utils.Utilities;
import com.github.sebyplays.jorms.utils.annotations.Column;
import com.github.sebyplays.jorms.utils.annotations.Table;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SQL {
    private String username;
    private String password;
    private String database;
    private String host;
    private int port;

    private final Logger logger;

    private File file;

    public Connection connection;

    public SQL(String username, String password, String database, String host, int port, Logger logger) {
        this.username = username;
        this.password = password;
        this.database = database;
        this.host = host;
        this.port = port;
        this.logger = logger;
    }

    public SQL(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public void connect() {
        if (!isConnected()) {
            try {
                if(file != null){
                    if(!file.exists())
                        file.createNewFile();
                    this.connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
                } else {
                    Class.forName("org.mariadb.jdbc.Driver");
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" +
                            this.database + "?autoReconnect=true", this.username, this.password);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void disconnect() {
        if (isConnected()) {
            try {
                this.connection.close();
            } catch (SQLException throwables) {
            }
        }
    }

    public boolean isConnected() {
        return connection != null;
    }

    public void insertEntry(String tableName, String[] columns, String... values) {
        String vals = "";
        int i = 0;
        for (String column : values) {
            if (i == values.length - 1) {
                vals = vals + escapeSQL(column) + "'";
            } else {
                vals = vals + escapeSQL(column) + "', '";
            }
            i++;
        }

        int i1 = 0;
        String allColumn = "";
        for (String column : columns) {
            if (i1 == columns.length - 1) {
                allColumn = allColumn + escapeSQL(column) + "`";
            } else {
                allColumn = allColumn + escapeSQL(column) + "`, `";
            }
            i1++;
        }
        execU("INSERT INTO `" + escapeSQL(tableName) + "` (`" + allColumn + ") VALUES ('" + vals + ")");
    }

    public void setPrimaryKey(String tableName, String primaryKey) {
        execU("ALTER TABLE `" + escapeSQL(tableName) + "` ADD PRIMARY KEY (`" + escapeSQL(primaryKey) + "`)");
    }
    //remove primary key from table
    public void removePrimaryKey(String tableName) {
        execU("ALTER TABLE `" + escapeSQL(tableName) + "` DROP PRIMARY KEY");
    }

    //get primary key from table
    @SneakyThrows
    public String getPrimaryKey(String tableName) {
        ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM `" + escapeSQL(tableName) + "` LIMIT 1");
        try {
            resultSet.next();
            return resultSet.getMetaData().getColumnName(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    public void insertEntry(TableBase table) {
        if(!table.getClass().isAnnotationPresent(Table.class))
            throw new IllegalArgumentException("Table class must be annotated with @Table");
        String tableName = table.getClass().getAnnotation(Table.class).name();
        String[] columns = new String[table.getClass().getDeclaredFields().length];
        String[] values = new String[table.getClass().getDeclaredFields().length];
        int i = 0;

        for (Field field : table.getColumns(false)) {
            String fieldName = Utilities.getNameOfColumn(field);
            columns[i] = fieldName;
            values[i] = String.valueOf(Utilities.getFieldValue(field, table));
            i++;
        }
        insertEntry(tableName, columns, values);
    }

    public void insertEntry(TableBase table, boolean ifNotExists, boolean throwException) throws IllegalAccessException {
        if(ifNotExists && tableContains(table.getTableName(), table.getColumns(false)[0].getName(),
                (String)Utilities.getFieldValue(table.getColumns(false)[0], table))){
            if(throwException)
                throw new IllegalArgumentException("Entry already exists");
            return;
        }

        this.insertEntry(table);
    }

    @SneakyThrows
    public Object execQ(String query, String columnLabel) {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(query);
        if(logger != null)
            logger.info(query);
        resultSet.next();
        return resultSet.getObject(columnLabel);
    }

    @SneakyThrows
    public void execU(String query) {
        Statement statement = connection.createStatement();
        statement.executeUpdate(query);
        if(logger != null)
            logger.info(query);
        return;
    }

    public boolean tableExists(String tableName) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
            statement.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void createTable(String tableName, String... columns) {
        String allColumn = "";
        for (String column : columns) {
            allColumn = allColumn + column + " MEDIUMTEXT,";
        }
        execU("CREATE TABLE IF NOT EXISTS " + tableName + "(" + allColumn.substring(0, allColumn.lastIndexOf(",") -1)
                + ")");
    }

    public void createTable(String tableName, String[] columns, String... types) {
        String allColumn = "";
        for (int i = 0; i < columns.length; i++) {
            allColumn = allColumn + columns[i] + " " + types[i] + ",";
        }
        execU("CREATE TABLE IF NOT EXISTS " + tableName + "(" + allColumn.substring(0, allColumn.lastIndexOf(",") -1) + ")");
    }

    public void createTable(String tableName, Field[] columns) {
        String allColumn = "";
        for (Field column : columns) {
            String fieldName = Utilities.getNameOfColumn(column);
            allColumn = allColumn + fieldName + " " + column.getAnnotation(Column.class).type().getText() + ", ";
        }
        System.out.println("CREATE TABLE IF NOT EXISTS " + tableName + "(" + allColumn.substring(0, allColumn.lastIndexOf(",") -1) + ")");
        execU("CREATE TABLE IF NOT EXISTS " + tableName + "(" + allColumn.substring(0, allColumn.lastIndexOf(",")) + ")");
    }

    @SneakyThrows
    public boolean tableContains(String tableName, String where, Object value) {
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + tableName + "` WHERE `" + where + "`='" + value + "'");
        return resultSet.next();
    }

    public void createTable(TableBase tableBase) {
        if(!tableBase.getClass().isAnnotationPresent(Table.class))
            throw new IllegalArgumentException("Table class must be annotated with @Table");
        String tableName = tableBase.getClass().getAnnotation(Table.class).name();
        if(tableName.equals("{nameOfClass}"))
            tableName = tableBase.getSubclass().getSimpleName();
        if(tableExists(tableName)) {
            String fieldName;

            //Handling deprecated columns to remove them from the table. Currently, (date: 2021-11-18) this is not supported by sqlite
            for (Field field : tableBase.getDeprecatedFields()){
                fieldName = Utilities.getNameOfColumn(field);
                if (columnExists(tableName, fieldName))
                    removeColumn(tableName, fieldName);
            }


            for (Field field : tableBase.getColumns(false)){
                fieldName = Utilities.getNameOfColumn(field);
                if(field.getAnnotation(Column.class).primaryKey()){
                    if(hasPrimaryKey(tableName) && !getPrimaryKey(tableName).equals(fieldName))
                        removePrimaryKey(tableName);

                    if(!getPrimaryKey(tableName).equals(fieldName))
                        setPrimaryKey(tableName, fieldName);
                }

                if (!columnExists(tableName, fieldName) && !field.isAnnotationPresent(Deprecated.class)) {
                    addColumn(tableName, fieldName, field.getAnnotation(Column.class).type().getText());
                }
            }
            return;
        }

        createTable(tableName, tableBase.getColumns(false));
    }

    //get if table has primary key
    public boolean hasPrimaryKey(String tableName) {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("PRAGMA table_info(" + tableName + ")");
            while (resultSet.next()) {
                if(resultSet.getString("pk") != null)
                    return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public Object getValue(String tableName, String where, Object whereValue, String columnName) {
        return execQ("SELECT `" + columnName + "` FROM `" + tableName + "` WHERE `" + where + "`='" + whereValue
                + "'", columnName);
    }

    public void renameTable(String oldName, String newName) {
        execU("ALTER TABLE `" + oldName + "` RENAME TO `" + newName + "`");
    }

    public void setValue(String tableName, String where, Object whereValue, String columnName, Object updatedValue) {
        execU("UPDATE `" + tableName + "` SET `" + columnName + "`= '" + updatedValue +
                "' WHERE `" + where + "`='" + whereValue + "'");
    }

    public boolean deleteEntry(String tableName, String where, Object whereValue) {
        if (tableContains(tableName, where, whereValue)) {
            execU("DELETE FROM `" + tableName + "` WHERE `" + where + "`='" + whereValue + "'");
            return true;
        }
        return false;
    }

    public ArrayList<String> getTables(){
        ArrayList<String> tables = new ArrayList<>();
        String liteQ = "SELECT name FROM sqlite_master WHERE type='table'";
        String fullQ = "SHOW TABLES";
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(file != null ? liteQ : fullQ);
            while (resultSet.next())
                tables.add(resultSet.getString(1));
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tables;
    }

    public ArrayList<String> getColumns(String tableName) {
        ArrayList<String> columns = new ArrayList<>();
        try {
            Statement statement = this.connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SHOW COLUMNS FROM `" + tableName + "`");
            while (resultSet.next()) {
                columns.add(resultSet.getString(1));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return columns;
    }


    public void renameColumn(String tableName, String oldName, String newName) {
        execU("ALTER TABLE `" + tableName + "` CHANGE `" + oldName + "` `" + newName + "` MEDIUMTEXT");
    }

    public void alterColumnType(String tableName, String columnName, String type) {
        execU("ALTER TABLE `" + tableName + "` MODIFY `" + columnName + "` " + type);
    }

    public boolean columnExists(String tableName, String columnName) {
        try {
            addColumn(tableName, columnName, "MEDIUMTEXT");
        } catch (Exception e) {
            return true;
        }
        removeColumn(tableName, columnName);
        return false;
    }


    public void addColumn(String tableName, String columnName, String columnType){
        execU("ALTER TABLE `" + tableName + "` ADD `" + columnName + "` " + columnType);
    }

    public void removeColumn(String tableName, String columnName){
        execU("ALTER TABLE `" + tableName + "` DROP `" + columnName + "`");
    }

    @SneakyThrows
    public ArrayList getList(String table, String column) {
        ArrayList<String> list = new ArrayList<>();
        Statement statement = this.connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT * FROM `" + table + "`");
        while (resultSet.next()) {
            if (resultSet.getString(column) != (null)) {
                list.add(resultSet.getString(column));
            }
        }
        return list;
    }

    @SneakyThrows
    public String[] getArray(String table, String column) {
        ArrayList<String> list = getList(table, column);
        String[] lArr = new String[list.size()];
        lArr = list.toArray(lArr);
        return lArr;
    }

    public static String escapeSQL(String string){
        String str;
        str = string.replaceAll("'", "");
        str = str.replaceAll("`", "");
        return str;
    }


    public void dropTable(String tableName) {
        execU("DROP TABLE `" + tableName + "`");
    }


}
