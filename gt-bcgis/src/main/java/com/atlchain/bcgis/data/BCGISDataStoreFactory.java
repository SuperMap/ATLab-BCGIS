package com.atlchain.bcgis.data;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataUtilities;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

public class BCGISDataStoreFactory implements DataStoreFactorySpi {
    Logger logger = Logger.getLogger(BCGISDataStoreFactory.class.toString());

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
    public static final Param KEY_PARAM = new Param("recordKey", String.class, "record key");
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
        File file = (File)NETWORK_CONFIG_PARAM.lookUp(params);
        System.out.println(file);
        File networkConfigFile = null;

        if (file.getPath().startsWith("file:")) {
            String path = file.getPath();
            path = path.replace("\\",File.separator);
            networkConfigFile = new File(new URL(path).getPath());
        } else {
            networkConfigFile = file;
        }

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
        logger.info("==============>createcreateNewDataStore" );
        File networkConfigFile = new File(((File)NETWORK_CONFIG_PARAM.lookUp(params)).getPath());

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
