package com.google.refine.exporters.sql.iris.config;

public class ColumnTypes {
	public final static int TEXT = java.sql.Types.LONGVARCHAR;
	public final static int REAL = java.sql.Types.REAL;
	public final static int INTEGER = java.sql.Types.INTEGER;
	
	public static String checkName(String t) {
		if(t.equalsIgnoreCase("TEXT")) {
			return "TEXT";
		}
		else if(t.equalsIgnoreCase("REAL")) {
			return "REAL";
		}
		else if(t.equalsIgnoreCase("INT")) {
			return "INTEGER";
		}
		else if(t.equalsIgnoreCase("INTEGER")) {
			return "INTEGER";
		}
		else {
			return "TEXT";
		}
	}
	
	public static boolean isText(String t) {
		if(t.equalsIgnoreCase("TEXT")) {
			return true;
		}
		return false;
	}
	
	public static boolean isReal(String t) {
		if(t.equalsIgnoreCase("REAL")) {
			return true;
		}
		return false;
	}
	
	public static boolean isInteger(String t) {
		if(t.equalsIgnoreCase("INTEGER")) {
			return true;
		}
		return false;
	}
	
	public static int toInt(String t){
		if(t.equalsIgnoreCase("TEXT")) {
			return ColumnTypes.TEXT;
		}
		else if(t.equalsIgnoreCase("REAL")) {
			return ColumnTypes.REAL;
		}
		else if(t.equalsIgnoreCase("INT")) {
			return ColumnTypes.INTEGER;
		}
		else if(t.equalsIgnoreCase("INTEGER")) {
			return ColumnTypes.INTEGER;
		}
		else {
			return -99999;
		}
	}
}
