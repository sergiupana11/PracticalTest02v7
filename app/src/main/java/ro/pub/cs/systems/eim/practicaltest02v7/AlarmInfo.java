package ro.pub.cs.systems.eim.practicaltest02v7;

public class AlarmInfo {
    private final int hour;
    private final int minute;

    public AlarmInfo(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
    }

    public int getHour() {
        return hour;
    }

    public int getMinute() {
        return minute;
    }
}
