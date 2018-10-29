package ru.ingvord.http2.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10/29/18
 */
@Path("/")
public class Http2Rest {
    @GET
    @Produces("text/plain")
    public String get(){
        return  "Hello World";
    }
}
