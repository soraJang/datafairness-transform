package com.google.refine.exporters.sql.iris.schema;

import java.util.ArrayList;
import java.util.List;

public class IndexSchema {
	/*
	 * index type
	 * 0: index
	 * 1: unique
	 * 2: primary
	 */
	private String tableName = "";
	private String indexName = "";
	private int indexType = 0;
	private int columnCount = 0;
	private List<String> columnName = new ArrayList<String>();
	private List<String> columnOrderBy = new ArrayList<String>();

	public String getTableName(){
		return this.tableName;
	}
	
	public String getIndexName(){
		return this.indexName;
	}
	
	public boolean isPrimary() {
		if(this.indexType == 2) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isUnique() {
		if(this.indexType == 1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public boolean isIndex() {
		if(this.indexType == 0) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getColumnCount(){
		return this.columnCount;
	}
	
	public String getColumnName(int index) {
		return this.columnName.get(index);
	}
	
	public String getOrderBy(int index) {
		return this.columnOrderBy.get(index);
	}

	public IndexSchema() {
	}

	public IndexSchema(String tableName, String indexName) {
		this.tableName = tableName;
		this.indexName = indexName;
	}
	
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	
	public void setTypeIsIndex() {
		this.indexType = 0;
	}
	
	public void setTypeIsUnique() {
		this.indexType = 1;
	}
	
	public void setTypeIsPrimary() {
		this.indexType = 2;
	}
	
	public void addColumn(String columnName, String columnOrderBy){
		this.columnName.add(columnName);
		this.columnOrderBy.add(columnOrderBy);
		this.columnCount++;
	}
}