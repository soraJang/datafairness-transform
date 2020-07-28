package com.google.refine.exporters.sql.iris;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryGeneralization {

    public static String query(String sql) {
        /*
		 * Birst - as이후에 "가 나오는 문제를 해결하기 위한 로직
		 * befor) select t as "b" from ...
		 * after) select t as b from ...
		 */
        String[] sqlTokens = sql.split("\\s+[aA][sS]\\s+\"");
        boolean firstRun = true;
        String newSql = new String("");
        if (sqlTokens.length > 1) {
            for (String sqlToken : sqlTokens) {
                if (firstRun) {
                    firstRun = false;
                    newSql = sqlToken;
                    continue;
                }
                String[] sqlTokenSplit = sqlToken.split("\"", 2);
                newSql += " as ";
                newSql += sqlTokenSplit[0];
                newSql += sqlTokenSplit[1];
            }
            sql = newSql;
        }
		
		/*
		 * 마지막에 ;을 자동으로 생성하도록.
		 * 단, dotcommand일 경우는 안함 
		 */
        String tmpSql = sql.toUpperCase().trim();
        if (tmpSql.startsWith("CREATE") ||
                tmpSql.startsWith("INSERT") ||
                tmpSql.startsWith("UPDATE") ||
                tmpSql.startsWith("DELETE") ||
                tmpSql.startsWith("SELECT") ||
                tmpSql.startsWith("DROP") ||
                tmpSql.startsWith("ALTER") ||
                tmpSql.startsWith("/+") ||
                tmpSql.startsWith("/*")) {
            if (!tmpSql.endsWith(";")) {
                sql += ";";
            }
        }

//		/*
//		 * Birst - hint가 쿼리 중간에 나오는 문제 해결
//		 */
//		// befor) select t from /*+ hint.... */ table ...
//		// after) /*+ hint.... */ select t from table ...
//		try {
//			Pattern dataPatt = Pattern.compile("^([\\S\\s]*)(\\/\\*\\+[\\S\\s]*\\*\\/)([\\S\\s]*)$");
//			Matcher m = dataPatt.matcher(sql);
//			if (m.matches()) {
//				String query = m.group(2);
//				if (m.group(1).length() != 0) {
//					query += "\n " + m.group(1);
//				}
//				if (m.group(3).length() != 0) {
//					query += "\n " + m.group(3);
//				}
//				sql = query;
//			}
//		} catch( Exception e) {}

        if (sql.trim().startsWith("show ")) {
            if (!sql.trim().endsWith(" tables"))
                sql = sql.substring(5);

        }
        return sql;
    }
}
