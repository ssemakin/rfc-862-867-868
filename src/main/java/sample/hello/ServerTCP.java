package sample.hello;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;
import sample.hello.handler.Daytime867Handler;

public class ServerTCP extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    final ActorRef manager;

    public ServerTCP(ActorRef manager) {
        this.manager = manager;
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef tcp = Tcp.get(getContext().system()).manager();
        tcp.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("localhost", 1234), 100), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        log.info("{} server got message {}", getSelf().path(), msg);
        if (msg instanceof Tcp.Bound) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Tcp.CommandFailed) {
            getContext().stop(getSelf());
        } else if (msg instanceof Tcp.Connected) {
            final Tcp.Connected conn = (Tcp.Connected) msg;
            manager.tell(conn, getSelf());
            final ActorRef handler = getContext().actorOf(Props.create(Daytime867Handler.class));
            getSender().tell(TcpMessage.register(handler), getSelf());
        } else unhandled(msg);
    }
}