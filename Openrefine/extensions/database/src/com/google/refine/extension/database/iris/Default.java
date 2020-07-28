package com.mobigen.iris.jdbc;

public class Default {
    protected static final int BUFFER_SIZE = 1024 * 1024;
    protected static byte[] field_sep = new String("_MOBIGEN_FIELD_").getBytes();
    protected static byte[] record_sep = new String("_MOBIGEN_LF_").getBytes();
}
