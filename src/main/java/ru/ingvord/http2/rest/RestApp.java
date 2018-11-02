package ru.ingvord.http2.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.sse.Sse;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10/29/18
 */
@ApplicationPath("/rest")
public class RestApp extends Application {
    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();

    public RestApp() {
//        empty.add(Http2Rest.class);
//        singletons.add(new Http2Rest());
        singletons.add(new Subscriptions());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return empty;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}