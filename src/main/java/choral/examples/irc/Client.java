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

    public static void main(String[] args) throws IOException {
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
                String[] parts = line.split(" +");

                if (parts.length == 0) {
                    System.out.println("Invalid command");
                    continue;
                }

                String cmd = parts[0];

                if (cmd.equalsIgnoreCase("/connect")) {
                    if (parts.length - 1 < 2) {
                        System.out.println("Usage: /connect <host> <port>");
                        continue;
                    }

                    if (irc != null) {
                        System.out.println("Already connected!");
                        continue;
                    }

                    String host = parts[1];
                    Integer port = parseInt(parts[2]);

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
                    if (parts.length - 1 < 1) {
                        System.out.println("Usage: /nick <nickname>");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    irc.addClientMessage(new NickMessage(parts[1]));
                }
                else if (cmd.equalsIgnoreCase("/user")) {
                    if (parts.length - 1 == 0) {
                        System.out.println("Usage: /user <username> [<realname>]");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    String username = parts[1];
                    String realname = parts.length - 1 < 2 ? username : parts[2];

                    irc.addClientMessage(new UserMessage(username, realname));
                }
                else if (cmd.equalsIgnoreCase("/join")) {
                    if (parts.length - 1 < 1) {
                        System.out.println("Usage: /join <channel>[,<channel>]...");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    irc.addClientMessage(new JoinMessage(
                        Arrays.asList(parts[1].split(","))));
                }
                else if (cmd.equalsIgnoreCase("/part")) {
                    if (parts.length - 1 < 1) {
                        System.out.println("Usage: /part <channel>[,<channel>]... [<reason>]");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    List<String> channels = Arrays.asList(parts[1].split(","));
                    PartMessage m = null;

                    if (parts.length - 1 < 2) {
                        m = new PartMessage(channels);
                    }
                    else {
                        m = new PartMessage(channels, parts[2]);
                    }

                    irc.addClientMessage(m);
                }
                else if (cmd.equalsIgnoreCase("/privmsg")) {
                    if (parts.length - 1 < 2) {
                        System.out.println("Usage: /privmsg <target>[,<target>]... <text>");
                        continue;
                    }

                    if (irc == null) {
                        System.out.println("Connect first");
                        continue;
                    }

                    irc.addClientMessage(new PrivmsgMessage(
                        Arrays.asList(parts[1].split(",")), parts[2]));
                }
                else if (cmd.equalsIgnoreCase("/state")) {
                    System.out.println(gson.toJson(state));
                }
                else if (cmd.equalsIgnoreCase("/quit")) {
                    if (irc == null) {
                        System.out.println("Connect first!");
                        continue;
                    }

                    String reason = parts.length - 1 < 1 ? "Bye" : parts[1];

                    irc.addClientMessage(new QuitMessage(reason));
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
