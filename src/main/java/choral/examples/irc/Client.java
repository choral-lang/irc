package choral.examples.irc;

import choral.lang.Unit;
import choral.runtime.Media.SocketByteChannel;
import choral.runtime.SerializerChannel.SerializerChannel_A;
import choral.runtime.Serializers.JSONSerializer;
import choral.runtime.WrapperByteChannel.WrapperByteChannel_A;
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

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        ClientState state = new ClientState("choralbot");
        ExecutorService executor = Executors.newCachedThreadPool();
        IrcChannel_A ch = null;
        Irc_Client irc = null;

        System.out.println("Commands: /connect, /nick, /user, /quit");

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

                    SocketByteChannel sbc = SocketByteChannel.connect(
                        host, port.intValue());

                    if (sbc == null) {
                        System.out.println("Failed to connect!");
                        continue;
                    }

                    ch = new IrcChannel_A(sbc);

                    System.out.println("Connected to " + host + " at " + port);

                    irc = new Irc_Client(ch, state, Unit.id);
                    Irc.runClient(irc, executor);
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

                    irc.addClientEvent(new ClientNickEvent(parts[1]));
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

                    irc.addClientEvent(new ClientUserEvent(username, realname));
                }
                else if (cmd.equalsIgnoreCase("/quit")) {
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
        // TODO: Disconnect properly.

        s.close();
        executor.shutdown();
    }
}
