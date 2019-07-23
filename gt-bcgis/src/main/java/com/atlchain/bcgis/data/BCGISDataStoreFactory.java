package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;

import java.awt.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {
    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Param[] getParametersInfo() {
        return new Param[0];
    }

    @Override
    public boolean canProcess(Map<String, Serializable> params) {
        return false;
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        return null;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return null;
    }
}
