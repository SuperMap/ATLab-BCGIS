package com.atlchain.bcgis.index;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

// TODO RTree索引类
@Path("index/rtree")
public class RTree {

    @GET
    @Path("create")
    public void create(){
    }
}
