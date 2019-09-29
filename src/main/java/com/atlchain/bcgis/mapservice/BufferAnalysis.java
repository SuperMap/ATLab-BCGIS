package com.atlchain.bcgis.mapservice;

import com.atlchain.bcgis.Utils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.locationtech.jts.geom.Geometry;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

/**
 * 缓冲区分析类
 */
@Path("mapservice/buffer")
public class BufferAnalysis {

    private Logger logger = Logger.getLogger(BufferAnalysis.class.toString());

    @Path("/bufferAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String buffer(
            @FormDataParam("bufferRadius") String bufferRadius,
            @FormDataParam("JSON") String JSONS
    ){
        String bufferJSON = bufferAnalysis(bufferRadius,JSONS);
        return bufferJSON;
    }

    @Path("/unionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String union(
            @FormDataParam("JSONS1") String JSONS1,
            @FormDataParam("JSONS2") String JSONS2
    ){
        String unionJSON = unionAnalysis(JSONS1,JSONS2);
        return unionJSON;
    }

    @Path("/intersectionAnalysis")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String intersectionJSON(
            @FormDataParam("JSONS1") String JSONS1,
            @FormDataParam("JSONS2") String JSONS2
    ){
        String intersectionJSON = intersectionAnalysis(JSONS1,JSONS2);
        return intersectionJSON;
    }

    private String bufferAnalysis(String bufferRadius , String json){
        Geometry geometry = Utils.geometryjsonToGeometry(json);
        Geometry bufferGeometry = geometry.buffer(Double.valueOf(bufferRadius));
        String bufferJSON = Utils.geometryTogeometryJSON(bufferGeometry);
        return bufferJSON;
    }

    private String unionAnalysis(String...jsons){
        Geometry geometry ;
        Geometry geometryUnion = Utils.geometryjsonToGeometry(jsons[0]);
        for (String json : jsons) {
            geometry =Utils.geometryjsonToGeometry(json);
            geometryUnion = geometryUnion.union(geometry);
        }
        String unionJSON = Utils.geometryTogeometryJSON(geometryUnion);
        return  unionJSON;
    }

    private String intersectionAnalysis(String...jsons){
        Geometry geometry ;
        Geometry geometryIntersection = Utils.geometryjsonToGeometry(jsons[0]);
        for (String json : jsons) {
            geometry =Utils.geometryjsonToGeometry(json);
            geometryIntersection = geometryIntersection.intersection(geometry);
        }
        String intersectionJSON = Utils.geometryTogeometryJSON(geometryIntersection);
        return intersectionJSON;
    }
}
