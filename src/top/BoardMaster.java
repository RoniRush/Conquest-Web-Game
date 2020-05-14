package top;

import java.util.ArrayList;
import java.util.List;

public class BoardMaster {

    private List<BoardAndTitle> bats;

    public BoardMaster()
    {
        bats = new ArrayList<>();
    }

    public synchronized void addBoardManager(String title)
    {
        bats.add(new BoardAndTitle(new BoardManager(),title));
    }

    public synchronized void clearBoardManager(String title) {
        boolean flag = false;
        for (int i=0;i<bats.size() && !flag; i++)
            if (bats.get(i).getTitle().equals(title)) {
                bats.get(i).boardManager.clearData();
                flag = true;
            }
    }

    public synchronized BoardManager findBoardManager(String title)
    {
        for (int i=0;i<bats.size(); i++)
            if (bats.get(i).getTitle().equals(title))
                return bats.get(i).boardManager;
        return null;
    }

    class BoardAndTitle
    {
        private BoardManager boardManager;
        private String title;

        public BoardAndTitle(BoardManager boardManager, String title)
        {
            this.boardManager = boardManager;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
