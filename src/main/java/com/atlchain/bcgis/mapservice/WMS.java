package com.atlchain.bcgis.mapservice;

import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

// TODO WMS服务类
@Path("mapservice/wms")
public class WMS {

    @PUT
    @Path("publish")
    public void publish() {
    }

    @DELETE
    @Path("delete")
    public void delete() {
    }
}
