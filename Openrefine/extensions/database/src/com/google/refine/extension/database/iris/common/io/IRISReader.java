package com.mobigen.iris.jdbc.common.io;

import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by social on 16. 5. 17.
 */
public interface IRISReader {
    public void convertToList(List<String[]> result, String readStr) throws Exception;
    public ArrayList<String[]> converToMetadata(String readStr) throws Exception;
}
