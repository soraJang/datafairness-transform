package com.google.refine.exporters.sql.iris.schema.struct;

import com.google.refine.exporters.sql.iris.schema.SelectSchema;

public class TableInfo {
    public String tableName;
    public String tableAliace;
    public boolean leaf;
    public SelectSchema selectSchema;
}
