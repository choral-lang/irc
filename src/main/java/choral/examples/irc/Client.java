package choral.examples.irc;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Client {
    private Gson gson;
    private ExecutorService executor;
    private IrcChannel_A ch;
    private Irc_Client irc;
    private ClientState state;

    public Client() {
        gson = new Gson();
        executor = null;
        ch = null;
        irc = null;
        state = null;
    }

    private static Integer parseInt(String str) {
        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    private static String[] splitArgs(String rest, int n) {
        return rest == null ? new String[0] : rest.split(" +", n);
    }

    public boolean isOpen() {
        // NOTE: The channel could've been closed by the loop threads without us
        // requesting it, e.g. if the server terminated the connection or there
        // was some sort of error. Using this function, we can "poll" whether
        // the channel is open on each command. A "callback" mechanism with
        // cooperation between the main thread and the loop threads would be
        // nicer.
        return ch != null && ch.isOpen();
    }

    public void run() throws IOException {
        Scanner s = new Scanner(System.in);
        System.out.println("Commands: /connect, /nick, /user, /join, /part, /privmsg, /state, /quit, /exit");

        try {
            while (true) {
                System.out.print("> ");

                if (!s.hasNextLine())
                    break;

                String line = s.nextLine();
                String[] parts = line.split(" +", 2);

                if (parts.length == 0) {
                    System.out.println("Invalid command");
                    continue;
                }

                String cmd = parts[0];
                String rest = parts.length == 1 ? null : parts[1];

                if (cmd.equalsIgnoreCase("/connect")) {
                    String[] args = splitArgs(rest, 2);

                    if (args.length < 2) {
                        System.out.println("Usage: /connect <host> <port>");
                        continue;
                    }

                    if (isOpen()) {
                        System.out.println("Already connected!");
                        continue;
                    }

                    String host = args[0];
                    Integer port = parseInt(args[1]);

                    if (port == null || port <= 0) {
                        System.out.println("Port has to be a positive integer");
                        continue;
                    }

                    System.out.println("Connecting to the server");
                    SocketChannel sc = null;

                    try {
                        sc = SocketChannel.open();
                        sc.configureBlocking(true);
                        sc.connect(new InetSocketAddress(host, port.intValue()));
                    }
                    catch (ConnectException e) {
                        System.out.println("Connection failed!");
                        e.printStackTrace();
                        continue;
                    }

                    System.out.println("Connected to " + host + " at " + port);

                    executor = Executors.newCachedThreadPool();
                    ch = new IrcChannel_A(sc);
                    irc = new Irc_Client(ch);
                    state = new ClientState(ch, "choralbot");
                    irc.run(state, executor);
                }
                else if (cmd.equalsIgnoreCase("/nick")) {
                    String[] args = splitArgs(rest, 1);

                    if (args.length < 1) {
                        System.out.println("Usage: /nick <nickname>");
                        continue;
                    }

                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    irc.enqueue(new NickMessage(args[0]));
                }
                else if (cmd.equalsIgnoreCase("/user")) {
                    String[] args = splitArgs(rest, 2);

                    if (args.length < 1) {
                        System.out.println("Usage: /user <username> [<realname>]");
                        continue;
                    }

                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    String username = args[0];
                    String realname = args.length < 2 ? username : args[1];

                    irc.enqueue(new UserMessage(username, realname));
                }
                else if (cmd.equalsIgnoreCase("/join")) {
                    String[] args = splitArgs(rest, 1);

                    if (args.length < 1) {
                        System.out.println("Usage: /join <channel>[,<channel>]...");
                        continue;
                    }

                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    irc.enqueue(new JoinMessage(
                        Arrays.asList(args[0].split(","))));
                }
                else if (cmd.equalsIgnoreCase("/part")) {
                    String[] args = splitArgs(rest, 2);

                    if (args.length < 1) {
                        System.out.println("Usage: /part <channel>[,<channel>]... [<reason>]");
                        continue;
                    }

                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    List<String> channels = Arrays.asList(args[0].split(","));
                    PartMessage m = null;

                    if (args.length < 2) {
                        m = new PartMessage(channels);
                    }
                    else {
                        m = new PartMessage(channels, args[1]);
                    }

                    irc.enqueue(m);
                }
                else if (cmd.equalsIgnoreCase("/privmsg")) {
                    String[] args = splitArgs(rest, 2);

                    if (args.length < 2) {
                        System.out.println("Usage: /privmsg <target>[,<target>]... <text>");
                        continue;
                    }

                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    irc.enqueue(new PrivmsgMessage(
                        Arrays.asList(args[0].split(",")), args[1]));
                }
                else if (cmd.equalsIgnoreCase("/state")) {
                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    System.out.println(gson.toJson(state));
                }
                else if (cmd.equalsIgnoreCase("/quit")) {
                    if (!isOpen()) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    String[] args = splitArgs(rest, 1);

                    irc.enqueue(new QuitMessage(
                        args.length < 1 ? "Bye" : args[0]));
                    state.setQuitRequested();
                    irc.clientQueue().stop();

                    executor.shutdown();
                    executor.awaitTermination(2, TimeUnit.SECONDS);

                    executor = null;
                    irc = null;
                    ch = null;
                    state = null;
                }
                else if (cmd.equalsIgnoreCase("/exit")) {
                    if (isOpen()) {
                        state.setQuitRequested();
                    }

                    break;
                }
                else {
                    System.out.println("Unrecognized command");
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Exiting");

        s.close();

        if (isOpen()) {
            ch.close();
        }

        if (executor != null) {
            executor.shutdown();
            try {
                executor.awaitTermination(2, TimeUnit.SECONDS);
            }
            catch (InterruptedException e2) {
                // Ignore
            }
        }
    }

    public static void main(String[] argv) throws IOException {
        new Client().run();
    }
}
