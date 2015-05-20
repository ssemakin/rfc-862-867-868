package sample.hello;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.*;
import sample.hello.handler.Echo862Handler;

public class ServerUDP extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    final ActorRef manager;

    public ServerUDP(ActorRef manager) {
        this.manager = manager;
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef udp = Udp.get(getContext().system()).manager();
        udp.tell(UdpMessage.bind(getSelf(), new InetSocketAddress("localhost", 1234)), getSelf());
    }

    @Override
    public void onReceive(Object msg) {
        log.info("{} server got message: {}", getSelf().path(), msg);
        if (msg instanceof Udp.Bound) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Udp.Received) {
            final Udp.Received r = (Udp.Received) msg;
            final ActorRef handler = getContext().actorOf(Props.create(Echo862Handler.class));
            handler.tell(r, getSender());
        } else if (msg.equals(UdpMessage.unbind())) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Udp.Unbound) {
            getContext().stop(getSelf());
        } else unhandled(msg);
    }
}
