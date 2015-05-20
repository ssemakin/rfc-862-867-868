package sample.hello;

import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.io.Udp;
import akka.io.UdpMessage;
import akka.util.ByteString;

public class SimplisticHandler extends UntypedActor {
    @Override
    public void onReceive(Object msg) throws Exception {
        System.out.println("handler got message " + msg);
        if (msg instanceof Tcp.Received) {
            final ByteString data = ((Received) msg).data();
            getSender().tell(TcpMessage.write(data), getSelf());
        } else if (msg instanceof Tcp.ConnectionClosed) {
            getContext().stop(getSelf());
        } else if (msg instanceof Udp.Received) {
            final Udp.Received udpMsg = (Udp.Received) msg;
            getSender().tell(UdpMessage.send(udpMsg.data(), udpMsg.sender()), getSelf());
            getContext().stop(getSelf());
        }
    }
}
