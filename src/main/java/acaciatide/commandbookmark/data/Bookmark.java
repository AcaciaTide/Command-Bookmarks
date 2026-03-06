package acaciatide.commandbookmark.data;

import java.util.UUID;

public class Bookmark {
    private String id;
    private String label;
    private String command;

    public Bookmark() {
        // Gson用
    }

    public Bookmark(String label, String command) {
        this.id = UUID.randomUUID().toString();
        this.label = label != null ? label : "";
        this.command = command != null ? command : "";
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command != null ? command : "";
    }
}
