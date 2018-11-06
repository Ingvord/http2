package ru.ingvord.http2.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/5/18
 */
@Path("/multi-event")
public class MultiEventSource {

    private Sse sse;
    private SseBroadcaster sseBroadcaster;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
//        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();

        exec.submit(new Runnable() {
            Random random = new Random();

            @Override
            public void run() {
                try {
                    do{
                        Thread.sleep(1);

                        OutboundSseEvent event = sse.newEventBuilder().
                                id("EventId").
                                name(String.valueOf(random.nextInt(500))).
                                data(Math.random()).
                                reconnectDelay(10000).
                                comment("Anything i wanna comment here!").
                                build();

                        sseBroadcaster.broadcast(event);
                    } while(!Thread.currentThread().isInterrupted());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private final ExecutorService exec = Executors.newSingleThreadExecutor();


    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void get(@Context SseEventSink sink){
        sseBroadcaster.register(sink);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void getSingle(@Context SseEventSink sink){
        sseBroadcaster.register(sink);
    }

}
