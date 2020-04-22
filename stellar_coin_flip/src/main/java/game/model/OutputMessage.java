package game.model;

import java.util.Date;

public class OutputMessage {
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    String from;
    String time;
    String text;

    public OutputMessage(String from, String time, String text) {
        this.from = from;
        this.time = time;
        this.text = text;
    }


}
