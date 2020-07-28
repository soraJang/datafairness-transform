package com.mobigen.iris.jdbc.schema;

import java.util.ArrayList;
import java.util.List;

import com.mobigen.iris.jdbc.schema.struct.ColumnInfo;
import com.mobigen.iris.jdbc.schema.struct.TableInfo;


public class SelectSchema {
	private List<ColumnInfo> columnInfo = new ArrayList<ColumnInfo> ();
	private List<TableInfo> tableInfo = new ArrayList<TableInfo>();

	/*
	 * @param List<{columnName, columnLabel>}
	 */
	public void addColumn(List<String[]> columns) {
		for(String[] column : columns) {
			ColumnInfo ci = new ColumnInfo();
			ci.columnAliace = column[1];
			if (column[0].matches("[a-zA-Z0-9_]+\\.[a-zA-Z0-9_]+")) {
				String[] col = column[0].split("\\.");
				ci.tableName = col[0];
				ci.columnName = col[1];
			} 
			else {
				ci.tableName = "";
				ci.columnName = column[0];
			}
			columnInfo.add(ci);
		}
	}

	public void addTable(List<String[]> tables) {
		for(String[] table: tables) {
			TableInfo ti = new TableInfo();
			ti.tableAliace = table[1];
			if (table[0].indexOf(" FROM ") > 0) {
				ti.leaf = false;
			}
			else {
				ti.leaf = true;
			}
			ti.tableName = table[0];
			tableInfo.add(ti);
		}
	}
	
	public List<ColumnInfo> getColumnInfo() {
		return this.columnInfo;
	}
	
	public List<TableInfo> getTableInfo() {
		return this.tableInfo;
	}
	public String find(String columnName) {
		columnName = columnName.toUpperCase();
	
		for (ColumnInfo ci : columnInfo) {
			if (ci.columnAliace.equals(columnName)) {
				if (tableInfo.size() == 1 && tableInfo.get(0).leaf == true) {
					if (ci.tableName.equals("")){
						return tableInfo.get(0).tableName;
					}
					else if (ci.tableName.equals(tableInfo.get(0).tableName)) {
						return tableInfo.get(0).tableName;
					}
				}
				else {
				    for (TableInfo ti : tableInfo) {
				   	    if (ti.tableAliace.equals(ci.tableName)) {
						    if (ti.leaf == true) {
							    return ti.tableName;
						    }
						    else {
						    	return ti.selectSchema.find(ci.columnName);
						    }
				   	    }
					}
				}
				return "";
			}
		}
		return "";
	}
}
