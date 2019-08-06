package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {

    public BCGISDataStoreFactory() {}

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
//        File file = (File) FILE_PARAM.lookUp(params);
        return new BCGISDataStore();
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
                    "bcgis",
                    String.class,
                    "data from blockchain"
            );

    @Override
    public Param[] getParametersInfo() {
        return new Param[] { FILE_PARAM };
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        return DataUtilities.canProcess(params, getParametersInfo());
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
