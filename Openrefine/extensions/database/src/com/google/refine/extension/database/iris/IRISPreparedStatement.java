package com.mobigen.iris.jdbc;

/**
 * IRISPreparedStatement.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISPreparedStatement class 생성
 *
 * @author jinho Park
 */

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

public class IRISPreparedStatement extends IRISStatement implements PreparedStatement {

    private String sql = null;
    private String[] parameterList = null;
    private int parameterCount = 0;
    private String replaceKeyword = new String("?");
    //private String replaceKeywordRegex = new String("\\?");

    protected IRISPreparedStatement(IRISConnection conn, String sql) throws SQLException {
        super(conn);
        this.sql = sql;
        int index = 0;
        while ((index = this.sql.indexOf(this.replaceKeyword, index)) > 0) {
            this.parameterCount++;
            index += this.replaceKeyword.length();
        }

        this.parameterList = new String[this.parameterCount];
        //System.out.println("Parameter Count = " + this.parameterCount);
    }

    /**
     * @return mapped query
     * @exception SQLException
     */
    private String binder() throws SQLException {
        if (this.sql.endsWith("?")) {
            this.sql += " ";
        }

        StringTokenizer st = new StringTokenizer(this.sql, "?");

        int paramIndex = 0;
        int paramLen = this.parameterList.length;
        String q = "";

        q = st.nextToken();

        while (st.hasMoreTokens()) {
            if (paramIndex == paramLen) {
                //Exception : # of ? is larger than # of binding
                throw new SQLException("Incorrect number of bindings supplied. query[" + this.sql + "]");
            }

            q += this.parameterList[paramIndex++];
            q += st.nextToken();
        }

        if (paramIndex != paramLen) {
            //Exception : # of binding is larger than # of ?
            throw new SQLException("Incorrect number of bindings supplied. query[" + this.sql + "]");
        }

        q = QueryGeneralization.query(q);

        return q;
    }

    /**
     * @return 0 : Success
     * @exception SQLException
     */
    public int executeUpdate() throws SQLException {
        String q = this.binder();

        if (super.irisConn == null)
            throw new SQLException("Statement is closed");

        try {
            // send request
            String sendMsg = String.format("EXECUTE2 %d\r\n%s", q.getBytes("UTF-8").length, q);
            super.irisSock.write(sendMsg);

            // receive response
            super.response = super.irisSock.readline();
        } catch (UnsupportedEncodingException e) {
            throw new SQLException(IRISError.UNSUPPORTED_ENCODING_ERROR.getDesc(),
                    null, IRISError.UNSUPPORTED_ENCODING_ERROR.getCode());
        }

        // parse response message
        super.parseResponse(super.response);

        return 0;
    }

    /**
     * @exception SQLException
     */
    public void addBatch() throws SQLException {
        String q = this.binder();
        super.addBatch(q);
    }

    public ResultSet executeQuery() throws SQLException {
        String q = this.binder();
        if (super.irisConn == null)
            throw new SQLException("Statement is closed");
        try {
            String sendMsg = String.format("EXECUTE2 %d\r\n%s", q.getBytes("UTF-8").length, q);
            super.irisSock.write(sendMsg);
            super.response = super.irisSock.readline();
        } catch (UnsupportedEncodingException e) {
            throw new SQLException(IRISError.UNSUPPORTED_ENCODING_ERROR.getDesc(),
                    null, IRISError.UNSUPPORTED_ENCODING_ERROR.getCode());
        }
        super.parseResponse(super.response);

        IRISResultSet rs = new IRISResultSet(this);
        return rs;
    }

    /*
     * @return
     * 		true : execute success
     * @throws
     * 		execute fail
     */
    public boolean execute() throws SQLException {
        String q = this.binder();

        if (super.irisConn == null)
            throw new SQLException("Statement is closed");
        try {
            String sendMsg = String.format("EXECUTE2 %d\r\n%s", q.getBytes("UTF-8").length, q);
            super.irisSock.write(sendMsg);
            super.response = super.irisSock.readline();
        } catch (UnsupportedEncodingException e) {
            throw new SQLException(IRISError.UNSUPPORTED_ENCODING_ERROR.getDesc(),
                    null, IRISError.UNSUPPORTED_ENCODING_ERROR.getCode());
        }
        super.parseResponse(super.response);
        return true;
    }

    public ResultSet getResultSet() throws SQLException {
        IRISResultSet rs = new IRISResultSet(this);
        return rs;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    public void setString(int columnIndex, String value) throws SQLException {
        /*
		 * todo : value에 '가 존재할 경우를 생각하여 임시방편으로 수정해
		 * 실제 값에 ' 가 존재하는 경우를 생각해야 함.
		 */
        value = value.replace("'", "''");
        this.parameterList[columnIndex - 1] = new String("'" + value + "'");
    }

    public void setDouble(int columnIndex, double value) throws SQLException {
        this.parameterList[columnIndex - 1] = new String(Double.toString(value));
    }

    public void setFloat(int columnIndex, float value) throws SQLException {
        this.parameterList[columnIndex - 1] = new String(Float.toString(value));
    }

    public void setLong(int columnIndex, long value) throws SQLException {
        this.parameterList[columnIndex - 1] = new String(Long.toString(value));
    }

    public void setInt(int columnIndex, int value) throws SQLException {
        this.parameterList[columnIndex - 1] = new String(Integer.toString(value));
    }

    public void setShort(int columnIndex, short value) throws SQLException {
        this.parameterList[columnIndex - 1] = new String(Short.toString(value));
    }

    public void setNull(int columnIndex, int value) throws SQLException {
        this.setNull(columnIndex, value, "");
    }

    public void setNull(int columnIndex, int value, String arg3) throws SQLException {
        this.parameterList[columnIndex - 1] = new String("NULL");
    }

    public void setBoolean(int columnIndex, boolean value) throws SQLException {
        if (value) {
            this.parameterList[columnIndex - 1] = new String("'true'");
        } else {
            this.parameterList[columnIndex - 1] = new String("'false'");
        }
    }

    public void setObject(int parameterIndex, java.lang.Object parameterObj) throws SQLException {
        if (parameterObj instanceof String) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.VARCHAR);
        } else if (parameterObj instanceof Double) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.DOUBLE);
        } else if (parameterObj instanceof Float) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.FLOAT);
        } else if (parameterObj instanceof Long) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.BIGINT);
        } else if (parameterObj instanceof Integer) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.INTEGER);
        } else if (parameterObj instanceof Short) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.SMALLINT);
        } else if (parameterObj == null) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.NULL);
        } else if (parameterObj instanceof Boolean) {
            this.setObject(parameterIndex, parameterObj, java.sql.Types.BOOLEAN);
        } else
            throw new SQLException("not supported type" + parameterObj);
    }

    public void setObject(int parameterIndex, java.lang.Object parameterObj, int targetSqlType) throws SQLException {
        setObject(parameterIndex, parameterObj, targetSqlType, 0);
    }

    public String objectToString(java.lang.Object object) throws SQLException {
        String result = null;
        if (object instanceof String)
            result = (String) object;
        else if (object instanceof Boolean)
            result = Boolean.toString((Boolean) object);
        else if (object instanceof Short)
            result = Short.toString((Short) object);
        else if (object instanceof Integer)
            result = Integer.toString((Integer) object);
        else if (object instanceof Long)
            result = Long.toString((Long) object);
        else if (object instanceof Float)
            result = Float.toString((Float) object);
        else if (object instanceof Double)
            result = Double.toString((Double) object);
        else if (object == null)
            result = "";
        else
            throw new SQLException("No conversion from " + object.getClass().getName());

        return result;
    }

    public void setObject(int parameterIndex, java.lang.Object parameterObj, int targetSqlType, int scale) throws SQLException {
        if (targetSqlType != java.sql.Types.BOOLEAN)
            parameterObj = objectToString(parameterObj);

        switch (targetSqlType) {
            case java.sql.Types.VARCHAR:
            case java.sql.Types.CHAR:
            case java.sql.Types.LONGVARCHAR:
                setString(parameterIndex, (String) parameterObj);
                break;
            case java.sql.Types.DOUBLE:
                setDouble(parameterIndex, Double.valueOf((String) parameterObj));
                break;
            case java.sql.Types.FLOAT:
                setFloat(parameterIndex, Float.valueOf((String) parameterObj));
                break;
            case java.sql.Types.INTEGER:
                setInt(parameterIndex, Integer.valueOf((String) parameterObj));
                break;
            case java.sql.Types.NULL:
                setNull(parameterIndex, 0);
                break;
            case java.sql.Types.SMALLINT:
                setShort(parameterIndex, Short.valueOf((String) parameterObj));
                break;
            case java.sql.Types.BIGINT:
                setLong(parameterIndex, Long.valueOf((String) parameterObj));
                break;
            case java.sql.Types.BOOLEAN:
                if (parameterObj instanceof String) {
                    setBoolean(parameterIndex, Boolean.valueOf((String) parameterObj));
                } else if (parameterObj instanceof Integer) {
                    setBoolean(parameterIndex, ((Integer) parameterObj == 0) ? false : true);
                } else if (parameterObj instanceof Boolean) {
                    setBoolean(parameterIndex, (Boolean) parameterObj);
                } else {
                    throw new SQLException("No conversion from " + parameterObj.getClass().getName() + " targetSQLType:" + targetSqlType);
                }
                break;
            default:
                throw new SQLException("No conversion from " + parameterObj.getClass().getName() + " targetSQLType:" + targetSqlType);
        }
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return new IRISParameterMetaData(parameterCount);
    }

    public void clearParameters() throws SQLException {
        this.parameterList = new String[this.parameterCount];
    }

    /**
     * @exception SQLException
     *              Method not supported
     */

    public void setBytes(int parameterIndex, byte[] parameterObj) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setURL(int parameterIndex, java.net.URL x) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setTimestamp(int arg1, java.sql.Timestamp arg2, java.util.Calendar arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setTime(int arg1, java.sql.Time arg2, java.util.Calendar arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setDate(int arg1, java.sql.Date arg2, java.util.Calendar arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setArray(int arg1, java.sql.Array arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setClob(int arg1, java.sql.Clob arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBlob(int arg1, java.sql.Blob arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setRef(int arg1, java.sql.Ref arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setCharacterStream(int arg1, java.io.Reader arg2, int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBinaryStream(int arg1, java.io.InputStream arg2, int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setUnicodeStream(int arg1, java.io.InputStream arg2, int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setAsciiStream(int arg1, java.io.InputStream arg2, int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setTimestamp(int arg1, java.sql.Timestamp arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setTime(int arg1, java.sql.Time arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setDate(int arg1, java.sql.Date arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBigDecimal(int arg1, java.math.BigDecimal arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setByte(int arg1, byte arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    // jdk 6.0
    public void setAsciiStream(int arg0, InputStream arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setAsciiStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBinaryStream(int arg0, InputStream arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBinaryStream(int arg0, InputStream arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBlob(int arg0, InputStream arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setBlob(int arg0, InputStream arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setCharacterStream(int arg0, Reader arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setClob(int arg0, Reader arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setClob(int arg0, Reader arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNCharacterStream(int arg0, Reader arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNCharacterStream(int arg0, Reader arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNClob(int arg0, NClob arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNClob(int arg0, Reader arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNClob(int arg0, Reader arg1, long arg2) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setNString(int arg0, String arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setRowId(int arg0, RowId arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public void setSQLXML(int arg0, SQLXML arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }
}