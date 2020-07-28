package com.google.refine.exporters.sql.iris;

/**
 * HISTORY
 * - 2011-05-09 : Error class 생성
 *
 * @author jinho Park
 */

public enum IRISError {
    UNKNOWN_ERROR(0, "Unknown error."),
    SOCKET_ERROR(1, "Socket error."),
    EXIST_TABLE_ERROR(2, "Table already exists."),
    NO_TABLE_ERROR(3, "Table does not exists."),
    UNSUPPORTED_ENCODING_ERROR(4, "Unsupported encoding error."),
    SQL_SYNTAX_ERROR(5, "SQL Syntax error."),
    ACCESS_DENIED_ERROR(6, "Access denied error."),
    ACCOUNT_ERROR(7, "Account error."),
    TOO_LARGE_RESULT_ERROR(8, "Too large result error."),
    IRIS_ERROR(99, "IRIS error."),
    UNDEFINED_ERROR(100, "Undefined error.");

    private int code;
    private String desc;

    IRISError(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return this.code;
    }

    public String getDesc() {
        return this.desc;
    }
}
