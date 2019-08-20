package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {

    public BCGISDataStoreFactory() {}

    @Override
    public String getDisplayName() {
        return "BCGIS";
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
    public String getDescription() {
        return "WKB binary file";
    }

    public static final Param NETWORK_CONFIG_PARAM = new Param("config", File.class, "network config file");
    public static final Param CC_NAME_PARAM = new Param("chaincodeName", String.class, "chaincode name");
    public static final Param FUNCTION_NAME_PARAM = new Param("functionName", String.class, "function name");
    public static final Param KEY_PARAM = new Param("Key", String.class, "record key");
    @Override
    public Param[] getParametersInfo() {
        return new Param[] {
                NETWORK_CONFIG_PARAM ,
                CC_NAME_PARAM,
                FUNCTION_NAME_PARAM,
                KEY_PARAM
        };
    }

    @Override
    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        File networkConfigFile = (File) NETWORK_CONFIG_PARAM.lookUp(params);

        String chaincodeName = (String)CC_NAME_PARAM.lookUp(params);
        String functionName = (String)FUNCTION_NAME_PARAM.lookUp(params);
        String key = (String)KEY_PARAM.lookUp(params);

        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                networkConfigFile,
                chaincodeName,
                functionName,
                key
        );
        return bcgisDataStore;
    }

    @Override
    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        File networkConfigFile = (File) NETWORK_CONFIG_PARAM.lookUp(params);

        String chaincodeName = (String)CC_NAME_PARAM.lookUp(params);
        String functionName = (String)FUNCTION_NAME_PARAM.lookUp(params);
        String key = (String)KEY_PARAM.lookUp(params);

        BCGISDataStore bcgisDataStore = new BCGISDataStore(
                networkConfigFile,
                chaincodeName,
                functionName,
                key
        );
        return bcgisDataStore;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
