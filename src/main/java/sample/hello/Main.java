package sample.hello;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import sample.hello.handler.Daytime867Handler;
import sample.hello.handler.Echo862Handler;
import sample.hello.handler.Time868Handler;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Main {


    public static void main(String[] args) {

        final int[] ports = Arrays.stream(args).mapToInt(Integer::parseInt).distinct().toArray();
        if(ports.length != 3) {
            System.exit(1);
        }

        final ActorSystem system = ActorSystem.create("tcp-udp");

        final Supplier<Props> echo = () -> Props.create(Echo862Handler.class);
        final Supplier<Props> time = () -> Props.create(Time868Handler.class);
        final Supplier<Props> daytime = () -> Props.create(Daytime867Handler.class);

        final List<ActorRef> actors =
            Arrays.asList(
                system.actorOf(Props.create(ServerTCP.class, system, ports[0], echo), "server-tcp-echo-" + ports[0]),
                system.actorOf(Props.create(ServerUDP.class, system, ports[0], echo), "server-udp-echo-" + ports[0]),
                system.actorOf(Props.create(ServerTCP.class, system, ports[1], time), "server-tcp-time-" + ports[1]),
                system.actorOf(Props.create(ServerUDP.class, system, ports[1], time), "server-udp-time-" + ports[1]),
                system.actorOf(Props.create(ServerTCP.class, system, ports[2], daytime), "server-tcp-daytime-" + ports[2]),
                system.actorOf(Props.create(ServerUDP.class, system, ports[2], daytime), "server-udp-daytime-" + ports[2])
            );

        final ActorRef terminator = system.actorOf(Props.create(Terminator.class, actors), "terminator");
        system.awaitTermination();
    }

    public static class Terminator extends UntypedActor {

		private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
		private final List<ActorRef> servers;

		public Terminator(List<ActorRef> servers) {
            this.servers = servers;
            this.servers.stream().forEach(s -> getContext().watch(s));
            setupSignalHandler();
		}

        private void stopServers() {
            servers.stream().forEach(s -> getContext().system().stop(s));
        }

        private void setupSignalHandler() {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    stopServers();
                    getContext().system().awaitTermination();
                }
            });
        }

        @Override
		public void onReceive(Object msg) {
			if (msg instanceof Terminated) {
                if(servers.stream().allMatch(s -> s.isTerminated())) {
                    getContext().system().terminate();
                }
			} else unhandled(msg);
		}
	}
}
