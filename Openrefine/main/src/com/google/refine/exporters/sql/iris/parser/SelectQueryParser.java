package com.google.refine.exporters.sql.iris.parser;

import java.util.ArrayList;
import java.util.List;

import com.google.refine.exporters.sql.iris.schema.SelectSchema;
import com.google.refine.exporters.sql.iris.schema.struct.ColumnInfo;
import com.google.refine.exporters.sql.iris.schema.struct.TableInfo;

public class SelectQueryParser {
	public SelectQueryParser() {
	}
	
	public SelectSchema setQuery(String selectQuery) {
		return this.parse(selectQuery.toUpperCase());
	}

	public String[] searchSelectFrom(String query) {
		query = query.trim();
		if (query.startsWith("(")) {
			query = query.substring(1, query.length()-1);
		}
		query += " ";
		
		int index = 0;
		int maxQueryIndex = query.length()-1; 
		String[] splitQuery = new String[2];
	
		int selectStartIndex, selectEndIndex;
		int bracketCount;
	
		selectStartIndex = index + 6; 
		selectEndIndex = index + 6;
		bracketCount = 0;
		while (true) {
			if (query.charAt(selectEndIndex) == '(') {
				bracketCount ++;
			}
			else if(query.charAt(selectEndIndex) == ')') {
				bracketCount --;
			}
			else if (bracketCount == 0 && query.substring(selectEndIndex, Math.min(selectEndIndex+6, maxQueryIndex)).equals(" FROM ")) {
				break;
			}
			selectEndIndex++;
			if (selectEndIndex >= maxQueryIndex) {
				return null;
			}
		}
		splitQuery[0] = query.substring(selectStartIndex, selectEndIndex).trim();
		
		int fromStartIndex, fromEndIndex;
		
		fromStartIndex = selectEndIndex + 6;
		fromEndIndex = selectEndIndex;
		bracketCount = 0;
		while (true) {
			if (query.charAt(selectEndIndex) == '(') {
				bracketCount ++;
			}
			else if(query.charAt(selectEndIndex) == ')') {
				bracketCount --;
			}
			else if (bracketCount == 0 && (
				query.substring(fromEndIndex, Math.min(fromEndIndex+7, maxQueryIndex)).equals(" WHERE ")  ||
				query.substring(fromEndIndex, Math.min(fromEndIndex+7, maxQueryIndex)).equals(" GROUP ") ||
				query.substring(fromEndIndex, Math.min(fromEndIndex+8, maxQueryIndex)).equals(" HAVING ") ||
				query.charAt(fromEndIndex) == ';' ||
				fromEndIndex >= maxQueryIndex)) {
				break;
			}
			else if(fromEndIndex >= maxQueryIndex) {
				return null;
			}
			fromEndIndex++;
		}
		splitQuery[1] = query.substring(fromStartIndex, fromEndIndex);
		
		return splitQuery;
	}
	
	public List<String[]> splitSentence(String selectSentence) {
		/*
		 * @param selectsentence: columnString
		 * 		ex) k,p, a
		 * @return: List<columnName, columnLabel>
		 */
		int edIndex = 0;
		int stIndex = 0;
		int bracketCount = 0;
        List<String[]> columnList = new ArrayList<String[]> (); 
        
        selectSentence += " ";
        		
		while (edIndex < selectSentence.length()) {
			if (selectSentence.charAt(edIndex) == '(') {
				bracketCount ++;
			}
			else if (selectSentence.charAt(edIndex) == ')') {
				bracketCount --;
			}
			else if (bracketCount == 0 && 
					(selectSentence.charAt(edIndex) == ',' || 
					(selectSentence.length()-1 == edIndex))) {
				
				String column = selectSentence.substring(stIndex, edIndex).trim();
				String []columnSplit = new String[2];
				if(column.indexOf(' ') >= 0) {
					int i = column.lastIndexOf(' ');
					columnSplit[0] = column.substring(0, i).trim();
					columnSplit[1] = column.substring(i, column.length()).trim();
					if (columnSplit[0].endsWith(" AS"))
						columnSplit[0] = columnSplit[0].substring(0, columnSplit[0].length()-3).trim();
				}
				else {
					columnSplit[0] = column;
					columnSplit[1] = column;
				}
				columnList.add(columnSplit);
				stIndex = edIndex;
				stIndex++;
			}
			edIndex ++;
		}
		return columnList;
	}
	
	public SelectSchema parse(String query) {
		SelectSchema selectSchema = new SelectSchema();
		
		String []splitQuery = this.searchSelectFrom(query);

		selectSchema.addColumn(this.splitSentence(splitQuery[0]));
		selectSchema.addTable(this.splitSentence(splitQuery[1]));
		
		for (TableInfo ti: selectSchema.getTableInfo() ) {
			if (ti.leaf == false) {
				ti.selectSchema = this.parse(ti.tableName);
			}
		}
		
		return selectSchema;
	}
}
