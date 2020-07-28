package com.mobigen.iris.jdbc.common.io;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by social on 16. 5. 16.
 */
public class JsonIRISReader implements IRISReader {

    public static String[] toStringArray(JSONArray array) {
        if(array==null) {
            return null;
        }

        String[] arr = new String[array.length()];
        for(int i=0; i<arr.length; i++) {
            arr[i] = array.optString(i);
        }
        return arr;
    }

    @Override
    public void convertToList(List<String[]> result, String readStr) throws JSONException {
        JSONArray record = new JSONArray(readStr);
        for (int i = 0; i < record.length(); i++) {
            String[] values = toStringArray((JSONArray) record.get(i));
            result.add(values);
        }
    }

    @Override
    public ArrayList<String[]> converToMetadata(String readStr) throws JSONException {
        JSONArray json = new JSONArray(readStr);
        ArrayList<String[]> tmpList = new ArrayList<String[]>();
        tmpList.add(JsonIRISReader.toStringArray((JSONArray) json.get(0)));
        tmpList.add(JsonIRISReader.toStringArray((JSONArray) json.get(1)));
        return tmpList;
    }
}
