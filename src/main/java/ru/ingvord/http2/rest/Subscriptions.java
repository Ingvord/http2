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
import java.util.Collections;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/1/18
 */
@Path("/subscriptions")
@Produces("application/json")
public class Subscriptions {

    private final List<Subscription> subscriptions = new ArrayList<>(1);

    private Sse sse;
    private SseBroadcaster sseBroadcaster;

    TangoEventListener<Object> tangoEventListener = new TangoEventListener<Object>() {
        @Override
        public void onEvent(EventData<Object> data) {
            OutboundSseEvent event = sse.newEventBuilder().
                    id("EventId").
                    name("EventName").
                    data(data.getValue()).
                    reconnectDelay(10000).
                    comment("Anything i wanna comment here!").
                    build();

            sseBroadcaster.broadcast(event);
        }

        @Override
        public void onError(Exception cause) {
            //TODO
            OutboundSseEvent event = sse.newEventBuilder().
                    id("EventId").
                    name("EventName").
                    data("Data").
                    reconnectDelay(10000).
                    comment("Anything i wanna comment here!").
                    build();

            sseBroadcaster.broadcast(event);
        }
    };



    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
//        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
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
    public Subscription getCustomer(@PathParam("id") int id) {
        return subscriptions.get(id);
    }

    @PUT
    public Subscription put(){
        throw new UnsupportedOperationException();
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

            try {
                TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://hzgxenvtest:10000/sys/tg_test/1");

                proxy.subscribeToEvent("double_scalar", TangoEvent.CHANGE);

                proxy.addEventListener("double_scalar", TangoEvent.CHANGE, tangoEventListener);
            } catch (TangoProxyException|NoSuchAttributeException e) {
                //ignored
            }
        }

        @GET
        @Produces(MediaType.SERVER_SENT_EVENTS)
        public void get(@Context SseEventSink eventSink){
            sink = eventSink;
            sseBroadcaster.register(eventSink);
        }

        @DELETE
        public void delete(){
            sink.close();
        }
    }
}
