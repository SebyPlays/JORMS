package com.github.sebyplays.jorms.test;

import com.github.sebyplays.jorms.api.TableBase;
import com.github.sebyplays.jorms.utils.ColumnType;
import com.github.sebyplays.jorms.utils.annotations.Column;
import com.github.sebyplays.jorms.utils.annotations.Table;
import com.github.sebyplays.jorms.utils.sql.SQL;
import lombok.SneakyThrows;

@Table(name = "HUMANS")
public class Human extends TableBase {

    @Column(type = ColumnType.MYSQL_STRING_CHAR, primaryKey = true)
    private String name;

    @Column(type = ColumnType.MYSQL_NUMERIC_INT)
    private int age;

    public Human() {}

    @SneakyThrows
    public Human(SQL sql, Class<TableBase> cl) {super(sql, cl);}

    public Human(String name, int age) {
        this.name = name;
        this.age = age;
    }



}
