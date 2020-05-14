package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.Player;
import bottom.Slot;
import com.google.gson.Gson;
import top.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GameFlowServlet extends HttpServlet {


    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        String userName = SessionUtils.getUsername(request);
        String title = SessionUtils.getTitle(request);
        BigBrother bigBrother= ServletUtils.getBigBrother(getServletContext());
        GameManager gameManager = bigBrother.findGame(title);
        Round round = gameManager.getRounds().get(gameManager.getRounds().size()-1);
        BoardMaster boardMaster = ServletUtils.getBoardMaster(getServletContext());
        BoardManager boardManager = boardMaster.findBoardManager(title);
        NotificationsMaster notificationsMaster = ServletUtils.getNotificationsMaster(getServletContext());
        NotificationsManager notificationsManager = notificationsMaster.findNotificationsManager(title);
        String userStat= SessionUtils.getUserStat(request);
        if (action.equals("startGame"))
        {
            if(userStat.equals("player")) {
                synchronized (getServletContext()) {
                    if (gameManager.getRounds().size() == 1) {
                        startRoundUpdates(gameManager, boardManager, notificationsManager);
                        gameManager.setTurnIndicator(0);
                        gameManager.setCurrPlayer();
                    }
                }
            }
        }
        if (action.equals("turnEnd"))
        {
            String status = request.getParameter("status");
            if (status.equals("passturn"))
            {
                String message ="";
                message = message.concat(userName);
                message = message.concat(" has done absolutely nothing. how productive...");
                notificationsManager.addNotification(message);
            }
            moveToNextRoundOrTurn(gameManager,boardManager,notificationsManager);
        }
        if(action.equals("checkFlow"))
        {
            response.setContentType("application/json");
            if(userStat.equals("player")) {
                Player player = round.getPlayerByName(userName);
                if (gameManager.getNumOfRound() > gameManager.getMaxRounds() || gameManager.isGameEnded())// last Round: game ended
                    gameEndSteps(gameManager, boardMaster, title, notificationsMaster, response);
                else {
                    MyTurn myTurn;
                    if (gameManager.getCurrPlayer() != null) {
                        if (gameManager.getCurrPlayer().getName().equals(userName)) {
                            if (!player.isInTurn()) {
                                player.setMessage("It's your turn! play wisely");
                                player.setInTurn(true);
                            }
                            myTurn = new MyTurn("myTurn");
                            myTurn.setBoardRowsColumns(round.getRows(), round.getColumns(), round.getBoard());
                        } else {
                            myTurn = new MyTurn("otherTurn");
                            myTurn.setBoardRowsColumns(round.getRows(), round.getColumns(), round.getBoard());
                            player.setInTurn(false);
                        }
                    } else {
                        myTurn = new MyTurn("otherTurn");
                        myTurn.setBoardRowsColumns(round.getRows(), round.getColumns(), round.getBoard());
                        player.setInTurn(false);
                    }
                    try (PrintWriter out = response.getWriter()) {
                        Gson gson = new Gson();
                        String json = gson.toJson(myTurn);
                        out.println(json);
                        out.flush();
                    }
                }
            }
            else
            {
                if (gameManager.getNumOfRound() > gameManager.getMaxRounds() || gameManager.isGameEnded())// last Round: game ended
                    gameEndSteps(gameManager, boardMaster, title, notificationsMaster, response);
                else {
                    MyTurn myTurn = new MyTurn("otherTurn");
                    myTurn.setBoardRowsColumns(round.getRows(), round.getColumns(), round.getBoard());
                    try (PrintWriter out = response.getWriter()) {
                        Gson gson = new Gson();
                        String json = gson.toJson(myTurn);
                        out.println(json);
                        out.flush();
                    }
                }
            }
        }
        if(action.equals("backToLobby"))
        {
            SessionUtils.removeTitleAttribute(request);
            SessionUtils.removeUserStatAttribute(request);
            if(gameManager.getRegistered()!=0)
                gameManager.setRegistered(gameManager.getRegistered()-1);
        }
        if(action.equals("surrender"))
        {
            if(userStat.equals("player")) {
                boolean flag = false;
                for (int j = 0; j < round.getPlayers().size() && !flag; j++) {
                    if (round.getPlayers().get(j).getName().equals(userName)) {
                        Player player = round.getPlayers().get(j);
                        for (int i = 0; i < player.getConquered().size(); i++) {
                            player.getConquered().get(i).setOwner(null);
                            player.getConquered().get(i).clearOccupyingArmy();
                            boardManager.addChangeLog(player.getConquered().get(i).getSerialNumber(), null);
                        }
                        round.getPlayers().remove(j);
                        round.setNumOfPlayers(round.getNumOfPlayers() - 1);
                        flag = true;
                    }
                }
                if (round.getNumOfPlayers() == 1 && gameManager.getActiveST().equals("Active")) { // after surrender remain only one user
                    gameManager.setGameEnded(true);
                } else {
                    if (gameManager.getCurrPlayer() != null) {
                        if (gameManager.getCurrPlayer().getName().equals(userName)) {
                            moveToNextRoundOrTurn(gameManager, boardManager, notificationsManager);
                        }
                    }
                }
                gameManager.setRegistered(gameManager.getRegistered() - 1);
                SessionUtils.removeTitleAttribute(request);
                SessionUtils.removeUserStatAttribute(request);
                String surrendered = "OH My! Looks like ";
                surrendered = surrendered.concat(userName);
                String str = " surrendered. I guess this was all too much for the weakling";
                surrendered = surrendered.concat(str);
                notificationsManager.addNotification(surrendered);
            }
            else
            {
                gameManager.setNumOfObservers(gameManager.getNumOfObservers()-1);
                SessionUtils.removeTitleAttribute(request);
                SessionUtils.removeUserStatAttribute(request);
                boolean flag=false;
                for(int i=0 ; i<gameManager.getObserversNames().size() &&!flag; i++){
                    if (userName.equals(gameManager.getObserversNames().get(i))) {
                        gameManager.getObserversNames().remove(i);
                        flag = true;
                    }
                }
            }
        }
    }

    public void gameEndSteps(GameManager gameManager, BoardMaster boardMaster, String title, NotificationsMaster notificationsMaster, HttpServletResponse response)
            throws ServletException, IOException
    {
        List<String> names= whoWon(gameManager);
        if(gameManager.getRegistered()==1)
            resetGame(gameManager, boardMaster, title, notificationsMaster);
        GameEndAndWinners gaw = new GameEndAndWinners("gameEnded", names);
        gameManager.setGameEnded(false);
        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            String json = gson.toJson(gaw);
            out.println(json);
            out.flush();
        }
    }

    public void moveToNextRoundOrTurn(GameManager gameManager, BoardManager boardManager, NotificationsManager notificationsManager)
    {
        if(gameManager.getTurnIndicator() >= gameManager.getNumOfPlayers() - 1)// round ended
        {
            gameManager.setTurnIndicator(0);
            gameManager.setCurrPlayer();
            gameManager.setNumOfRound(gameManager.getNumOfRound() + 1);
            if(gameManager.getNumOfRound()<=gameManager.getMaxRounds())// if not last Round: game ended
                startRoundUpdates(gameManager, boardManager, notificationsManager);
        }
        else
        {
            gameManager.setTurnIndicator(gameManager.getTurnIndicator() + 1);
            gameManager.setCurrPlayer();
        }
    }

    public void resetGame(GameManager gameManager, BoardMaster boardMaster, String title, NotificationsMaster notificationsMaster)
    {
        boardMaster.clearBoardManager(title);
        notificationsMaster.clearNotificationsManager(title);
        gameManager.resetGameManager();
    }


    public List<String> whoWon(GameManager gameManager)
    {
        List<Player> winners = gameManager.whoWon();
        List<String> winnersNames = new ArrayList<>();
        for (int i=0; i<winners.size(); i++)
            winnersNames.add(winners.get(i).getName());
        return winnersNames;
    }

    public void startRoundUpdates(GameManager gameManager, BoardManager boardManager, NotificationsManager notificationsManager)
    {

        if (gameManager.createNewRound()) {
            Round newRound= gameManager.getRounds().get(gameManager.getRounds().size()-1);
            String updates = "This is a fresh new ROUND! take a look at your new Turing amount :)";
            String territoriesUpdate="";
            List<Slot> deadAndBuried;
            List<Slot> totalDead= new ArrayList<>();
            for (int i = 0; i < newRound.getNumOfPlayers(); i++) { // calculate become neutral territories
                deadAndBuried = newRound.calcFatigue(i);
                if (deadAndBuried.size() > 0)
                    totalDead.addAll(deadAndBuried);
            }
            //add alert to all players of new round and become neutral territories
            if (totalDead.size() > 0) {
                String dead = "The following Territories have turned neutral:\n";
                for (int j = 0; j < totalDead.size(); j++) {
                    if (!dead.equals("The following Territories have turned neutral:\n"))
                        dead = dead.concat(", ");
                    dead = dead.concat(totalDead.get(j).getSerialNumber().toString());
                    boardManager.addChangeLog(totalDead.get(j).getSerialNumber(), null);
                }
                dead = dead.concat("\n");
                dead = dead.concat("tsk-tsk, All those poor dead soldiers, sigh");
                territoriesUpdate = dead;
            }
            // calculate healPerSlot for each conquered slot on board
            for(int i=0;i<newRound.getPlayers().size();i++)
            {
                List<Slot> conquered = newRound.getPlayers().get(i).getConquered();
                for(int j=0;j<conquered.size();j++)
                    conquered.get(j).calcHealPerSlot();
            }
            updates= updates.concat("\n");
            updates = updates.concat(territoriesUpdate);
            notificationsManager.addNotification(updates);

            // add rewards for each player
            Player player;
            for (int i = 0; i < newRound.getNumOfPlayers(); i++) {
                player = newRound.getPlayers().get(i);
                player.addRewards();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    class GameEndAndWinners
    {
        private String status;
        private String winners="";

        public GameEndAndWinners(String status, List<String> winners) {
            this.status = status;
            this.winners = this.winners.concat(winners.get(0));
            for(int i=1; i<winners.size(); i++)
            {
                this.winners = this.winners.concat(", ");
                this.winners = this.winners.concat(winners.get(i));
            }
        }
    }

    class MyTurn
    {
        private String status;
        private int [][]board;
        private int rows;
        private int columns;

        public MyTurn(String status){
            this.status= status;
            this.rows = 0;
            this.columns = 0;
            this.board = null;
        }

        public void setBoardRowsColumns(int rows, int columns, Slot[][] board)
        {
            this.rows=rows;
            this.columns=columns;
            this.board = new int [rows][columns];
            for(int i=0; i<rows; i++)
            {
                for( int j=0; j<columns; j++)
                {
                    this.board[i][j]=board[i][j].getSerialNumber();
                }
            }
        }

    }
}
