package top;

import bottom.Player;

import java.util.ArrayList;
import java.util.List;

public class BoardManager {

    private List<ChangeLog> changeLogs;
    private int boardManagerVersion;

    public BoardManager()
    {
        changeLogs = new ArrayList<>();
        boardManagerVersion = 0;
    }

    public synchronized void clearData()
    {
        changeLogs.clear();
        boardManagerVersion = 0;
    }

    public synchronized int getBoardManagerVersion() {
        return boardManagerVersion;
    }

    public synchronized void addChangeLog(int serial, Player owner)
    {
        String neutral;
        String playerName="";
        double []color=null;
        if(owner==null) {
            neutral = "yes";
        }
        else {
            neutral = "no";
            playerName = owner.getName();
            color=owner.getColor();
        }
        changeLogs.add(new ChangeLog(serial,playerName, neutral, color));
        boardManagerVersion++;
    }

    public synchronized List<ChangeLog> getChangeLogs(int fromIndex){
        if (fromIndex < 0 || fromIndex > changeLogs.size()) {
            fromIndex = 0;
        }
        return changeLogs.subList(fromIndex, changeLogs.size());
    }

}
