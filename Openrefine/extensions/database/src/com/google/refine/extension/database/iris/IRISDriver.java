package com.mobigen.iris.jdbc;

/**
 * IMbaseDriver.java
 * For example:
 * <pre>
 *    Connection con = DriverManager.getConnection("jdbc:imbase:myDataSource//IMBASE_IP:IMBASE_PORT", "USERID", "PASSWD");
 * </pre>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISDriver class 생성
 *
 * @author sungwon Park
 */

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class IRISDriver implements Driver {
    static final int DRIVER_MAJOR_VERSION = VersionInfo.driverMajorVersion;
    static final int DRIVER_MINOR_VERSION = VersionInfo.driverMinorVersion;
    static final String DRIVER_NAME = VersionInfo.driverName;
    static final String IMBASE_JDBC_PROTOCOL = "jdbc:iris:";

    static final String DRIVER_VERSION = Integer.toString(DRIVER_MAJOR_VERSION)
            + "." + Integer.toString(DRIVER_MINOR_VERSION);

    static {
        try {
            java.sql.DriverManager.registerDriver(new IRISDriver());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * JDBC 접속
     *
     * @param url
     * @param info
     * @return Connection
     */
    public Connection connect(String url, java.util.Properties info)
            throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        String[] connParams = parseUrl(url);
        String host = connParams[0];
        int port = Integer.parseInt(connParams[1]);
        String databaseName = connParams[2];

        String user = info.getProperty("user");
        String password = info.getProperty("password");

        boolean isDirect = false;

        String directInfo = info.getProperty("direct", "false");
        if (directInfo.toUpperCase().equals("TRUE")) {
            isDirect = true;
        }

        //info.list(System.out);
        IRISConnection conn;
        conn = new IRISConnection(host, port, databaseName, user, password, isDirect);
        return conn;
    }

    public boolean acceptsURL(String url)
            throws SQLException {
        return url.startsWith(IMBASE_JDBC_PROTOCOL);
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
            throws SQLException {
        if (!acceptsURL(url))
            throw new SQLException("Invalid IRIS JDBC protocol");

        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return DRIVER_MAJOR_VERSION;
    }

    public int getMinorVersion() {
        return DRIVER_MINOR_VERSION;
    }

    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    private String[] parseUrl(String url)
            throws SQLException {
        String[] connParams = new String[3];

        String addressPart = url.substring(
                url.indexOf(IMBASE_JDBC_PROTOCOL) + IMBASE_JDBC_PROTOCOL.length());

        if (!addressPart.startsWith("//")) {
            throw new SQLException("Bad URL(" + url + "), Usage(" + IMBASE_JDBC_PROTOCOL + "//ip:port/database)");
        }


        try {
            String tmp = addressPart.substring(2, addressPart.length());
            String[] parts = tmp.split("/");
            String hostPort = parts[0];
            String databaseName = parts[1];

            parts = hostPort.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            connParams[0] = host;
            connParams[1] = Integer.toString(port);
            connParams[2] = databaseName;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new SQLException("Bad URL(" + url + "), Usage(" + IMBASE_JDBC_PROTOCOL + "//ip:port/database)");
        }

        return connParams;
    }

}
