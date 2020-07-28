package com.google.refine.exporters.sql.iris;

/**
 * IRISResultSet.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISResultSet class 생성
 * - 2014-04-02 : 불필요한 함수 외부 클래스(IRISUnused)로 분리
 *
 * @author sungwon Park
 * @author Woo-Cheol Kim
 */

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.lang.Integer;
import java.util.Hashtable;


public class IRISResultSet extends IRISUnused implements ResultSet {
    private boolean isFetchEnd;
    private boolean closed;
    private boolean wasNull;
    private List<String[]> result;
    private IRISStatement creator;
    private String[] record;
    private int rowCount = 0;
    private Hashtable<String, Integer> columnNameHash;

    IRISResultSet(Statement creator) throws SQLException {

        this.isFetchEnd = false;
        this.closed = false;
        this.wasNull = false;
        this.creator = (IRISStatement) creator;
        this.result = new LinkedList<String[]>();
        this.columnNameHash = null;
        //initColumnNameHash(this.getMetaData());
    }

    private Hashtable<String, Integer> initColumnNameHash(ResultSetMetaData metadata) throws SQLException {
        Hashtable<String, Integer> nameHash = new Hashtable<String, Integer>();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            nameHash.put(metadata.getColumnName(i), new Integer(i));
        }
        return nameHash;
    }

    public boolean isClosed() throws SQLException {
        return closed;
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        return null;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        return null;
    }

    public void close() throws SQLException {
        closed = true;
        result = null;
    }

    public int findColumn(String columnName) throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");
        if (columnNameHash == null) {
            columnNameHash = initColumnNameHash(getMetaData());
        }
        return columnNameHash.get(columnName).intValue();
    }

    public int getFetchDirection() throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");

        return ResultSet.FETCH_FORWARD;
    }

    public int getFetchSize() throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");
        return 1;
    }

    public int getHoldability() throws SQLException {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    public int getConcurrency() throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");

        return ResultSet.CONCUR_READ_ONLY;
    }

    public String getCursorName() throws SQLException {
        return null;
    }

    public Object getObject(int columnIndex) throws SQLException {
        return record[columnIndex - 1];
    }

    public Object getObject(String columnName) throws SQLException {
        return getObject(findColumn(columnName));
    }

    public String getString(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? null : value;
    }

    public String getString(String columnIndex) throws SQLException {
        return getString(this.findColumn(columnIndex));
    }

    public double getDouble(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? 0 : Double.parseDouble(value);
    }

    public double getDouble(String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }

    public float getFloat(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? 0 : Float.parseFloat(value);
    }

    public float getFloat(String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }

    public long getLong(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? 0 : Long.parseLong(value);
    }

    public long getLong(String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }

    public int getInt(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? 0 : Integer.parseInt(value);
    }

    public int getInt(String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }

    public short getShort(int columnIndex) throws SQLException {
        String value = record[columnIndex - 1];
        return isColumnNull(value) ? 0 : Short.parseShort(value);
    }

    public short getShort(String columnName) throws SQLException {
        return getShort(findColumn(columnName));
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return creator.getMetaData();
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        if (record[columnIndex - 1].equals("true")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return getBoolean(this.findColumn(columnName));
    }

    public Statement getStatement() throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");

        if (this.creator instanceof IRISStatement)
            return (IRISStatement) this.creator;
        else
            return null;
    }


    public int getType() throws SQLException {
        if (closed) {
            throw new SQLException("ResultSet is closed");
        }
        return ResultSet.TYPE_FORWARD_ONLY;
    }

    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    public void clearWarnings() throws SQLException {
    }

    public boolean isAfterLast() throws SQLException {
        throw new SQLException("function not yet implemented for SQLite");
    }

    public boolean isBeforeFirst() throws SQLException {
        throw new SQLException("function not yet implemented for SQLite");
    }

    public boolean isFirst() throws SQLException {
        return rowCount == 1;
    }

    public boolean isLast() throws SQLException {
        if (this.result.size() <= 0) {
            if (!this.isFetchEnd) {
                boolean isEnd = false;
                isEnd = this.creator.getData(this.result);
                if (isEnd) {
                    this.isFetchEnd = isEnd;
                } else {
                    this.rowCount += this.result.size();
                }
            }
        }

        if (this.result.size() > 0) {
            return false;
        }
        return true;
    }

    public int getRow() throws SQLException {
        if (this.result.size() <= 0) {
            if (!this.isFetchEnd) {
                boolean isEnd = false;
                isEnd = this.creator.getData(this.result);
                if (isEnd) {
                    this.isFetchEnd = isEnd;
                } else {
                    this.rowCount += this.result.size();
                }
            }
        }
        return this.rowCount;
    }

    public boolean next() throws SQLException {

        if (this.result.size() <= 0) {
            if (!this.isFetchEnd) {
                boolean isEnd = false;
                isEnd = this.creator.getData(this.result);
                if (isEnd) {
                    this.isFetchEnd = isEnd;
                } else {
                    this.rowCount += this.result.size();
                }
            }
        }
        if (this.result.size() > 0) {
            this.record = this.result.get(0);
            this.result.remove(0);

            //System.out.println(String.format("===> record: %d", this.record.length));
            return true;
        }
        return false;
    }

    public boolean rowDeleted() throws SQLException {
        return false;
    }

    public boolean rowInserted() throws SQLException {
        return false;
    }

    public boolean rowUpdated() throws SQLException {
        return false;
    }

    public void setFetchDirection(int direction) throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");

        if (ResultSet.FETCH_FORWARD != direction)
            throw new SQLException("ResultSet type is TYPE_FORWARD_ONLY");
    }

    public void setFetchSize(int rows) throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");

        if (rows < 0)
            throw new SQLException("Invalid fetch size: " + rows);
    }

    public boolean wasNull() throws SQLException {
        if (closed)
            throw new SQLException("ResultSet is closed");
        return this.wasNull;
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        throw new SQLException("Method not supported");
    }

    @SuppressWarnings("hiding")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Method not supported");
    }

    private boolean isColumnNull(String value) {
        boolean columnNull;
        columnNull = (value.length() == 0);
        if (value.length() >= 0 && value.equals("None")) {
            columnNull = true;
        }
        wasNull = columnNull;
        return columnNull;
    }
}
