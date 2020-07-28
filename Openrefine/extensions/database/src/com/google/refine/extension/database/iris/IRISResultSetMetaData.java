package com.mobigen.iris.jdbc;

/**
 * IRISResultSetMetaData.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2011-05-16 : IRISResultSetMetaData class 생성
 *
 * @author jinho Park
 */

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.mobigen.iris.jdbc.parser.SelectQueryParser;
import com.mobigen.iris.jdbc.schema.SelectSchema;

public class IRISResultSetMetaData implements ResultSetMetaData {

    private String[] columnNames;
    private String[] columnTypes;
    private int columnCount;
    private SelectQueryParser selectQueryParser = new SelectQueryParser();
    private SelectSchema ss = new SelectSchema();

    IRISResultSetMetaData(String[] columnNames, String[] columnTypes, int columnCount, String query) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.columnCount = columnCount;
        try {
            ss = selectQueryParser.setQuery(query);
        } catch (Exception err) {
        }
    }

    // takes col in [1,x] form, returns in [0,x-1] form
    // from SQLite jdbc implements
    private int checkCol(int col) throws SQLException {
        if (columnNames == null)
            throw new IllegalStateException("IRIS JDBC: inconsistent internal state");
        if (col < 1 || col > columnNames.length)
            throw new SQLException("column " + col + " out of bounds [1," + (columnNames.length) + "]");
        return col - 1;
    }

    /*
     * Whats the number of columns in the ResultSet?
     *
     * @return the number
     * @exception SQLException if a database access error occurs
     */
    public int getColumnCount() throws SQLException {
        return columnCount;
    }

    /*
     * What's a column's name?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the column name
     * @exception SQLException if a database access error occurs
     */
    public String getColumnName(int column) throws SQLException {
        int col = checkCol(column);
        String columnName = columnNames[col];
        if (columnName.indexOf(" AS ") > 0) {
            return columnName.split(" AS ")[1].trim();
        } else
            return columnNames[col];
    }

    /*
     * What's a column's label?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the column label(name)
     * @exception SQLException if a database access error occurs
     */
    public String getColumnLabel(int column) throws SQLException {
        int col = checkCol(column);
        return columnNames[col];
    }

    /*
     * What is a column's SQL Type? (java.sql.Type int)
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the java.sql.Type value
     * @exception SQLException
     * @see java.sql.Types
     */
    public int getColumnType(int column) throws SQLException {
        int col = checkCol(column);
        if (columnTypes[col].equalsIgnoreCase("TEXT")) {
            return java.sql.Types.LONGVARCHAR;
        } else if (columnTypes[col].equalsIgnoreCase("NUMBER")) {
            return java.sql.Types.INTEGER;
        } else if (columnTypes[col].equalsIgnoreCase("INTEGER")) {
            return java.sql.Types.INTEGER;
        } else if (columnTypes[col].equalsIgnoreCase("INT")) {
            return java.sql.Types.INTEGER;
        } else if (columnTypes[col].equalsIgnoreCase("REAL")) {
            return java.sql.Types.REAL;
        } else {
            return -1;
        }
    }

    /*
     * Whats is the column's data source specific type name?
     *
     * @param column the first column is 1, the second is 2, etc.
     * @return the type name
     * @exception SQLException
     */
    public String getColumnTypeName(int column) throws SQLException {
        int col = checkCol(column);
        return columnTypes[col];
    }


    public String getColumnClassName(int column) throws SQLException {
        checkCol(column);
        return "java.lang.Object";
    }

    public String getCatalogName(int column) throws SQLException {
        checkCol(column);
        return "";
    }

    public String getSchemaName(int column) throws SQLException {
        return "";
    }

    public int getColumnDisplaySize(int column) throws SQLException {
        int col = checkCol(column);
        if (columnTypes[col].equalsIgnoreCase("TEXT")) {
            return 65535;
        } else if (columnTypes[col].equalsIgnoreCase("NUMBER")) {
            return 10;
        } else if (columnTypes[col].equalsIgnoreCase("INTEGER")) {
            return 10;
        } else if (columnTypes[col].equalsIgnoreCase("INT")) {
            return 10;
        } else if (columnTypes[col].equalsIgnoreCase("REAL")) {
            return 10;
        } else {
            return 0;
        }
    }

    public int getPrecision(int column) throws SQLException {
        return getColumnDisplaySize(column);
    }

    public int getScale(int column) throws SQLException {
        int col = checkCol(column);
        if (columnTypes[col].equalsIgnoreCase("REAL")) {
            return -1;
        } else {
            return 0;
        }
    }

    public boolean isReadOnly(int column) throws SQLException {
        return true;
    }

    public boolean isWritable(int column) throws SQLException {
        return false;
    }

    public String getTableName(int column) throws SQLException {
        int col = checkCol(column);
        return this.ss.find(columnNames[col]);
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public boolean isDefinitelyWritable(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isSigned(int column) throws SQLException {
        int col = checkCol(column);
        if (columnTypes[col].equals("REAL") || columnTypes[col].equals("INTEGER")) {
            return true;
        } else {
            return false;
        }
    }

    public int isNullable(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isCurrency(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isSearchable(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isCaseSensitive(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isAutoIncrement(int column) throws SQLException {
        throw new SQLException("Method not supported");
    }

    // jdk 1.6
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Method not supported");
    }

    @SuppressWarnings("hiding")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Method not supported");
    }
}
