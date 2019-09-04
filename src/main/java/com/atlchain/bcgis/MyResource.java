package com.atlchain.bcgis;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("myresource")
public class MyResource {
    /**
     * 示例代码
     * 运行Main，然后在浏览器中访问 http://localhost:8080/myapp/myresource
     * @return
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
        return "Got it!";
    }
}
