package com.atlchain.bcgis.storage;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

// TODO MongoDB存储类
@Path("storage/mongodb")
public class MongoDB {
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
