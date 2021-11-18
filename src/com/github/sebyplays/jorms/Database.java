package com.github.sebyplays.jorms;

import com.github.sebyplays.jorms.api.TableBase;
import com.github.sebyplays.jorms.utils.sql.SQL;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;

public class Database {

    @Getter private String username;
    @Getter private String password;
    @Getter private String database;

    @Getter private String host;
    @Getter private int port;

    @Getter private File file;
    @Getter private Logger logger;

    @Getter private SQL sql;

    public Database(){}
    public Database(File dbFile, Logger logger){
        this.file = dbFile;
        this.logger = logger;
        this.sql = new SQL(dbFile, logger);
    }

    public Database(String username, String password, String database, String host, int port, Logger logger){
        this.database = database;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.logger = logger;
        this.sql = new SQL(username, password, database, host, port, logger);
    }

    public void registerTable(TableBase tableBase){
        this.sql.createTable(tableBase);
    }

    public boolean tableExists(String tableName){
        return this.sql.tableExists(tableName);
    }

    public Database setUsername(String username){
        this.username = username;
        return this;
    }

    @SneakyThrows
    public TableBase getTable(Class tableClass){
        return Class.forName(tableClass.getName()).asSubclass(TableBase.class).getConstructor(SQL.class, Class.class).newInstance(this.sql, tableClass);
    }

    public Database setPassword(String password){
        this.password = password;
        return this;
    }

    public Database setDatabase(String database){
        this.database = database;
        return this;
    }

    public Database setHost(String host){
        this.host = host;
        return this;
    }

    public Database setPort(int port) {
        this.port = port;
        return this;
    }

    public Database setFile(File file){
        this.file = file;
        return this;
    }

    public Database setLogger(Logger logger){
        this.logger = logger;
        return this;
    }

    public void build(){}

    public void connect(){
        this.sql.connect();
    }

    public void disconnect(){
        this.sql.disconnect();
    }

    @SneakyThrows
    public ResultSet executeQuery(String query){
        Statement statement = this.sql.connection.createStatement();
        return statement.executeQuery(query);
    }

    public void executeUpdate(String query){
        this.sql.execU(query);
    }


}
