package top;

import generated.GameDescriptor;

import java.util.ArrayList;
import java.util.List;

public class BigBrother {
    private List<GameManager> games;

    public BigBrother()
    {
        games = new ArrayList<>();
    }

    public synchronized void addGame(GameDescriptor gameDes,String creator)
    {
        games.add(new GameManager(gameDes));
        games.get(games.size()-1).setCreator(creator);
        games.get(games.size()-1).createRoundZero(gameDes);
    }

    public synchronized boolean isGameExists(String gameName)
    {
        for (int i=0;i<games.size(); i++)
            if (games.get(i).getGameTitle().equals(gameName))
                return true;
        return false;
    }

    public synchronized List<GameManager> getGames() {
        return games;
    }

    public synchronized GameManager findGame(String title)
    {
        for (int i=0;i<games.size(); i++)
            if (games.get(i).getGameTitle().equals(title))
                return games.get(i);
        return null;
    }
}
