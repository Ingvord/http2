package ru.ingvord.http2.rest;

import org.tango.client.ez.proxy.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 10/29/18
 */
@Path("/")
//@Singleton
public class Http2Rest {
    private volatile Sse sse;

    @GET
    @Produces("text/plain")
    public String get(){
        return  "Hello World";
    }



    private volatile SseBroadcaster sseBroadcaster;

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

//    @PostConstruct
    public Http2Rest() {
        try {
            TangoProxy proxy = TangoProxies.newDeviceProxyWrapper("tango://hzgxenvtest:10000/sys/tg_test/1");

            proxy.subscribeToEvent("double_scalar", TangoEvent.CHANGE);

            proxy.addEventListener("double_scalar", TangoEvent.CHANGE, tangoEventListener);
        } catch (TangoProxyException|NoSuchAttributeException e) {
            //ignored
        }
    }

    @GET
    @Path("/event")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void event(@Context SseEventSink eventSink, @Context Sse sse){
        sseBroadcaster.register(eventSink);
    }
}
