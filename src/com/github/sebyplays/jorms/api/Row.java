package com.github.sebyplays.jorms.api;

import com.github.sebyplays.jorms.utils.Utilities;
import com.github.sebyplays.jorms.utils.sql.SQL;
import lombok.Getter;

import java.util.HashMap;

public class Row {

    protected String indexName;
    protected String indexValue;
    protected SQL sql;

    @Getter private Object result;

    @Getter protected HashMap<String, Object> data = new HashMap<>();

    protected boolean loadRowToMemory;

    protected TableBase tableBase;

    protected Row(SQL sql, TableBase tableBase, boolean loadWholeRowToMemory){
        this.sql = sql;
        this.tableBase = tableBase;
        this.loadRowToMemory = loadWholeRowToMemory;
    }

    public Row(){}

    public Where where(String column){
        this.indexName = column;
        return new Where(this);
    }

    public Where where(){
        this.indexName = Utilities.getNameOfColumn(tableBase.getPrimaryKey());
        return new Where(this);
    }

    public Object get(String columnName){
        if(this.indexName != null && this.indexValue != null && !this.data.containsKey(columnName))
            return (this.result = this.sql.getValue(tableBase.getTableName(), this.indexName, this.indexValue, columnName));
        if(this.data.containsKey(columnName))
            return this.data.get(columnName);
        return null;
    }

    public void set(String columnName, Object value){
        if(this.indexName != null && this.indexValue != null)
            this.sql.setValue(tableBase.getTableName(), this.indexName, this.indexValue, columnName, value);
        this.data.put(columnName, value);
    }

    public void drop(){
        if(this.indexName != null && this.indexValue != null && exists())
            this.sql.deleteEntry(tableBase.getTableName(), this.indexName, this.indexValue);
        if(this.data.containsKey(this.indexName))
            this.data.remove(this.indexName);
    }

    public boolean exists(){
        return this.sql.tableContains(tableBase.getTableName(), this.indexName, this.indexValue);
    }

}
