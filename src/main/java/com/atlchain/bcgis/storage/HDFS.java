package com.atlchain.bcgis.storage;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.util.logging.Logger;

// TODO HDFS存储类
@Path("storage/hdfs")
public class HDFS {
    @POST
    @Path("put")
    public void put() {
    }

    @POST
    @Path("get")
    public void get() {
    }
}
