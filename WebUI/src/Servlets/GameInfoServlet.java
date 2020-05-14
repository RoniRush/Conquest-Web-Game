package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.Player;
import bottom.User;
import com.google.gson.Gson;
import top.BigBrother;
import top.GameManager;
import top.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GameInfoServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String action = request.getParameter("action");
        Gson gson = new Gson();
        BigBrother bigBrother = ServletUtils.getBigBrother(getServletContext());
        String title = SessionUtils.getTitle(request);
        GameManager game = bigBrother.findGame(title);
        if (action.equals("allplayers")) {
            try (PrintWriter out = response.getWriter()) {
                List<Player> players = game.getRounds().get(game.getRounds().size()-1).getPlayers();
                List<PlayerInfo> playerInfos = new ArrayList<>();
                for(int i=0; i<players.size(); i++)
                {
                    Player player= players.get(i);
                    playerInfos.add(new PlayerInfo(player.getColor(),player.getTotalTuring(), player.getName()));
                }
                String json = gson.toJson(playerInfos);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("getplayer"))
        {
            try (PrintWriter out = response.getWriter()) {
                String userName = SessionUtils.getUsername(request);
                PlayerAndTitle pat = new PlayerAndTitle(userName,title);
                String json = gson.toJson(pat);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("playerAndRound")) {
            try (PrintWriter out = response.getWriter()) {
                String name;
                int round = game.getNumOfRound();
                int max = game.getMaxRounds();
                Player player = game.getCurrPlayer();
                if(player==null)
                    name = "";
                else
                    name = player.getName();
                CurrentPAndRound car = new CurrentPAndRound(name,round,max);
                String json = gson.toJson(car);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("getStatus"))
        {
            try (PrintWriter out = response.getWriter()) {
                String stat = game.getActiveST();
                String userStat = SessionUtils.getUserStat(request);
                GameStatAndUserStat sas= new GameStatAndUserStat(stat, userStat);
                String json = gson.toJson(sas);
                out.println(json);
                out.flush();
            }
        }
        else if(action.equals("allobservers"))
        {
            try (PrintWriter out = response.getWriter()) {
                String json = gson.toJson(game.getObserversNames());
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

    class PlayerAndTitle{
        private String playerName;
        private String currentGameTitle;

        public PlayerAndTitle(String player, String title)
        {
            this.playerName = player;
            this.currentGameTitle = title;
        }
    }

    class CurrentPAndRound{
        private String currentPlayer;
        private String currentRound;
        private String maxRound;

        public CurrentPAndRound(String player, int round, int max)
        {
            this.currentPlayer = player;
            currentRound = Integer.toString(round);
            maxRound = Integer.toString(max);
        }
    }

    class PlayerInfo{
        private double[] color;
        private int tempTotalTuring;
        private String name;

        public PlayerInfo(double[] color, int tempTotalTuring, String name) {
            this.color = color;
            this.tempTotalTuring = tempTotalTuring;
            this.name = name;
        }
    }

    class GameStatAndUserStat{
        private String stat;
        private String userStat;

        public GameStatAndUserStat(String stat, String userStat) {
            this.stat = stat;
            this.userStat = userStat;
        }
    }
}
