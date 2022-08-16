package choral.examples.irc;

import java.util.ArrayList;
import java.util.List;

public class MessageBuilder {
    private Source source;
    private String command;
    private List<String> params;

    private MessageBuilder() {
        this.source = null;
        this.command = null;
        this.params = new ArrayList<>();
    }

    public static MessageBuilder build() {
        return new MessageBuilder();
    }

    public MessageBuilder fromMessage(Message m) {
        this.source = m.getSource();
        this.command = m.getCommand();
        this.params = m.getParams();
        return this;
    }

    public MessageBuilder source(Source source) {
        this.source = source;
        return this;
    }

    public MessageBuilder command(String command) {
        this.command = command;
        return this;
    }

    public MessageBuilder param(String param) {
        params.add(param);
        return this;
    }

    public MessageBuilder params(List<String> params) {
        this.params = params;
        return this;
    }

    public Message message() {
        return new Message(source, command, params);
    }
}
