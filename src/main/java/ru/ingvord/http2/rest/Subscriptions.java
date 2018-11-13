package ru.ingvord.http2.rest;

import org.tango.client.ez.proxy.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/1/18
 */
@Path("/subscriptions")
@Produces("application/json")
public class Subscriptions {

    private final List<Subscription> subscriptions = new ArrayList<>(1);

    private Sse sse;


    Listener1 tangoEventListener1;

    Listener2 tangoEventListener2;
    Listener3 tangoEventListener3;


    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
//        this.eventBuilder = sse.newEventBuilder();
//        this.sseBroadcaster = sse.newBroadcaster();

        tangoEventListener1 = new Listener1();

        tangoEventListener2 = new Listener2();

        tangoEventListener3 = new Listener3();


        try {
            TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://hzgxenvtest:10000/sys/tg_test/1");

            proxy.subscribeToEvent("double_scalar", TangoEvent.CHANGE);

            proxy.addEventListener("double_scalar", TangoEvent.CHANGE, tangoEventListener1);
        } catch (TangoProxyException|NoSuchAttributeException e) {
            //ignored
        }

        try {
            TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://hzgxenvtest:10000/sys/tg_test/1");

            proxy.subscribeToEvent("long_scalar", TangoEvent.CHANGE);

            proxy.addEventListener("long_scalar", TangoEvent.CHANGE, tangoEventListener2);
        } catch (TangoProxyException|NoSuchAttributeException e) {
            //ignored
        }

        try {
            TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://hzgxenvtest:10000/sys/tg_test/1");

            proxy.subscribeToEvent("no_value", TangoEvent.CHANGE);

            proxy.addEventListener("no_value", TangoEvent.CHANGE, tangoEventListener3);
        } catch (TangoProxyException|NoSuchAttributeException e) {
            //ignored
        }
    }


    @GET
    public Collection<Subscription> get(){
        return subscriptions;
    }

    @POST
    public Subscription post(){
        Subscription subscription = new Subscription("tango://hzxenvtest:10000/sys/tg_test/1/double_scalar/change", 0, "test");
        subscriptions.add(subscription);
        return subscription;
    }

    @Path("/{id}")
    public Subscription getSubscription(@PathParam("id") int id) {
        return subscriptions.get(id);
    }

    @Path("{id}")
    @Produces("application/json")
    public class Subscription {
        public final String target;
        public final int id;
        public final String clientId;
        private volatile SseEventSink sink;

        private Subscription(String target, int id, String clientId) {
            this.target = target;
            this.id = id;
            this.clientId = clientId;


        }

        @GET
        @Path("/event-stream")
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void get(@Context SseEventSink eventSink){
            sink = eventSink;
//            sseBroadcaster.register(eventSink);
        }

        @PUT
        @Path("/1")
        public void put_1(){
            tangoEventListener1.sseBroadcaster.register(sink);
        }

        @PUT
        @Path("/2")
        public void put_2(){
            tangoEventListener2.sseBroadcaster.register(sink);
        }

        @PUT
        @Path("/3")
        public void put_3(){
            tangoEventListener3.sseBroadcaster.register(sink);
        }

        @DELETE
        public void delete(){
            sink.close();
        }
    }

    private class Listener1 implements TangoEventListener<Object>{
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();

        {
            sseBroadcaster.onError(new BiConsumer<SseEventSink, Throwable>() {
                @Override
                public void accept(SseEventSink sseEventSink, Throwable throwable) {
                    System.err.println(throwable.getMessage());
                }
            });
        }

        @Override
        public void onEvent(EventData<Object> data) {
            OutboundSseEvent event = sse.newEventBuilder().
                    id(String.valueOf(System.currentTimeMillis())).
                    name("1").
                    data(EventData.class, data).
                    mediaType(MediaType.APPLICATION_JSON_TYPE).
                    reconnectDelay(10000).
                    build();

            sseBroadcaster.broadcast(event);
        }

        @Override
        public void onError(Exception cause) {
            //TODO
            OutboundSseEvent event = sse.newEventBuilder().
                    id(String.valueOf(System.currentTimeMillis())).
                    name("error").
                    data(cause.getMessage()).
//                    reconnectDelay(10000).
        build();

            sseBroadcaster.broadcast(event);
        }
    }


    private class Listener2 implements TangoEventListener<Object>{
        SseBroadcaster sseBroadcaster = sse.newBroadcaster();


        @Override
        public void onEvent(EventData<Object> data) {
            OutboundSseEvent event = sse.newEventBuilder().
                    id(String.valueOf(System.currentTimeMillis())).
                    name("2").
                    data(EventData.class, data).
                    mediaType(MediaType.APPLICATION_JSON_TYPE).
                    reconnectDelay(10000).
                    build();

            sseBroadcaster.broadcast(event);
        }

        @Override
        public void onError(Exception cause) {
            //TODO
            OutboundSseEvent event = sse.newEventBuilder().
                    id(String.valueOf(System.currentTimeMillis())).
                    name("error").
                    data(cause.getMessage()).
//                    reconnectDelay(10000).
        build();

            sseBroadcaster.broadcast(event);
        }
    }

    private class Listener3 implements TangoEventListener<Object>{
            SseBroadcaster sseBroadcaster = sse.newBroadcaster();

            @Override
            public void onEvent(EventData<Object> data) {
                OutboundSseEvent event = sse.newEventBuilder().
                        id(String.valueOf(System.currentTimeMillis())).
                        name("3").
                        data(data.getValue()).
                        reconnectDelay(10000).
                        build();

                sseBroadcaster.broadcast(event);
            }

            @Override
            public void onError(Exception cause) {
                //TODO
                OutboundSseEvent event = sse.newEventBuilder().
                        id(String.valueOf(System.currentTimeMillis())).
                        name("error").
                        data(cause.getMessage()).
//                    reconnectDelay(10000).
        build();

                sseBroadcaster.broadcast(event);
            }
    }
}
