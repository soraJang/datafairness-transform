package com.google.refine.exporters.sql.iris;

/**
 * IRISConnection.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISConnection class 생성
 * - 2014-04-02 : IRISDatabaseMetaData를 지원하는 코드 추가
 *
 * @author sungwon Park
 * @author Woo-Choel Kim
 */

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;


public class IRISConnection implements Connection {
    protected int port;
    protected String user;
    protected String databaseName;
    protected String passwd;
    protected String server;
    protected boolean isDirect;
    protected boolean connClosed;
    protected Socket rawSock;
    protected IRISSocket irisSock;

    IRISConnection(String server, int port, String databaseName, String user, String passwd, boolean isDirect)
            throws SQLException {
        this.server = server;
        this.port = port;
        this.databaseName = databaseName;
        this.user = user;
        this.passwd = passwd;
        this.isDirect = isDirect;
        this.connClosed = false;

        createConnect();
    }


    private void createConnect() throws SQLException {
        Socket rawSock;

        try {
            rawSock = new Socket(this.server, this.port);
        } catch (UnknownHostException e) {
            if (this.isDirect) {
                throw new SQLException("IRIS not direct connect to host: " + this.server
                        + " port:" + this.port + " Exception:" + e.toString(), "08xx");
            } else {
                throw new SQLException("IRIS not connect to host: " + this.server
                        + " port:" + this.port + " Exception:" + e.toString(), "08xx");
            }
        } catch (IOException e) {
            throw new SQLException(e);
        }

        IRISSocket irisSock = new IRISSocket(rawSock);

        String response;
        if (this.isDirect) {
            //welcome message NSD
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //get UDM ip,port
            String udm_ip, udm_port;
            irisSock.write("GET\r\n");
            String msg = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }
            String[] splitMsg = msg.split(" ")[1].split(":");
            udm_ip = splitMsg[0];
            udm_port = splitMsg[1];
            try {
                irisSock.write("QUIT\r\n");
                irisSock.readline();
            } catch (Exception e) {
            }
            try {
                irisSock.close();
            } catch (Exception e) {
            }
            try {
                rawSock.close();
            } catch (Exception e) {
            }

            //connect UDM
            try {
                rawSock = new Socket(udm_ip, Integer.parseInt(udm_port));
            } catch (UnknownHostException e) {
                throw new SQLException("IRIS not UDM connect to host: " + this.server
                        + " port:" + this.port + " Exception:" + e.toString(), "08xx");
            } catch (IOException e) {
                throw new SQLException(e);
            }
            irisSock = new IRISSocket(rawSock);

            //welcome message UDM
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //set INFO
            String sendMsg = String.format("%s,%s,%s,%s", this.user, this.passwd, rawSock.getLocalAddress(), IRISDriver.DRIVER_VERSION);
            irisSock.write(String.format("SETINFO %s\r\n", Base64.encodeBytes(sendMsg.getBytes())));

            //welcome message UDM
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //use database
            String cmd = String.format("use %s", this.databaseName);
            irisSock.write(String.format("EXECUTE2 %d\r\n%s", cmd.length(), cmd));
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }
        } else {
            //welcome message LISTNER
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //LOGIN
            String sendMsg = String.format("%s,%s,%s", this.user, this.passwd, IRISDriver.DRIVER_VERSION);
            irisSock.write(String.format("LOGIN %s\r\n", Base64.encodeBytes(sendMsg.getBytes())));

            //welcome message PGD
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //welcome message UDM
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }

            //use database
            String cmd = String.format("use %s", this.databaseName);
            irisSock.write(String.format("EXECUTE2 %d\r\n%s", cmd.length(), cmd));
            response = irisSock.readline();
            if (response == null || response.charAt(0) == '-') {
                throw new SQLException(response, "08xx");
            }
        }

        this.rawSock = rawSock;
        this.irisSock = irisSock;
    }

    public void clearWarnings() throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed", "08xx");
    }

    public void close() throws SQLException {
        if (connClosed) {
            return;
        }

        this.connClosed = true;
        this.user = null;
        this.passwd = null;
        this.server = null;
        this.isDirect = false;

        this.irisSock.close();

        try {
            this.rawSock.close();
        } catch (IOException e) {
        }


    }

    public void commit() throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed", "08xx");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Blob createBlob() throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Clob createClob() throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    /*
	// java 6
	public NClob createNClob() throws SQLException {
		throw new SQLException( "Method not supported");
	}
	 */

    /**
     * @exception SQLException
     *              Method not supported
     */
	/*
	// java 6
	public SQLXML createSQLXML() throws SQLException {
		throw new SQLException( "Method not supported");
	}
	 */
    public Statement createStatement() throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed");

        return new IRISStatement(this);
    }

    public Statement createStatement(int arg0, int arg1) throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed");

        return this.createStatement();
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Statement createStatement(int arg0, int arg1, int arg2)
            throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
        throw new SQLException("Method not supported");
    }

    @Override
    public void setSchema(String schema) throws SQLException {

    }

    @Override
    public String getSchema() throws SQLException {
        return null;
    }

    @Override
    public void abort(Executor executor) throws SQLException {

    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {

    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

    public boolean getAutoCommit() throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
        return true;
    }

    public String getCatalog() throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
        //return this.dbMetaData.getCatalog();
        return null;
    }

    public Properties getClientInfo() throws SQLException {
        return null;
    }

    public String getClientInfo(String arg0) throws SQLException {
        return null;
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public int getHoldability() throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed");

        IRISDatabaseMetaData irisDatabaseMetaData = new IRISDatabaseMetaData(this);
        return irisDatabaseMetaData;

    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public int getTransactionIsolation() throws SQLException {
        return Connection.TRANSACTION_NONE;
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        throw new SQLException("Method not supported");
    }

    public SQLWarning getWarnings() throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
        return null;
    }

    public boolean isClosed() throws SQLException {
        return this.connClosed;
    }

    public boolean isReadOnly() throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
        return false;
    }

    public boolean isValid(int timeout) throws SQLException {
        if (timeout < 0)
            throw new SQLException("Invalid timeout: " + timeout);
        if (this.connClosed)
            return false;
        return true;
    }

    public String nativeSQL(String sql) throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
        return sql;
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public CallableStatement prepareCall(String arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public CallableStatement prepareCall(String arg0, int arg1, int arg2)
            throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public CallableStatement prepareCall(String arg0, int arg1, int arg2,
                                         int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed");

        return new IRISPreparedStatement(this, sql);
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String arg0, int arg1)
            throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String arg0, int[] arg1)
            throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String arg0, String[] arg1)
            throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return this.prepareStatement(sql);
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public PreparedStatement prepareStatement(String arg0, int arg1, int arg2,
                                              int arg3) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void releaseSavepoint(Savepoint arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void rollback() throws SQLException {
        //throw new SQLException( "Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void rollback(Savepoint arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setAutoCommit(boolean arg0) throws SQLException {
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setCatalog(String arg0) throws SQLException {
        if (this.connClosed)
            throw new SQLException("Connection is closed");
    }

    public void setClientInfo(Properties arg0) {
    }

    public void setClientInfo(String arg0, String arg1) {
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setHoldability(int arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setReadOnly(boolean arg0) throws SQLException {
        if (connClosed)
            throw new SQLException("Connection is closed");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Savepoint setSavepoint() throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public Savepoint setSavepoint(String arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setTransactionIsolation(int arg0) throws SQLException {
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    public boolean isWrapperFor(Class<?> arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    /**
     * @exception SQLException
     *              Method not supported
     */
    @SuppressWarnings("hiding")
    public <T> T unwrap(Class<T> arg0) throws SQLException {
        throw new SQLException("Method not supported");
    }

    public NClob createNClob() throws SQLException {
        return null;
    }

    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    /* SUPPORT for IRISDatabaseMetaData */
    String libversion() {
        return VersionInfo.IRISLibVersion;
    }

    String getDriverVersion() {
        return VersionInfo.driverVersion;
    }

    String url() {
        return server;
    }
}
