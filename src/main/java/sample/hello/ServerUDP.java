package sample.hello;

import java.net.InetSocketAddress;
import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.*;

public class ServerUDP extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorRef manager;
    private final int port;
    private final Supplier<Props> handlerFactory;

    public ServerUDP(ActorSystem system, int port, Supplier<Props> handlerFactory) {
        this.manager = Udp.get(system).manager();
        this.port = port;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void preStart() throws Exception {
        manager.tell(UdpMessage.bind(getSelf(), new InetSocketAddress("localhost", port)), getSelf());
    }

    @Override
    public void onReceive(Object msg) {
        log.info("{} server got message: {}", getSelf().path(), msg);
        if (msg instanceof Udp.Bound) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Udp.Received) {
            final Udp.Received r = (Udp.Received) msg;
            final ActorRef handler = getContext().actorOf(handlerFactory.get());
            handler.tell(r, getSender());
        } else if (msg.equals(UdpMessage.unbind())) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Udp.Unbound) {
            getContext().stop(getSelf());
        } else unhandled(msg);
    }
}
