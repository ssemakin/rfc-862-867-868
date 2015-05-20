package sample.hello.handler;

import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;

public class Time868Handler extends UntypedActor {

    private ByteString time() {
        return ByteString.fromInts((int) (System.currentTimeMillis() / 1000L + 2208988800L));
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.Received) {
            getSender().tell(TcpMessage.write(time()), getSelf());
            getContext().stop(getSelf());
        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        } else if (msg instanceof Udp.Received) {
            final Udp.Received udpMsg = (Udp.Received) msg;
            getSender().tell(UdpMessage.send(time(), udpMsg.sender()), getSelf());
            getContext().stop(getSelf());
        } else unhandled(msg);
    }

}
