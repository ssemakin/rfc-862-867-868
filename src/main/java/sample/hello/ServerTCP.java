package sample.hello;

import java.net.InetSocketAddress;
import java.util.function.Supplier;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.TcpMessage;

public class ServerTCP extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final ActorRef manager;
    private final int port;
    private final Supplier<Props> handlerFactory;

    public ServerTCP(ActorSystem system, int port, Supplier<Props> handlerFactory) {
        this.manager = Tcp.get(system).manager();
        this.port = port;
        this.handlerFactory = handlerFactory;
    }

    @Override
    public void preStart() throws Exception {
        manager.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("localhost", port), 100), getSelf());
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
            final ActorRef handler = getContext().actorOf(handlerFactory.get());
            getSender().tell(TcpMessage.register(handler), getSelf());
        } else unhandled(msg);
    }
}
