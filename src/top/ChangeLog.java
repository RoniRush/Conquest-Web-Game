package top;

import bottom.Player;

public class ChangeLog {

    private int serial;
    //private Player owner;
    private String playerName;
    private double[]color;
    private String neutral;


    public ChangeLog(int serial, String playerName, String neutral, double[]color) {
        this.serial = serial;
        this.playerName = playerName;
        this.neutral = neutral;
        this.color=color;
    }
}
