package com.atlchain.bcgis.storage;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

// TODO FastDFS存储类
@Path("storage/fastdfs")
public class FastDFS {
    @POST
    @Path("put")
    public void put() {
    }

    @POST
    @Path("get")
    public void get() {
    }

    @DELETE
    @Path("delete")
    public void delete() {

    }
}
