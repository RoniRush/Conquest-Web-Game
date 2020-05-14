package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.Player;
import bottom.Slot;
import bottom.Unit;
import com.google.gson.Gson;
import generated.GameDescriptor;
import top.BigBrother;
import top.GameManager;
import top.Round;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GamesListServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String action = request.getParameter("action");
        Gson gson = new Gson();
        BigBrother bigBrother = ServletUtils.getBigBrother(getServletContext());
        if(action.equals("refresh")) {
            try (PrintWriter out = response.getWriter()) {
                List<GameManager> games = bigBrother.getGames();
                List<GameInfo> gameInfos = new ArrayList<>();
                for(int i=0; i<games.size();i++)
                {
                    GameManager game = games.get(i);
                    gameInfos.add(new GameInfo(game.getNumOfObservers(), game.getGameTitle(), game.getActiveST(), game.getCreator(),
                    game.getRegistered(), game.getTotalPlayers()));
                }
                String json = gson.toJson(gameInfos);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("showUnits"))
        {
            try (PrintWriter out = response.getWriter()) {
                String title = request.getParameter("gametitle");
                GameManager game;
                if(!title.equals(""))
                    game= bigBrother.findGame(title);
                else
                    game= bigBrother.findGame(SessionUtils.getTitle(request));
                List<Unit> model = game.getRounds().get(0).getModel();
                String json = gson.toJson(model);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("showBoard")){
            try (PrintWriter out = response.getWriter()) {
                String page = request.getParameter("page");
                String title;
                if(page.equals("two"))
                    title = request.getParameter("gametitle");
                else
                    title= SessionUtils.getTitle(request);
                GameManager game= bigBrother.findGame(title);
                Round round = game.getRounds().get(0);
                BoardInfo boardInfo = new BoardInfo(round.getRows(),round.getColumns(), round.getBoard());
                String json = gson.toJson(boardInfo);
                out.println(json);
                out.flush();
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    class GameInfo{
        private String gameTitle;
        private String activeST;
        private String creator;
        private int registered;
        private int totalPlayers;
        private int observers;

        public GameInfo(int observers, String title, String activeST, String creator, int registered, int totalPlayers) {
            this.gameTitle = title;
            this.activeST = activeST;
            this.creator = creator;
            this.registered = registered;
            this.totalPlayers = totalPlayers;
            this.observers = observers;
        }
    }

    class TileInfo{
        private int serialNumber;
        private int minimumMight;
        private int roundRewards;

        public TileInfo(int serialNumber, int minimumMight, int roundRewards) {
            this.serialNumber = serialNumber;
            this.minimumMight = minimumMight;
            this.roundRewards = roundRewards;
        }
    }

    class BoardInfo{
        private TileInfo [][] board;
        private int rows;
        private int columns;

        public BoardInfo(int rows, int columns, Slot[][]board) {
            this.rows = rows;
            this.columns = columns;
            this.board = new TileInfo[rows][columns];
            for(int i=0; i<rows; i++)
            {
                for(int j=0; j<columns; j++)
                {
                    Slot slot = board[i][j];
                    this.board[i][j]= new TileInfo(slot.getSerialNumber(),
                            slot.getMinimumMight(), slot.getRoundRewards());
                }
            }
        }
    }
}
