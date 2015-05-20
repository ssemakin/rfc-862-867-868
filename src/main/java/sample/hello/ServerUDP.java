package sample.hello;

import java.net.InetSocketAddress;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.*;
import sample.hello.handler.Echo862Handler;

public class ServerUDP extends UntypedActor {

//    final ActorRef nextActor;
    final ActorRef manager;

    public ServerUDP(ActorRef manager) {
//        this.nextActor = nextActor;
        this.manager = manager;

// request creation of a bound listen socket
//        final ActorRef mgr = Udp.get(getContext().system()).getManager();
//        mgr.tell(UdpMessage.bind(getSelf(), new InetSocketAddress("localhost", 8877)), getSelf());
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef udp = Udp.get(getContext().system()).manager();
//        udp.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("localhost", 1234), 100), getSelf());
        udp.tell(UdpMessage.bind(getSelf(), new InetSocketAddress("localhost", 1234)), getSelf());
    }

    @Override
    public void onReceive(Object msg) {
        System.out.println("udp server got message " + msg);
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

//    @Override
//    public void onReceive(Object msg) {
//        System.out.println("1 udp server got message " + msg);
//        if (msg instanceof Udp.Bound) {
//            final Udp.Bound b = (Udp.Bound) msg;
//            getContext().become(ready(getSender()));
//        } else unhandled(msg);
//    }


//    private Procedure<Object> ready(final ActorRef socket) {
//        return new Procedure<Object>() {
//            @Override
//            public void apply(Object msg) throws Exception {
//                System.out.println("2 udp server got message " + msg);
//                if (msg instanceof Udp.Received) {
//                    final Udp.Received r = (Udp.Received) msg;
//// echo server example: send back the data
//                    socket.tell(UdpMessage.send(r.data(), r.sender()), getSelf());
//// or do some processing and forward it on
////                    final Object processed = "bye dude!!!"; // parse data etc., e.g. using PipelineStage
////                            nextActor.tell(processed, getSelf());
//                } else if (msg.equals(UdpMessage.unbind())) {
//                    socket.tell(msg, getSelf());
//                } else if (msg instanceof Udp.Unbound) {
//                    getContext().stop(getSelf());
//                } else unhandled(msg);
//            }
//        };
//    }
}
