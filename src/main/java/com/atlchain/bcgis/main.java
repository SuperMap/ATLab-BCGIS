package com.atlchain.bcgis;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

public class main extends ResourceConfig {
    public static final String BASE_URI = "http://localhost:8899/bcgis/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.atlchain.bcgis");
        // 注册multipart
        rc.register(MultiPartFeature.class);
        rc.register(CorsFilter.class);
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
        System.in.read();
        server.shutdownNow();
    }
}
