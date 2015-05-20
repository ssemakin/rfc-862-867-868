package sample.hello;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Udp;

public class Main {

//	public static void main(String[] args) {
//		ActorSystem system = ActorSystem.create("Hello");
//		ActorRef a = system.actorOf(Props.create(HelloWorld.class), "helloWorld");
//		system.actorOf(Props.create(Terminator.class, a), "terminator");
//		system.awaitTermination();
//        System.out.println("now done");
//	}

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("Hello");

        final ActorRef tcpManager = Tcp.get(system).manager();
        ActorRef a = system.actorOf(Props.create(Server.class, tcpManager), "server-tcp");

        final ActorRef udpManager = Udp.get(system).manager();
        ActorRef b = system.actorOf(Props.create(ServerUDP.class, udpManager), "server-udp");

        system.actorOf(Props.create(Terminator.class, a, b), "terminator");
        system.awaitTermination();
        System.out.println("now done");
    }

    public static class Terminator extends UntypedActor {

		private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		private final ActorRef ref;

		public Terminator(ActorRef ref, ActorRef ref2) {
			this.ref = ref;
			getContext().watch(ref);
            getContext().watch(ref2);
		}

		@Override
		public void onReceive(Object msg) {
			if (msg instanceof Terminated) {
				log.info("{} has terminated, shutting down system", ref.path());
				getContext().system().terminate();
			} else {
				unhandled(msg);
			}
		}

	}
}
