package com.google.refine.exporters.sql.iris.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.refine.exporters.sql.iris.schema.IndexSchema;

public class IndexQueryParser {
	private String indexQuery;
	private IndexSchema indexInfo;
	private Pattern indexQueryPattern;
	
	public IndexQueryParser() {
		final String regex = "\\s*CREATE\\s+(UNIQUE\\s+)?INDEX"
							+ "\\s+(IF\\s+NOT\\sEXISTS\\s)?"
							+ "(\\S+)\\s+"
							+ "ON\\s+(\\S+)\\s+"
							+ "\\((.+)\\)"
							+ "\\s*;?";
		this.indexQueryPattern = Pattern.compile(regex);
	}
	
	public IndexSchema getIndexInfo(){
		return this.indexInfo;
	}
	
	public boolean setQuery(String indexQuery){
		this.indexInfo =  new IndexSchema();
		this.indexQuery = indexQuery.toUpperCase();
		return this.parse();
	}
	
	public boolean parse() {
		Matcher queryMatch = this.indexQueryPattern.matcher(this.indexQuery);
		if(!queryMatch.find()) {
			return false;
		}
	
		if(queryMatch.group(1) == null) {
			this.indexInfo.setTypeIsIndex();
		}
		else {
			this.indexInfo.setTypeIsUnique();
		}
		
		this.indexInfo.setIndexName(queryMatch.group(3).trim());
		this.indexInfo.setTableName(queryMatch.group(4).trim());
		
		String[] columns = queryMatch.group(5).split(",");
		for(String column: columns) {
			column = column.trim();
			String columnOrderBy = "";
			String columnName = "";
			
			if(column.endsWith(" DESC")) {
				columnOrderBy = "D";
				columnName = column.split(" ")[0].trim();
			}
			else if(column.endsWith(" ASC")) {
				columnOrderBy = "A";
				columnName = column.split(" ")[0].trim();
			}
			else {
				columnOrderBy = "A";
				columnName = column;
			}
			this.indexInfo.addColumn(columnName, columnOrderBy);
		}
		
		return true;
	}
}
