package choral.examples.irc;

import choral.lang.Unit;
import com.google.gson.Gson;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
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

    public static void main(String[] argv) throws IOException {
        Scanner s = new Scanner(System.in);
        Gson gson = new Gson();
        ClientState state = new ClientState("choralbot");
        ExecutorService executor = Executors.newCachedThreadPool();
        Irc_Client irc = null;

        System.out.println("Commands: /connect, /nick, /user, /join, /part, /privmsg, /state, /quit");

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

                    if (irc != null) {
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

                    SocketChannel sc = SocketChannel.open();
                    sc.configureBlocking(true);
                    sc.connect(new InetSocketAddress(host, port.intValue()));

                    System.out.println("Connected to " + host + " at " + port);

                    IrcChannel_A ch = new IrcChannel_A(sc);
                    irc = new Irc_Client(ch, state, Unit.id);
                    irc.run(executor);
                }
                else if (cmd.equalsIgnoreCase("/nick")) {
                    String[] args = splitArgs(rest, 1);

                    if (args.length == 0) {
                        System.out.println("Usage: /nick <nickname>");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    irc.enqueue(new NickMessage(args[0]));
                }
                else if (cmd.equalsIgnoreCase("/user")) {
                    String[] args = rest.split(" +", 2);

                    if (args.length == 0) {
                        System.out.println("Usage: /user <username> [<realname>]");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    String username = args[0];
                    String realname = args.length < 2 ? username : args[1];

                    irc.enqueue(new UserMessage(username, realname));
                }
                else if (cmd.equalsIgnoreCase("/join")) {
                    String[] args = rest.split(" +", 1);

                    if (args.length == 0) {
                        System.out.println("Usage: /join <channel>[,<channel>]...");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    irc.enqueue(new JoinMessage(
                        Arrays.asList(args[0].split(","))));
                }
                else if (cmd.equalsIgnoreCase("/part")) {
                    String[] args = rest.split(" +", 2);

                    if (args.length == 0) {
                        System.out.println("Usage: /part <channel>[,<channel>]... [<reason>]");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
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
                    String[] args = rest.split(" +", 2);

                    if (args.length < 2) {
                        System.out.println("Usage: /privmsg <target>[,<target>]... <text>");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    irc.enqueue(new PrivmsgMessage(
                        Arrays.asList(args[0].split(",")), args[1]));
                }
                else if (cmd.equalsIgnoreCase("/state")) {
                    System.out.println(gson.toJson(state));
                }
                else if (cmd.equalsIgnoreCase("/quit")) {
                    if (irc == null) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    String[] args = rest.split(" +", 1);

                    irc.enqueue(new QuitMessage(
                        args.length == 0 ? "Bye" : args[0]));

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

        System.out.println("Quitting");

        s.close();
        executor.shutdown();
    }
}
