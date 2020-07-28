package com.google.refine.exporters.sql.iris;

/**
 * IRISStatement.java
 * <p>
 * <p>
 * HISTORY
 * 코드 변경 이력 정보를 남긴다.
 * - 2009-05-12 : IRISStatement class 생성
 *
 * @author sungwon Park
 */

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IRISVirtualStatement extends IRISStatement implements Statement {
    protected ArrayList<String> queryList = new ArrayList<String>();

    private List<String[]> virtualResult;
    private String[] virtualMetaData;


    IRISVirtualStatement() throws SQLException {
    }

    IRISVirtualStatement(List<String[]> result, String[] metaData) throws SQLException {
        this.virtualResult = result;
        this.virtualMetaData = metaData;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        IRISVirtualResultSet rs = new IRISVirtualResultSet(this);
        return rs;
    }

    @Override
    public void closeOnCompletion() throws SQLException {

    }

    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    public boolean getData(List<String[]> result) {
        if (virtualResult != null) {
            result.addAll(this.virtualResult);
            this.virtualResult = null;
        }
        return true;
    }

    public ResultSetMetaData getMetaData() {
        String[] typeSet = new String[this.virtualMetaData.length];
        for (int i = 0; i < this.virtualMetaData.length; i++) {
            typeSet[i] = "STRING";
        }
        IRISResultSetMetaData metaData = new IRISResultSetMetaData(this.virtualMetaData, typeSet, this.virtualMetaData.length, "");
        return metaData;
    }
}