package com.github.sebyplays.jorms.api;

import com.github.sebyplays.jorms.utils.Utilities;

import java.util.Arrays;

public class Where {

    private Row row;

    protected Where(Row row){
        this.row = row;
    }

    public Row equals(String value){
        row.indexValue = value;
        if(row.loadRowToMemory)
            if(row.sql.tableContains(row.tableBase.getTableName(), row.indexName, value))
                Arrays.stream(row.tableBase.getColumns(false)).distinct().forEach(column -> row.data.put(Utilities.getNameOfColumn(column), row.get(Utilities.getNameOfColumn(column))));
        return row;
    }

}
