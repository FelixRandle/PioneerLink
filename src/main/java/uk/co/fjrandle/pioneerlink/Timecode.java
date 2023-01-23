package uk.co.fjrandle.pioneerlink;

import java.io.Serializable;

public class Timecode implements Serializable {
    private int hours, minutes, seconds, frames;
    private long millis;

    Timecode(long millis) {
        this.setTimeFromMillis(millis);
    }

    public void setTimeFromMillis(long millis) {
        this.millis = millis;
        this.hours = (int) (millis / (1000 * 60 * 60)) % 24;
        this.minutes = (int) (millis / (1000 * 60)) % 60;
        this.seconds = (int) (millis / 1000) % 60;
        this.frames = (int) (Math.abs(millis % 1000)) / (1000 / 30);
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getFrames() {
        return frames;
    }

    public void setFrames(int frames) {
        this.frames = frames;
    }

    public void add(Timecode t) {
        setTimeFromMillis(this.millis + t.millis);
    }

    @Override
    public String toString() {
        return padTime(this.getHours())+":"+padTime(this.getMinutes())+":"+
                padTime(this.getSeconds())+"."+padTime(this.getFrames());
    }

    private String padTime(int inputString) {
        return String.format("%1$" + 2 + "s", inputString).replace(' ', '0');
    }
}
