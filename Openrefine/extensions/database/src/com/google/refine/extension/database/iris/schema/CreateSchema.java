package com.mobigen.iris.jdbc.schema;

import java.util.ArrayList;
import java.util.List;

class ColumnDefine {
	String columnName;
	String columnType;
	boolean isNotNull;
	boolean isDefault;
	String defaultValue;
	boolean isAutoIncrement;
	
	public ColumnDefine() {
		this.columnName = "";
		this.columnType = "";
		this.isNotNull = false;
		this.isDefault = false;
		this.isAutoIncrement = false;
	}
}

public class CreateSchema {
	private String tableName;
	private List<ColumnDefine> column = new ArrayList<ColumnDefine>();
	
	private IndexSchema primaryKey = null;
	private List<IndexSchema> uniqueKey = new ArrayList<IndexSchema>();
	
	public String getTableName(){
		return this.tableName;
	}
	
	public int getColumnCount(){
		return this.column.size();
	}
	
	public String getColumnName(int index){
		return this.column.get(index).columnName;
	}
	
	public String getColumnType(int index){
		return this.column.get(index).columnType;
	}
	
	public boolean isNotNull(int index) {
		return this.column.get(index).isNotNull;
	}
	
	public boolean isAutoIncrement(int index) {
		return this.column.get(index).isAutoIncrement;
	}
	
	public boolean isDefault(int index) {
		return this.column.get(index).isDefault;
	}
	
	public String getDefaultValue(int index) {
		return this.column.get(index).defaultValue;
	}
	
	public int getUniqueKeyCount() {
		return this.uniqueKey.size();
	}
	
	public IndexSchema getUniqueKey(int index) {
		return this.uniqueKey.get(index);
	}
	
	public IndexSchema getPrimaryKey() {
		return this.primaryKey;
	}

	public CreateSchema() {
	}
	
	public CreateSchema(String tableName) {
		this.tableName = tableName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void addColumn(String columnName, String columnType, boolean isNotNull, boolean isDefault, String defaultValue) {
		ColumnDefine column = new ColumnDefine();
		column.columnName = columnName;
		column.columnType = columnType;
		column.isNotNull = isNotNull;
		column.isDefault = isDefault;
		column.defaultValue = defaultValue;
		this.column.add(column);
	}
	
	public void setPrimaryKey(IndexSchema primaryKey) {
		this.primaryKey = primaryKey;
	}
	
	public void addUniqueKey(IndexSchema uniquekKey) {
		this.uniqueKey.add(uniquekKey);
	}
}
