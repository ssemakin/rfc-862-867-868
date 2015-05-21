package sample.hello.handler;

import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.TcpMessage;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Daytime867Handler extends UntypedActor {

    private ByteString daytime() {
        return ByteString.fromString(new SimpleDateFormat("dd MMM yyyy HH:mm:ss zzz").format(new Date()) + "\r\n");
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof Tcp.Received) {
            getSender().tell(TcpMessage.write(daytime()), getSelf());
            getContext().stop(getSelf());
        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        } else if (msg instanceof Udp.Received) {
            final Udp.Received udpMsg = (Udp.Received) msg;
            getSender().tell(UdpMessage.send(daytime(), udpMsg.sender()), getSelf());
            getContext().stop(getSelf());
        } else unhandled(msg);
    }

}
