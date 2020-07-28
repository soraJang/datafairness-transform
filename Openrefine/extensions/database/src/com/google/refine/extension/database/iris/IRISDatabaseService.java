package com.google.refine.extension.database.iris;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.refine.extension.database.DatabaseConfiguration;
import com.google.refine.extension.database.DatabaseService;
import com.google.refine.extension.database.DatabaseServiceException;
import com.google.refine.extension.database.DatabaseUtils;
import com.google.refine.extension.database.SQLType;
import com.google.refine.extension.database.model.DatabaseColumn;
import com.google.refine.extension.database.model.DatabaseInfo;
import com.google.refine.extension.database.model.DatabaseRow;

public class IRISDatabaseService extends DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger("IRISDatabaseService");
    public static final String DB_NAME = "iris";
    public static final String DB_DRIVER = "jdbc:iris:";
    private static IRISDatabaseService instance;
    //private Connection irisConn = null;

    private IRISDatabaseService() {
    }

    public static IRISDatabaseService getInstance() {
        if (instance == null) {
            SQLType.registerSQLDriver(DB_NAME, DB_DRIVER);
            instance = new IRISDatabaseService();
            if(logger.isDebugEnabled()) {
                logger.debug("IRISDatabaseService Instance: {}", instance);
            }
        }
        return instance;
    }

    @Override
    public boolean testConnection(DatabaseConfiguration dbConfig) throws DatabaseServiceException{
        return true;
    }

    @Override
    public DatabaseInfo connect(DatabaseConfiguration dbConfig) throws DatabaseServiceException{
        return getMetadata(dbConfig);
    }

    
    /**
     * @param connectionInfo
     * @return
     * @throws DatabaseServiceException
     */
    private DatabaseInfo getMetadata(DatabaseConfiguration connectionInfo)  throws DatabaseServiceException {
        try {
                Class.forName("com.mobigen.iris.jdbc.IRISDriver");
        } catch (java.lang.ClassNotFoundException e) {
                e.printStackTrace();
                return null;
        }
        try {
                String url = DB_DRIVER + "//" + connectionInfo.getDatabaseHost() + ":" + connectionInfo.getDatabasePort() 
                            + "/" + connectionInfo.getDatabaseName();
                Connection connection = DriverManager.getConnection(url, connectionInfo.getDatabaseUser(), 
                            connectionInfo.getDatabasePassword());
             
                if(connection != null) {
                    //irisConn = connection;
                    java.sql.DatabaseMetaData metadata;
                    metadata = connection.getMetaData();
                    int dbMajorVersion = metadata.getDatabaseMajorVersion();
                    int dbMinorVersion = metadata.getDatabaseMinorVersion();
                    String dbProductVersion = metadata.getDatabaseProductVersion();
                    String dbProductName = metadata.getDatabaseProductName();
                    DatabaseInfo dbInfo = new DatabaseInfo();
                    dbInfo.setDatabaseMajorVersion(dbMajorVersion);
                    dbInfo.setDatabaseMinorVersion(dbMinorVersion);
                    dbInfo.setDatabaseProductVersion(dbProductVersion);
                    dbInfo.setDatabaseProductName(dbProductName);
                    return dbInfo;
            }
        } catch (SQLException e) {
            logger.error("SQLException::", e);
            throw new DatabaseServiceException(true, e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
        return null;
    }

    @Override
    public DatabaseInfo executeQuery(DatabaseConfiguration dbConfig, String query) throws DatabaseServiceException{
        return null;
    }

    @Override
    public ArrayList<DatabaseColumn> getColumns(DatabaseConfiguration dbConfig, String query) throws DatabaseServiceException{
        try {
                Class.forName("com.mobigen.iris.jdbc.IRISDriver");
        } catch (java.lang.ClassNotFoundException e) {
                e.printStackTrace();
                return null;
        }
        try {
                //Connection connection = irisConn;
                String url = DB_DRIVER + "//" + dbConfig.getDatabaseHost() + ":" + dbConfig.getDatabasePort() 
                            + "/" + dbConfig.getDatabaseName();
                Connection connection = DriverManager.getConnection(url, dbConfig.getDatabaseUser(), 
                            dbConfig.getDatabasePassword());
                Statement statement = connection.createStatement();
                ResultSet queryResult = statement.executeQuery(query);
                java.sql.ResultSetMetaData metadata = queryResult.getMetaData();
                if(metadata instanceof com.mysql.jdbc.ResultSetMetaData) {
                    metadata = (com.mysql.jdbc.ResultSetMetaData)metadata;
                }
                int columnCount = metadata.getColumnCount();
                ArrayList<DatabaseColumn> columns = new ArrayList<DatabaseColumn>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    DatabaseColumn dc = new DatabaseColumn(metadata.getColumnName(i), metadata.getColumnLabel(i),
                            DatabaseUtils.getDbColumnType(metadata.getColumnType(i)), metadata.getColumnDisplaySize(i));
                    columns.add(dc);
                }
                return columns;
        } catch (SQLException e) {
                logger.error("SQLException::", e);
                throw new DatabaseServiceException(true, e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public List<DatabaseRow> getRows(DatabaseConfiguration dbConfig, String query)
            throws DatabaseServiceException {
        try {
                Class.forName("com.mobigen.iris.jdbc.IRISDriver");
        } catch (java.lang.ClassNotFoundException e) {
                e.printStackTrace();
                return null;
        }
        try {
                //Connection connection = irisConn;
                String url = DB_DRIVER + "//" + dbConfig.getDatabaseHost() + ":" + dbConfig.getDatabasePort() 
                            + "/" + dbConfig.getDatabaseName();
                Connection connection = DriverManager.getConnection(url, dbConfig.getDatabaseUser(), 
                            dbConfig.getDatabasePassword());
                Statement statement = connection.createStatement();
                statement.setFetchSize(10);
                ResultSet queryResult = statement.executeQuery(query);
                java.sql.ResultSetMetaData metadata = queryResult.getMetaData();
                if(metadata instanceof com.mysql.jdbc.ResultSetMetaData) {
                    metadata = (com.mysql.jdbc.ResultSetMetaData)metadata;
                }
                int columnCount = metadata.getColumnCount();
                int index = 0;
                List<DatabaseRow> rows = new ArrayList<DatabaseRow>();
                while (queryResult.next()) {
                    DatabaseRow row = new DatabaseRow();
                    row.setIndex(index);
                    List<String> values = new ArrayList<String>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        values.add(queryResult.getString(i));
                    }
                    row.setValues(values);
                    rows.add(row);
                    index++;
                }
                return rows;
        } catch (SQLException e) {
            logger.error("SQLException::", e);
            throw new DatabaseServiceException(true, e.getSQLState(), e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    protected String getDatabaseUrl(DatabaseConfiguration dbConfig) {
            int port = dbConfig.getDatabasePort();
            return "jdbc:" + dbConfig.getDatabaseType() + "://" + dbConfig.getDatabaseHost()
                    + ((port == 0) ? "" : (":" + port)) + "/" + dbConfig.getDatabaseName() + "?useSSL=" + dbConfig.isUseSSL();
    }

    @Override
    public Connection getConnection(DatabaseConfiguration dbConfig)
            throws DatabaseServiceException {
        return null;
    }

    @Override
    public DatabaseInfo testQuery(DatabaseConfiguration dbConfig, String query){
        return null;
    }
}

