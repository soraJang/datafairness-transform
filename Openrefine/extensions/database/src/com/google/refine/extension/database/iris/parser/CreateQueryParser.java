package com.mobigen.iris.jdbc.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mobigen.iris.jdbc.config.ColumnTypes;
import com.mobigen.iris.jdbc.schema.CreateSchema;
import com.mobigen.iris.jdbc.schema.IndexSchema;

public class CreateQueryParser {
	private String createQuery;
	private CreateSchema createInfo;
	private Pattern createQueryPattern;
	
	
	public CreateQueryParser() {
		final String regex = "\\s*CREATE\\s+TABLE\\s+(\\S+)\\s*"
							+ "\\((.+)\\)"
							+ "\\s*;?";
		this.createQueryPattern = Pattern.compile(regex);
	}
	
	public CreateSchema getCreateInfo() {
		return this.createInfo;
	}

	public boolean setQuery(String indexQuery){
		this.createQuery = indexQuery.toUpperCase();
		this.createInfo = new CreateSchema();
		
		return this.parse();
	}
	
	public boolean parse() {
		Matcher queryMatch = this.createQueryPattern.matcher(this.createQuery);
		if(!queryMatch.find()) {
			return false;
		}
		String tableName = queryMatch.group(1);
		String queryBody = queryMatch.group(2);
		createInfo.setTableName(tableName);
	
		List<String> columnList = new ArrayList<String>();
		

		int sIndex = 0;
		int eIndex = 0;
		while (sIndex < queryBody.length()) {
			int commaIndex = queryBody.indexOf(",", sIndex);
			int bracketIndex = queryBody.indexOf("(", sIndex);
		
			if (bracketIndex == -1 && commaIndex == -1) {
				eIndex = queryBody.length();
			}
			else if(bracketIndex != -1 && commaIndex < bracketIndex) {
				eIndex = commaIndex;
			}
			else if(bracketIndex == -1 && commaIndex >= 0) {
				eIndex = commaIndex;
			}
			else {
				eIndex = queryBody.indexOf(")", sIndex);
				eIndex = queryBody.indexOf(",", eIndex);
			}
			if(eIndex == -1) {
                eIndex = queryBody.length();
			}
			columnList.add(queryBody.substring(sIndex, eIndex).trim());
			sIndex = eIndex+1;
		}
	
		for(String columnStr : columnList) {
			columnStr = columnStr.trim();
		
			//PRIMARY KEY (col1, col2, ...)
			if(columnStr.matches("PRIMARY\\s+KEY\\s*\\(.+")) {
				IndexSchema primaryKey = new IndexSchema();
				primaryKey.setTypeIsPrimary();
				primaryKey.setTableName(tableName);
				primaryKey.setIndexName("PRIMARY");
				sIndex = columnStr.indexOf("(");
				eIndex = columnStr.indexOf(")");
				for(String column: columnStr.substring(sIndex+1, eIndex).split(",")) {
					column = column.trim();
					if(column.indexOf(" ") != -1) {
						String name = column.split(" ")[0].trim();
						String orderby = column.split(" ")[1].trim();
						if(orderby.equals("DESC")) {
							primaryKey.addColumn(name, "D");
						}
						else {
							primaryKey.addColumn(name, "A");
						}
					}
					else {
						primaryKey.addColumn(column, "A");
					}
				}
				createInfo.setPrimaryKey(primaryKey);
			}
			//UNIQUE (col1, col2, ...)
			else if(columnStr.matches("UNIQUE\\s*\\(.+")) {
				IndexSchema uniqueKey = new IndexSchema();
				uniqueKey.setTypeIsUnique();
				uniqueKey.setTableName(tableName);
				uniqueKey.setIndexName("UNIQUE");
				sIndex = columnStr.indexOf("(");
				eIndex = columnStr.indexOf(")");
				for(String column: columnStr.substring(sIndex+1, eIndex).split(",")) {
					column = column.trim();
					if(column.indexOf(" ") != -1) {
						String name = column.split(" ")[0].trim();
						String orderby = column.split(" ")[1].trim();
						if(orderby.equals("DESC")) {
							uniqueKey.addColumn(name, "D");
						}
						else {
							uniqueKey.addColumn(name, "A");
						}
					}
					else {
						uniqueKey.addColumn(column, "A");
					}
				}
				createInfo.addUniqueKey(uniqueKey);
			}
			else {
				String columnName;
				String columnType = "TEXT";
				boolean isAutoincrement = false;
				boolean isNotNull = false;
				boolean isPrimaryKey = false;
				boolean isUnique = false;
				boolean isDefault = false;
				String defaultValue = "";
				String orderby = "";
	
				String []columnStrSplit = columnStr.split(" ");
				columnName = columnStrSplit[0].trim();
				
				if (columnStrSplit.length >= 2) {
					columnType = ColumnTypes.checkName(columnStrSplit[1].trim());
				}
				
				if(columnStr.matches(".+ AUTOINCREMENT.*")) {
					isAutoincrement = true;
				}
				if(columnStr.matches(".+ NOT\\s+NULL.*")) {
					isNotNull = true;
				}
				if(columnStr.matches(".+ PRIMARY\\s+KEY.*")) {
					isPrimaryKey = true;
				}
				if(columnStr.matches(".+ UNIQUE.*")) {
					isUnique = true;
				}
				if(columnStr.matches(".+ DEFAULT\\s+.+")) {
					isDefault = true;
					if(columnStr.matches(".+ DEFAULT\\s+\".+\"")) {
						Pattern r = Pattern.compile(".+ DEFAULT\\s+\"(.+)\"");
						Matcher m = r.matcher(columnStr);
						m.find();
						defaultValue = m.group(1);
					}
					else {
						Pattern r = Pattern.compile(".+ DEFAULT\\s+(\\S+)");
						Matcher m = r.matcher(columnStr);
						m.find();
						defaultValue = m.group(1);
					}
				}
				if(columnStr.matches(".+ ASC.*")) {
					orderby = "A";
				}
				else if(columnStr.matches(".+ DESC.*")) {
					orderby = "D";
				}
				
				/*
				System.out.println(columnStr);
				System.out.println("columnName: " + columnName);
				System.out.println("columnType: " + columnType);
				System.out.println("isNotNull : " + isNotNull);
				System.out.println("isDefault : " + isDefault);
				System.out.println("          : " + defaultValue);
				System.out.println("primary   : " + isPrimaryKey);
				System.out.println("orderby   : " + orderby);
				System.out.println("unique    : " + isUnique);
				System.out.println("autoincry : " + isAutoincrement);
				System.out.println("");
				*/
				createInfo.addColumn(columnName, columnType, isNotNull, isDefault, defaultValue);
				
				if(isPrimaryKey) {
					IndexSchema primaryKey = new IndexSchema();
					primaryKey.setIndexName("PRIMARY");
					primaryKey.setTableName(tableName);
					primaryKey.setTypeIsPrimary();
					if (orderby.equals("D")) {
						primaryKey.addColumn(columnStr.split(" ")[0].trim(), orderby);
					}
					else {
						primaryKey.addColumn(columnStr.split(" ")[0].trim(), "A");
					}
					createInfo.setPrimaryKey(primaryKey);
				}
				else if(isUnique) {
					IndexSchema uniqueKey = new IndexSchema();
					uniqueKey.setIndexName("UNIQUE_" + columnName);
					uniqueKey.setTableName(tableName);
					uniqueKey.setTypeIsUnique();
					if (orderby.equals("D")) {
						uniqueKey.addColumn(columnName, orderby);
					}
					else {
						uniqueKey.addColumn(columnName, "A");
					}
					
					createInfo.addUniqueKey(uniqueKey);
				}
			}
		}
		return true;
	}
}
