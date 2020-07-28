package com.mobigen.iris.jdbc;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class LoadProperty {
     private boolean useCsvDat;
     private boolean useUniqueLoad;
     private String uniqueLoadName;
     private boolean useZlib;

    public LoadProperty() {
        this.useZlib = false;
        this.useCsvDat = false;
        this.useUniqueLoad = false;
        this.uniqueLoadName = "";
    }

    public void setCsvData() {
        this.useCsvDat = true;
    }

    public void setUniqueLoad(String uniqueLoadName) {
        this.useUniqueLoad = true;
        this.uniqueLoadName = uniqueLoadName;
    }

    public void setZlib() {
        this.useZlib = true;
    }

    public boolean isCsvData() {
        return this.useCsvDat;
    }

    public boolean isUniqueLoad() {
        return this.useUniqueLoad;
    }

    public boolean isZlib() {
        return this.useZlib;
    }

    public String toStr() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("is_csv_dat", this.useCsvDat);
            jsonObject.put("is_unique_load", this.useUniqueLoad);
            jsonObject.put("unique_load_name", this.uniqueLoadName);
            jsonObject.put("use_zlib", this.useZlib);
        } catch(JSONException err) {
        }

        return jsonObject.toString();
    }
}
