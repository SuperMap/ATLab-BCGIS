package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.KVP;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {

    public BCGISDataStoreFactory() {}

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        File file = (File) FILE_PARAM.lookUp(params);
        return new BCGISDataStore(file);
    }

    @Override
    public String getDisplayName() {
        return "BCGIS";
    }

    @Override
    public String getDescription() {
        return "WKB binary file";
    }

    public static final Param FILE_PARAM =
            new Param(
                    "file",
                    File.class,
                    "WKB binary file",
                    true,
                    null,
                    new KVP(Param.EXT, "wkb"));

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { FILE_PARAM };
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        try {
            File file = (File) FILE_PARAM.lookUp(params);
            if (file != null) {
                return file.getPath().toLowerCase().endsWith(".wkb");
            }
        } catch (IOException e) {
            // ignore as we are expected to return true or false
        }
        return false;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        return null;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
