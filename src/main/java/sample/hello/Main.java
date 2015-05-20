package sample.hello;

import akka.actor.*;
import akka.actor.dsl.Creators;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Udp;

public class Main {

    public static void main(String[] args) {

        ActorSystem system = ActorSystem.create("Hello");

        final ActorRef tcpManager = Tcp.get(system).manager();
        ActorRef a = system.actorOf(Props.create(Server.class, tcpManager), "server-tcp");

        final ActorRef udpManager = Udp.get(system).manager();
        ActorRef b = system.actorOf(Props.create(ServerUDP.class, udpManager), "server-udp");

        ActorRef t = system.actorOf(Props.create(Terminator.class, a, b), "terminator");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                system.stop(a);
                system.stop(b);
                system.awaitTermination();
            }
        });

        system.awaitTermination();
    }

    public static class Terminator extends UntypedActor {

		private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		private final ActorRef ref;
        private final ActorRef ref2;

		public Terminator(ActorRef ref, ActorRef ref2) {
			this.ref = ref;
            this.ref2 = ref2;
			getContext().watch(ref);
            getContext().watch(ref2);
		}

		@Override
		public void onReceive(Object msg) {
			if (msg instanceof Terminated) {
                if(ref.isTerminated() && ref2.isTerminated()) {
//                    log.info("{} has terminated, shutting down system", ref.path());
//                    log.info("{} has terminated, shutting down system", ref2.path());
                    getContext().system().terminate();
                }
			} else unhandled(msg);
		}

	}
}
