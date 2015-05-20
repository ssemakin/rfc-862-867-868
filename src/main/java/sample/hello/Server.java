package sample.hello;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.TcpMessage;

public class Server extends UntypedActor {

    final ActorRef manager;

    public Server(ActorRef manager) {
        this.manager = manager;
    }

    public static Props props(ActorRef manager) {
        return Props.create(Server.class, manager);
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef tcp = Tcp.get(getContext().system()).manager();
        tcp.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("localhost", 1234), 100), getSelf());
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        System.out.println("tcp server got message " + msg);
        if (msg instanceof Tcp.Bound) {
            manager.tell(msg, getSelf());
        } else if (msg instanceof Tcp.CommandFailed) {
            getContext().stop(getSelf());
        } else if (msg instanceof Tcp.Connected) {
            final Tcp.Connected conn = (Tcp.Connected) msg;
            manager.tell(conn, getSelf());
            final ActorRef handler = getContext().actorOf(Props.create(SimplisticHandler.class));
            getSender().tell(TcpMessage.register(handler), getSelf());
        } else unhandled(msg);
    }
}
