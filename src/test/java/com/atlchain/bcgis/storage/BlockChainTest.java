package com.atlchain.bcgis.storage;

import com.atlchain.bcgis.Utils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;

import java.io.*;
import java.net.URISyntaxException;

public class BlockChainTest {

    private BlockChain client;
    private File networkFile = new File(this.getClass().getResource("/network-config-test.yaml").toURI());

    public BlockChainTest() throws URISyntaxException {
        client = new BlockChain(networkFile);
    }

    /**
     *  根据FeatureID从区块链上查询数据
     *  只能单独查询，整体查询需要用bcgis里面的getRecord
     * @throws ParseException
     */
    @Test
    public void testQueryGeometryFromChain() throws ParseException {
        String key = "6bff876faa82c51aee79068a68d4a814af8c304a0876a08c0e8fe16e5645fde4-9";
        byte[][] result = client.getRecordBytes(
                key,
                "bcgiscc",
                "GetRecordByKey"
        );
        Geometry geometry = Utils.getGeometryFromBytes(result[0]);
        System.out.println(geometry.getNumGeometries());
    }

}
