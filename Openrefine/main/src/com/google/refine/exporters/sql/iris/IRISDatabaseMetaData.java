package com.google.refine.exporters.sql.iris;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Struct;
import java.util.ArrayList;
import java.util.List;

import com.google.refine.exporters.sql.iris.config.ColumnTypes;
import com.google.refine.exporters.sql.iris.parser.CreateQueryParser;
import com.google.refine.exporters.sql.iris.parser.IndexQueryParser;
import com.google.refine.exporters.sql.iris.schema.CreateSchema;
import com.google.refine.exporters.sql.iris.schema.IndexSchema;

public class IRISDatabaseMetaData implements DatabaseMetaData {

    private IRISConnection conn;
    private PreparedStatement
            getTables = null,
            getTableTypes = null,
            getTypeInfo = null,
            getSchemas = null,
            getUDTs = null,
            getColumnsTblName = null,
            getSuperTypes = null,
            getSuperTables = null,
            getTablePrivileges = null,
            getIndexInfo = null,
            getProcedures = null,
            getProcedureColumns = null,
            getAttributes = null,
            getBestRowIdentifier = null,
            getVersionColumns = null,
            getColumnPrivileges = null;

    /**
     * Used by PrepStmt to save generating a new statement every call.
     */
    private PreparedStatement getGeneratedKeys = null;
    
    IRISDatabaseMetaData(IRISConnection conn) {
        this.conn = conn;
    }

    void checkOpen() throws SQLException {
        if (conn == null)
            throw new SQLException("connection closed");
    }

    synchronized void close() throws SQLException {
        if (conn == null)
            return;

        try {
            if (getTables != null)
                getTables.close();
            if (getTableTypes != null)
                getTableTypes.close();
            if (getTypeInfo != null)
                getTypeInfo.close();
            if (getSchemas != null)
                getSchemas.close();
            if (getUDTs != null)
                getUDTs.close();
            if (getColumnsTblName != null)
                getColumnsTblName.close();
            if (getSuperTypes != null)
                getSuperTypes.close();
            if (getSuperTables != null)
                getSuperTables.close();
            if (getTablePrivileges != null)
                getTablePrivileges.close();
            if (getIndexInfo != null)
                getIndexInfo.close();
            if (getProcedures != null)
                getProcedures.close();
            if (getProcedureColumns != null)
                getProcedureColumns.close();
            if (getAttributes != null)
                getAttributes.close();
            if (getBestRowIdentifier != null)
                getBestRowIdentifier.close();
            if (getVersionColumns != null)
                getVersionColumns.close();
            if (getColumnPrivileges != null)
                getColumnPrivileges.close();
            if (getGeneratedKeys != null)
                getGeneratedKeys.close();

            getTables = null;
            getTableTypes = null;
            getTypeInfo = null;
            getSchemas = null;
            getUDTs = null;
            getColumnsTblName = null;
            getSuperTypes = null;
            getSuperTables = null;
            getTablePrivileges = null;
            getIndexInfo = null;
            getProcedures = null;
            getProcedureColumns = null;
            getAttributes = null;
            getBestRowIdentifier = null;
            getVersionColumns = null;
            getColumnPrivileges = null;
            getGeneratedKeys = null;
        } finally {
            conn = null;
        }
    }

    public Connection getConnection() {
        return conn;
    }

    public int getDatabaseMajorVersion() {
        return 1;
    }

    public int getDatabaseMinorVersion() {
        return 5;
    }

    public int getDriverMajorVersion() {
        return IRISDriver.DRIVER_MAJOR_VERSION;
    }

    public int getDriverMinorVersion() {
        return IRISDriver.DRIVER_MINOR_VERSION;
    }

    public int getJDBCMajorVersion() {
        return VersionInfo.JDBCMajorVersion;
    }

    public int getJDBCMinorVersion() {
        return VersionInfo.JDBCMinorVersion;
    }

    public int getDefaultTransactionIsolation() {
        return Connection.TRANSACTION_SERIALIZABLE;
    }

    public int getMaxBinaryLiteralLength() {
        return 0;
    }

    public int getMaxCatalogNameLength() {
        return 0;
    }

    public int getMaxCharLiteralLength() {
        return 0;
    }

    public int getMaxColumnNameLength() {
        return 0;
    }

    public int getMaxColumnsInGroupBy() {
        return 0;
    }

    public int getMaxColumnsInIndex() {
        return 0;
    }

    public int getMaxColumnsInOrderBy() {
        return 0;
    }

    public int getMaxColumnsInSelect() {
        return 0;
    }

    public int getMaxColumnsInTable() {
        return 0;
    }

    public int getMaxConnections() {
        return 0;
    }

    public int getMaxCursorNameLength() {
        return 0;
    }

    public int getMaxIndexLength() {
        return 0;
    }

    public int getMaxProcedureNameLength() {
        return 0;
    }

    public int getMaxRowSize() {
        return 0;
    }

    public int getMaxSchemaNameLength() {
        return 0;
    }

    public int getMaxStatementLength() {
        return 0;
    }

    public int getMaxStatements() {
        return 0;
    }

    public int getMaxTableNameLength() {
        return 0;
    }

    public int getMaxTablesInSelect() {
        return 0;
    }

    public int getMaxUserNameLength() {
        return 0;
    }

    public int getResultSetHoldability() {
        return ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    public int getSQLStateType() {
        return sqlStateSQL99;
    }

    public String getDatabaseProductName() {
        return "IRIS";
    }

    public String getDatabaseProductVersion() throws SQLException {
        return conn.libversion();
    }

    public String getDriverName() {
        return IRISDriver.DRIVER_NAME;
    }

    public String getDriverVersion() {
        return conn.getDriverVersion();
    }

    public String getExtraNameCharacters() {
        return "";
    }

    public String getCatalogSeparator() {
        return ".";
    }

    public String getCatalogTerm() {
        return "catalog";
    }

    public String getSchemaTerm() {
        return "schema";
    }

    public String getProcedureTerm() {
        return "not_implemented";
    }

    public String getSearchStringEscape() {
        return null;
    }

    public String getIdentifierQuoteString() {
        return " ";
    }

    public String getSQLKeywords() {
        return "";
    }

    public String getNumericFunctions() {
        return "";
    }

    public String getStringFunctions() {
        return "";
    }

    public String getSystemFunctions() {
        return "";
    }

    public String getTimeDateFunctions() {
        return "";
    }

    public String getURL() {
        return conn.url();
    }

    public String getUserName() {
        return null;
    }

    public boolean allProceduresAreCallable() {
        return false;
    }

    public boolean allTablesAreSelectable() {
        return true;
    }

    public boolean dataDefinitionCausesTransactionCommit() {
        return false;
    }

    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }

    public boolean doesMaxRowSizeIncludeBlobs() {
        return false;
    }

    public boolean deletesAreDetected(int type) {
        return false;
    }

    public boolean insertsAreDetected(int type) {
        return false;
    }

    public boolean isCatalogAtStart() {
        return true;
    }

    public boolean locatorsUpdateCopy() {
        return false;
    }

    public boolean nullPlusNonNullIsNull() {
        return true;
    }

    public boolean nullsAreSortedAtEnd() {
        return !nullsAreSortedAtStart();
    }

    public boolean nullsAreSortedAtStart() {
        return true;
    }

    public boolean nullsAreSortedHigh() {
        return true;
    }

    public boolean nullsAreSortedLow() {
        return !nullsAreSortedHigh();
    }

    public boolean othersDeletesAreVisible(int type) {
        return false;
    }

    public boolean othersInsertsAreVisible(int type) {
        return false;
    }

    public boolean othersUpdatesAreVisible(int type) {
        return false;
    }

    public boolean ownDeletesAreVisible(int type) {
        return false;
    }

    public boolean ownInsertsAreVisible(int type) {
        return false;
    }

    public boolean ownUpdatesAreVisible(int type) {
        return false;
    }

    public boolean storesLowerCaseIdentifiers() {
        return false;
    }

    public boolean storesLowerCaseQuotedIdentifiers() {
        return false;
    }

    public boolean storesMixedCaseIdentifiers() {
        return true;
    }

    public boolean storesMixedCaseQuotedIdentifiers() {
        return false;
    }

    public boolean storesUpperCaseIdentifiers() {
        return false;
    }

    public boolean storesUpperCaseQuotedIdentifiers() {
        return false;
    }

    public boolean supportsAlterTableWithAddColumn() {
        return false;
    }

    public boolean supportsAlterTableWithDropColumn() {
        return false;
    }

    public boolean supportsANSI92EntryLevelSQL() {
        return false;
    }

    public boolean supportsANSI92FullSQL() {
        return false;
    }

    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }

    public boolean supportsBatchUpdates() {
        return true;
    }

    public boolean supportsCatalogsInDataManipulation() {
        return false;
    }

    public boolean supportsCatalogsInIndexDefinitions() {
        return false;
    }

    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return false;
    }

    public boolean supportsCatalogsInProcedureCalls() {
        return false;
    }

    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }

    public boolean supportsColumnAliasing() {
        return true;
    }

    public boolean supportsConvert() {
        return false;
    }

    public boolean supportsConvert(int fromType, int toType) {
        return false;
    }

    public boolean supportsCorrelatedSubqueries() {
        return false;
    }

    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return true;
    }

    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }

    public boolean supportsDifferentTableCorrelationNames() {
        return false;
    }

    public boolean supportsExpressionsInOrderBy() {
        return true;
    }

    public boolean supportsMinimumSQLGrammar() {
        return true;
    }

    public boolean supportsCoreSQLGrammar() {
        return true;
    }

    public boolean supportsExtendedSQLGrammar() {
        return false;
    }

    public boolean supportsLimitedOuterJoins() {
        return true;
    }

    public boolean supportsFullOuterJoins() {
        return false;
    }

    public boolean supportsGetGeneratedKeys() {
        return false;
    }

    public boolean supportsGroupBy() {
        return true;
    }

    public boolean supportsGroupByBeyondSelect() {
        return false;
    }

    public boolean supportsGroupByUnrelated() {
        return false;
    }

    public boolean supportsIntegrityEnhancementFacility() {
        return false;
    }

    public boolean supportsLikeEscapeClause() {
        return false;
    }

    public boolean supportsMixedCaseIdentifiers() {
        return true;
    }

    public boolean supportsMixedCaseQuotedIdentifiers() {
        return false;
    }

    public boolean supportsMultipleOpenResults() {
        return false;
    }

    public boolean supportsMultipleResultSets() {
        return false;
    }

    public boolean supportsMultipleTransactions() {
        return true;
    }

    public boolean supportsNamedParameters() {
        return true;
    }

    public boolean supportsNonNullableColumns() {
        return true;
    }

    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }

    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }

    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }

    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }

    public boolean supportsOrderByUnrelated() {
        return false;
    }

    public boolean supportsOuterJoins() {
        return true;
    }

    public boolean supportsPositionedDelete() {
        return false;
    }

    public boolean supportsPositionedUpdate() {
        return false;
    }

    public boolean supportsResultSetConcurrency(int t, int c) {
        return t == ResultSet.TYPE_FORWARD_ONLY && c == ResultSet.CONCUR_READ_ONLY;
    }

    public boolean supportsResultSetHoldability(int h) {
        return h == ResultSet.CLOSE_CURSORS_AT_COMMIT;
    }

    public boolean supportsResultSetType(int t) {
        return t == ResultSet.TYPE_FORWARD_ONLY;
    }

    public boolean supportsSavepoints() {
        return false;
    }

    public boolean supportsSchemasInDataManipulation() {
        return false;
    }

    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }

    public boolean supportsSchemasInPrivilegeDefinitions() {
        return false;
    }

    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }

    public boolean supportsSchemasInTableDefinitions() {
        return false;
    }

    public boolean supportsSelectForUpdate() {
        return false;
    }

    public boolean supportsStatementPooling() {
        return false;
    }

    public boolean supportsStoredProcedures() {
        return false;
    }

    public boolean supportsSubqueriesInComparisons() {
        return false;
    }

    public boolean supportsSubqueriesInExists() {
        return true;
    } // TODO: check

    public boolean supportsSubqueriesInIns() {
        return true;
    } // TODO: check

    public boolean supportsSubqueriesInQuantifieds() {
        return false;
    }

    public boolean supportsTableCorrelationNames() {
        return false;
    }

    public boolean supportsTransactionIsolationLevel(int level) {
        return level == Connection.TRANSACTION_SERIALIZABLE;
    }

    public boolean supportsTransactions() {
        return true;
    }

    public boolean supportsUnion() {
        return true;
    }

    public boolean supportsUnionAll() {
        return true;
    }

    public boolean updatesAreDetected(int type) {
        return false;
    }

    public boolean usesLocalFilePerTable() {
        return false;
    }

    public boolean usesLocalFiles() {
        return true;
    }

    public boolean isReadOnly() throws SQLException {
        return conn.isReadOnly();
    }

    public ResultSet getAttributes(String c, String s, String t, String a) throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "ATTR_NAME", "DATA_TYPE", "ATTR_TYPE_NAME", "ATTR_SIZE", "DECIMAL_DIGITS",
                "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "ATTR_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH",
                "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE"};
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }


    public ResultSet getBestRowIdentifier(String c, String s, String t, int scope, boolean n) throws SQLException {
        String[] virtualMetaData = {"SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE",
                "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getColumnPrivileges(String c, String s, String t, String colPat) throws SQLException {
        String[] virtualMetaData = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "GRANTOR",
                "GRANTEE", "PRIVILEGE", "IS_GRANTABLE"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getColumns(String catalog, String schema, String tableNamePattern, String columnNamePattern) throws SQLException {
        String[] virtualMetaData = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE",
                "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS",
                "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB",
                "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATLOG", "SCOPE_SCHEMA",
                "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN"};
        List<String[]> virtualResult = new ArrayList<String[]>();
        
        String sql;
        if (catalog == null || catalog.equals("")) {
            sql = "table columns " + tableNamePattern;
            if (tableNamePattern.equals("%")){
                sql = "table columns";
            }
        }
        else {
            sql = "table columns " + catalog + "." + tableNamePattern;
        }

        ResultSet rs = conn.createStatement().executeQuery(sql);

        String _01_tableCat = catalog;
        String _02_tableSchem = "null";
        String _08_bufferSize = "0";
        String _10_decimalDigits = "10";
        String _12_remarks = "0";
        String _14_sqlDataType = "0";
        String _15_sqlDatetimeSub = "0";
        String _19_scopeScatalog = "null";
        String _20_scopeSchema = "null";
        String _21_scopeTable = "0";
        String _22_sourceDataType = "null";
        
        while (rs.next()) {
            String _03_tableName = rs.getString(3);
            String _04_columnName = rs.getString(4);
            String _06_typeName = rs.getString(6);
            String _05_dataType = Integer.toString(ColumnTypes.toInt(_06_typeName));
            String _07_columnSize = rs.getString(7);
            String _09_bufferLength = rs.getString(9);
            if (_09_bufferLength.equals("null")){
                _09_bufferLength = "0";
            }
            String _11_nullable = rs.getString(11);
            String _13_columnDef = rs.getString(13);
            String _16_charOctetLength = rs.getString(16);
            if (_16_charOctetLength.equals("null")){
                _16_charOctetLength = "65535";
            }
            String _17_ordinalPosition = rs.getString(17);
            String _18_isNullable = rs.getString(18);
            String _23_isAutoincrement = (rs.getString(23).equals("True") ? "YES" : "NO");
            String _24_isGeneratedcolumn = "NO";
            String[] record = {_01_tableCat, _02_tableSchem, _03_tableName, _04_columnName, _05_dataType,
                    _06_typeName, _07_columnSize, _08_bufferSize, _09_bufferLength, _10_decimalDigits,
                    _11_nullable, _12_remarks, _13_columnDef, _14_sqlDataType, _15_sqlDatetimeSub,
                    _16_charOctetLength, _17_ordinalPosition, _18_isNullable, _19_scopeScatalog, _20_scopeSchema,
                    _21_scopeTable, _22_sourceDataType, _23_isAutoincrement, _24_isGeneratedcolumn};
            
            virtualResult.add(record);
        }
        
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getCrossReference(String pc, String ps, String pt, String fc, String fs, String ft)
            throws SQLException {
        String[] virtualMetaData = {"PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE",
                "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY"};
        List<String[]> virtualResult = new ArrayList<String[]>();
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getCatalogs() throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"TABLE_CAT"};

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getPrimaryKeys(String c, String s, String table) throws SQLException {
        String[] virtualMetaData = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "KEY_SEQ",
                "PK_NAME"};

        List<String[]> virtualResult = new ArrayList<String[]>();
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    private static String quote(String tableName) {
        if (tableName == null)
            return "null";
        else
            return String.format("'%s'", tableName);
    }

    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKCOLUMN_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE",
                "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY"};
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException {
        String[] virtualMetaData = {"PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT",
                "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE",
                "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException {
        String[] virtualMetaData = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "NON_UNIQUE", "INDEX_QUALIFIER",
                "INDEX_NAME", "TYPE", "ORDINAL_POSITION", "COLUMN_NAME", "ASC_OR_DESC",
                "CARDINALITY", "PAGES", "FILTER_CONDITION"};

        List<String[]> virtualResult = new ArrayList<String[]>();
        String sql;

        if (catalog == null || catalog.equals("")) {
            sql = "table index " + table;
            if (table.equals("%")){
                sql = "table index";
                if (catalog != null){
                    sql = "table index " + catalog + ".*";
                }
            }
        }
        else {
            sql = "table index " + catalog + "." + table;
        }

        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            String tableCat = rs.getString(2);
            String tableSchem = "";
            String tableName = rs.getString(3);
            String nonUnique = "";
            String indexQualifier = "";
            String indexName = rs.getString(1);
            String type = "-1";
            String ordinalPosition = "0";
            String columnName = rs.getString(4);
            String ascOrDesc = "";
            String cardinality = "";
            String pages = "0";
            String filterCondition = "";
            
            String[] virtualData = {tableCat, tableSchem, tableName, nonUnique, indexQualifier, indexName, type, ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition};
            virtualResult.add(virtualData);
        }

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    private String[] createIndexInfoRecord(IndexSchema is, int index) {
        String tableCat = "null";
        String tableSchem = "null";
        String tableName = is.getTableName();
        String nonUnique = Boolean.toString(is.isIndex());
        String indexQualifier = "";
        String indexName = is.getIndexName();
        String type = "3";
        String ordinalPosition = Integer.toString(index + 1);
        String columnName = is.getColumnName(index);
        String ascOrDesc = is.getOrderBy(index);
        String cardinality = "null";
        String pages = "0";
        String filterCondition = "null";
        String[] virtualData = {tableCat, tableSchem, tableName, nonUnique, indexQualifier, indexName,
                type, ordinalPosition, columnName, ascOrDesc, cardinality, pages, filterCondition};
        return virtualData;
    }

    public ResultSet getProcedureColumns(String c, String s, String p, String colPat) throws SQLException {
        String[] virtualMetaData = {"PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "COLUMN_NAME", "COLUMN_TYPE",
                "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE",
                "RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE",
                "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SPECIFIC_NAME"};
        List<String[]> virtualResult = new ArrayList<String[]>();
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");


    }

    public ResultSet getProcedures(String c, String s, String p) throws SQLException {
        String[] virtualMetaData = {"PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "NUM_INPUT_PARAMS", "NUM_OUTPUT_PARAMS",
                "NUM_RESULT_SETS", "REMARKS", "PROCEDURE_TYPE", "SPECIFIC_NAME"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getSuperTables(String c, String s, String t) throws SQLException {
        String[] virtualMetaData = {"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "SUPERTABLE_NAME"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getSuperTypes(String c, String s, String t) throws SQLException {
        String[] virtualMetaData = {"TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SUPERTYPE_CAT", "SUPERTYPE_SCHEM",
                "SUPERTYPE_NAME"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getTablePrivileges(String c, String s, String t) throws SQLException {
        String[] virtualMetaData = {};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }


    public synchronized ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException {
        String[] virtualMetaData;
        List<String[]> virtualResult = new ArrayList<String[]>();
        IRISVirtualStatement virtualStat;
        String sql;

        if (catalog == null || catalog.equals("")) {
            sql = "table list ";
        }
        else {
            sql = "table list " + catalog + ".*";
        }

        ResultSet rs = conn.createStatement().executeQuery(sql);
        while (rs.next()) {
            String tableCat = rs.getString(1);
            String tableSchem = "";
            String tableName = rs.getString(2);
            String tableType = "";
            String remarks = "";

            String[] virtualData = {tableCat, tableSchem, tableName, tableType, remarks};
            virtualResult.add(virtualData);
        }

        virtualMetaData = new String[]{"TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS"};
        virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getTableTypes() throws SQLException {
        String[] virtualMetaData = {"TABLE_TYPE"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        String[] record = {"TABLE"};
        virtualResult.add(record);

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getTypeInfo() throws SQLException {
        String[] virtualMetaData = {"TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX",
                "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE",
                "FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE",
                "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX"};

        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] recordReal = {"REAL", "8", "17", "", "", "[(M,D)] [ZEROFILL]", "1", "false", "3", "false", "false", "true", "REAL", "-308", "308", "0", "0", "10"};
        String[] recordInt = {"INT", "4", "10", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "false", "3", "true", "false", "true", "INT", "0", "0", "0", "0", "10"};
        String[] recordInteger = {"INTEGER", "4", "10", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "false", "3", "true", "false", "true", "INTEGER", "0", "0", "0", "0", "10"};
        String[] recordText = {"TEXT", "-1", "65535", "'", "'", "", "1", "false", "3", "false", "false", "false", "TEXT", "0", "0", "0", "0", "10"};

        virtualResult.add(recordReal);
        virtualResult.add(recordInt);
        virtualResult.add(recordInteger);
        virtualResult.add(recordText);

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getUDTs(String c, String s, String t, int[] types) throws SQLException {
        String[] virtualMetaData = {"TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "CLASS_NAME", "DATA_TYPE",
                "REMARKS", "BASE_TYPE"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getVersionColumns(String c, String s, String t) throws SQLException {
        String[] virtualMetaData = {"SCOPE", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE",
                "BUFFER_LENGTH", "DECIMAL_DIGITS", "PSEUDO_COLUMN"};
        List<String[]> virtualResult = new ArrayList<String[]>();

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    ResultSet getGeneratedKeys() throws SQLException {
        if (getGeneratedKeys == null)
            getGeneratedKeys = conn.prepareStatement("select last_insert_rowid();");
        return getGeneratedKeys.executeQuery();
    }

    /**
     * Replace all instances of ' with ''
     */
    private String escape(final String val) {
        // TODO: this function is ugly, pass this work off to SQLite, then we
        //       don't have to worry about Unicode 4, other characters needing
        //       escaping, etc.
        int len = val.length();
        StringBuffer buf = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            if (val.charAt(i) == '\'')
                buf.append('\'');
            buf.append(val.charAt(i));
        }
        return buf.toString();
    }

    public Struct createStruct(String t, Object[] attr) throws SQLException {
        throw new SQLException("Not yet implemented by SQLite JDBC driver");
    }

    public ResultSet getFunctionColumns(String a, String b, String c, String d) throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "COLUMN_NAME", "COLUMN_TYPE",
                "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE",
                "RADIX", "NULLABLE", "REMARKS", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION",
                "IS_NULLABLE", "SPECIFIC_NAME"};
        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"NAME", "MAX_LEN", "DEFAULT_VALUE", "DESCRIPTION"};

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    @Override
    public ResultSet getFunctions(String catalog, String schemaPattern,
                                  String functionNamePattern) throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "REMARKS", "FUNCTION_TYPE",
                "SPECIFIC_NAME"};

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSet getSchemas() throws SQLException {
        return getSchemas("", "");
    }

    @Override
    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        List<String[]> virtualResult = new ArrayList<String[]>();
        String[] virtualMetaData = {"TABLE_SCHEM", "TABLE_CATALOG"};

        IRISVirtualStatement virtualStat = new IRISVirtualStatement(virtualResult, virtualMetaData);
        return virtualStat.executeQuery("");
    }

    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }
}

