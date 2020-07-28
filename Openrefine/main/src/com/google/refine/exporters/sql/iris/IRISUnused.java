package com.google.refine.exporters.sql.iris;

/**
 * IRISUnused.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2014-04-02 : IRISUnused class 생성
 *
 * @author Woo-Cheol Kim
 */

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

class IRISUnused extends SQLiteUnused {

    private SQLException unused() {
        return new SQLException("not implemented by IRIS JDBC driver");
    }

    public BigDecimal getBigDecimal(int col) throws SQLException {
        throw unused();
    }

    public BigDecimal getBigDecimal(String col) throws SQLException {
        throw unused();
    }

    public byte getByte(int col) throws SQLException {
        throw unused();
    }

    public byte getByte(String col) throws SQLException {
        throw unused();
    }

    public byte[] getBytes(int col) throws SQLException {
        throw unused();
    }

    public byte[] getBytes(String col) throws SQLException {
        throw unused();
    }

    public Reader getCharacterStream(int col) throws SQLException {
        throw unused();
    }

    public Reader getCharacterStream(String col) throws SQLException {
        throw unused();
    }

    public Date getDate(int col) throws SQLException {
        throw unused();
    }

    public Date getDate(String col) throws SQLException {
        throw unused();
    }

    public Date getDate(int col, Calendar x) throws SQLException {
        throw unused();
    }

    public Date getDate(String col, Calendar x) throws SQLException {
        throw unused();
    }

    public Time getTime(int col) throws SQLException {
        throw unused();
    }

    public Time getTime(String col) throws SQLException {
        throw unused();
    }

    public Time getTime(int col, Calendar x) throws SQLException {
        throw unused();
    }

    public Time getTime(String col, Calendar x) throws SQLException {
        throw unused();
    }

    public Timestamp getTimestamp(int col) throws SQLException {
        throw unused();
    }

    public Timestamp getTimestamp(String col) throws SQLException {
        throw unused();
    }

    public Timestamp getTimestamp(int col, Calendar x) throws SQLException {
        throw unused();
    }

    public Timestamp getTimestamp(String col, Calendar x) throws SQLException {
        throw unused();
    }

    public Reader getNCharacterStream(int col) throws SQLException {
        throw unused();
    }

    public Reader getNCharacterStream(String col) throws SQLException {
        throw unused();
    }

    public String getNString(int col) throws SQLException {
        throw unused();
    }

    public String getNString(String col) throws SQLException {
        throw unused();
    }

    public void updateAsciiStream(int col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateAsciiStream(String col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateAsciiStream(int col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateAsciiStream(String col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateBinaryStream(int col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateBinaryStream(String col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateBinaryStream(int col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateBinaryStream(String col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateBlob(int col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateBlob(String col, InputStream x) throws SQLException {
        throw unused();
    }

    public void updateBlob(int col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateBlob(String col, InputStream x, long l) throws SQLException {
        throw unused();
    }

    public void updateCharacterStream(int col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateCharacterStream(String col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateCharacterStream(int col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateCharacterStream(String col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateClob(int col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateClob(String col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateClob(int col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateClob(String col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateNClob(int col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateNClob(String col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateNClob(int col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateNClob(String col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateNCharacterStream(int col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateNCharacterStream(String col, Reader x) throws SQLException {
        throw unused();
    }

    public void updateNCharacterStream(int col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateNCharacterStream(String col, Reader x, long l) throws SQLException {
        throw unused();
    }

    public void updateNString(int col, String x) throws SQLException {
        throw unused();
    }

    public void updateNString(String col, String x) throws SQLException {
        throw unused();
    }

    // jdk 1.6
    public NClob getNClob(int col) throws SQLException {
        throw unused();
    }

    public NClob getNClob(String col) throws SQLException {
        throw unused();
    }

    public RowId getRowId(int col) throws SQLException {
        throw unused();
    }

    public RowId getRowId(String col) throws SQLException {
        throw unused();
    }

    public SQLXML getSQLXML(int col) throws SQLException {
        throw unused();
    }

    public SQLXML getSQLXML(String col) throws SQLException {
        throw unused();
    }

    public void updateNClob(int col, NClob x) throws SQLException {
        throw unused();
    }

    public void updateNClob(String col, NClob x) throws SQLException {
        throw unused();
    }

    public void updateRowId(int col, RowId x) throws SQLException {
        throw unused();
    }

    public void updateRowId(String col, RowId x) throws SQLException {
        throw unused();
    }

    public void updateSQLXML(int col, SQLXML x) throws SQLException {
        throw unused();
    }

    public void updateSQLXML(String col, SQLXML x) throws SQLException {
        throw unused();
    }

}
