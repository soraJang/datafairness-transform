package com.google.refine.exporters.sql.iris;

/**
 * IRISParameterMetaData.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2011-05-23 : IRISParameterMetaData class 생성
 *
 * @author jinho Park
 */

import java.sql.ParameterMetaData;
import java.sql.SQLException;

public class IRISParameterMetaData implements ParameterMetaData {
    int parameterCount = 0;

    IRISParameterMetaData(int parameterCount) {
        this.parameterCount = parameterCount;
    }

    public int getParameterCount() throws SQLException {
        return this.parameterCount;
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public int getParameterMode(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public String getParameterClassName(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public String getParameterTypeName(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public int getParameterType(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public int getScale(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public int getPrecision(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isSigned(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public int isNullable(int arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    @SuppressWarnings("hiding")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Method not supported");
    }
}
