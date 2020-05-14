package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.Player;
import bottom.Slot;
import bottom.Unit;
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
import static top.Round.*;

public class ShopServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String title = SessionUtils.getTitle(request);
        BigBrother bigBrother = ServletUtils.getBigBrother(getServletContext());
        GameManager gameManager = bigBrother.findGame(title);
        Round lastRound = gameManager.getRounds().get(gameManager.getRounds().size()-1);
        List<Unit> model = lastRound.getModel();
        String serial = request.getParameter("serial");
        String direct = request.getParameter("direct");
        String userName = SessionUtils.getUsername(request);
        Player player = lastRound.getPlayerByName(userName);
        Slot slot = lastRound.cellToSlot(Integer.parseInt(serial));
        NotificationsMaster notificationsMaster = ServletUtils.getNotificationsMaster(getServletContext());
        BoardMaster boardMaster = ServletUtils.getBoardMaster(getServletContext());
        NotificationsManager notificationsManager = notificationsMaster.findNotificationsManager(title);
        BoardManager boardManager = boardMaster.findBoardManager(title);
        ShopServlet.InfoForConcludingPopUp pop = new ShopServlet.InfoForConcludingPopUp(serial,direct,player.getName(),player.getColor(),slot.getOccupyingArmy());
        int purchase = 0;
        int might = 0;
        int[] numOfUnitsToBuy = new int[model.size()];
        if (!direct.equals("heal")) {
            int numOfUnit;
            for (int i = 0; i < numOfUnitsToBuy.length; i++)
                numOfUnitsToBuy[i] = 0;
            for (int i = 0; i < numOfUnitsToBuy.length; i++) {
                if ((request.getParameter(model.get(i).getType()) != null)
                        && (!request.getParameter(model.get(i).getType()).equals(""))) {
                    try {
                        numOfUnit = Integer.parseInt(request.getParameter(model.get(i).getType()));
                        if (numOfUnit > 0) {
                            numOfUnitsToBuy[i] = numOfUnit;
                            purchase += (numOfUnit * model.get(i).getCost());
                            might += (numOfUnit * model.get(i).getMaximumMight());
                        } else {
                            player.setMessage("No! ,Is it so hard to choose a positive number?");
                            pop.setStatus("failed");
                        }
                    } catch (NumberFormatException e) {
                        player.setMessage("No! ,Is it so hard to choose a positive number?");
                        pop.setStatus("failed");
                    }
                }
            }
        }

        if(direct.equals("neutral"))
        {
            if (lastRound.purchaseUnits(player, slot, BUY, purchase, might, numOfUnitsToBuy)) {
                String st = player.getName();
                st= st.concat(" is now the proud owner of Territory ");
                st = st.concat(Integer.toString(slot.getSerialNumber()));
                st = st.concat(", All of you better take notice");
                notificationsManager.addNotification(st);
                boardManager.addChangeLog(Integer.parseInt(serial),player);
                pop.setStatus("success");
                pop.setCurrentOccupyingArmy(slot.getOccupyingArmy());
                gameManager.resetCurrPlayer();
                // בוצע בהצלחה, צריך לעבור תור/ סיבוב
            }
            if (might < slot.getMinimumMight()) {
                player.setMessage("That puny army is to weak. You need more might");
                pop.setStatus("failed");
            }
            if (purchase > player.getTotalTuring()) {
                player.setMessage("You can barely afford a slither of that, You need more Turing");
                pop.setStatus("failed");
            }
        }

        if(direct.equals("heal")) {
            if (slot.getHeal()<=player.getTotalTuring())
            {
                lastRound.healUnitsInSlot(player,slot);
                String str = player.getName();
                str = str.concat(" cured ALL his units in territory ");
                str = str.concat(serial);
                str = str.concat(", Now the units have returned to full form!");
                notificationsManager.addNotification(str);
                pop.setStatus("success");
                pop.setCurrentOccupyingArmy(slot.getOccupyingArmy());
                gameManager.resetCurrPlayer();
                // בוצע בהצלחה, צריך לעבור תור/ סיבוב
            } else {
                player.setMessage("SHAME! Are you making fun of me?! Looks like you're too poor to help your units, You truly lead by example, don't you?");
                pop.setStatus("failed");
            }
        }


        if(direct.equals("strengthen")) {
            if (lastRound.purchaseUnits(player, slot, STRENGTHEN, purchase, might, numOfUnitsToBuy)) {
                String str = player.getName();
                str = str.concat(" has strengthened his hold on territory ");
                str = str.concat(serial);
                str = str.concat(", Look at all these shiny new toys");
                notificationsManager.addNotification(str);
                pop.setStatus("success");
                pop.setCurrentOccupyingArmy(slot.getOccupyingArmy());
                gameManager.resetCurrPlayer();
                // בוצע בהצלחה, צריך לעבור תור/ סיבוב
            } else if (purchase == 0) {
                player.setMessage("Are you making fun of me?! Stop wasting my time");
                pop.setStatus("failed");
            }
            else {
                player.setMessage("You can barely afford a slither of that You need more Turing");
                pop.setStatus("failed");
            }
        }

        if(direct.equals("lucky"))
        {
            int report; //report=1: attacker wins. report=2: defender wins
            if (lastRound.purchaseUnits(player, slot, CONQUER, purchase, might, numOfUnitsToBuy)) {
                report = lastRound.attackOccupied(player,slot,might,numOfUnitsToBuy);
                attackAfterReport(boardManager,report, slot, pop, notificationsManager, player);
                gameManager.resetCurrPlayer();

            }
            else if (purchase == 0) {
                player.setMessage("Are you making fun of me?! Stop wasting my time");
                pop.setStatus("failed");
            }
            else {
                player.setMessage("You can barely afford a slither of that You need more Turing");
                pop.setStatus("failed");
            }
        }


        if(direct.equals("orchestrated"))
        {
            int report;
            if (lastRound.purchaseUnits(player, slot, CONQUER, purchase, might, numOfUnitsToBuy))
            {
                report = lastRound.calculatedAttack(player,slot,numOfUnitsToBuy);
                attackAfterReport(boardManager,report, slot, pop, notificationsManager, player);
                gameManager.resetCurrPlayer();
            }
            else if (purchase == 0) {
                player.setMessage("Are you making fun of me?! Stop wasting my time");
                pop.setStatus("failed");
            }
            else {
                player.setMessage("You can barely afford a slither of that You need more Turing");
                pop.setStatus("failed");
            }
        }

        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            String json = gson.toJson(pop);
            out.println(json);
            out.flush();
        }

    }

    public void attackAfterReport(BoardManager boardManager, int report, Slot slot, InfoForConcludingPopUp pop, NotificationsManager notificationsManager, Player player)
    {
        int notification=0;
        int serial = slot.getSerialNumber();
        switch (report){
            case 1:{
                if(slot.getOwner()==null) {
                    notification=1;
                    boardManager.addChangeLog(serial,null);
                    pop.setLuckyResults(1); // attacker wins but loses the territory
                }
                else {
                    notification=2;
                    pop.setLuckyResults(2); // attacker wins and get to hold the territory
                    boardManager.addChangeLog(serial,player);
                }
                break;
            }
            case 2:{
                if(slot.getOwner()==null) {
                    notification=3;
                    boardManager.addChangeLog(serial,null);
                    pop.setLuckyResults(3); // attacker loses and the defender also loses his territory
                }
                else {
                    notification=4;
                    pop.setLuckyResults(4); // attacker loses but the defender get to hold his territory
                }
                break;
            }
        }
        pop.setSpoils(slot.getHeal());
        slot.setHeal(0);
        pop.setPlayerTuring(player.getTotalTuring());
        pop.setStatus("success");
        pop.setCurrentOccupyingArmy(slot.getOccupyingArmy());
        String str = notificationsFromLuckAttack(notification, player, slot);
        notificationsManager.addNotification(str);
    }


    public String notificationsFromLuckAttack(int notification, Player player, Slot slot)
    {
        String str="";
        switch (notification){
            case 1:{
                str = player.getName();
                str = str.concat(" attacked territory number ");
                str = str.concat(slot.getSerialNumber().toString());
                str = str.concat(", And won!! But, There wasn't enough force to hold it.. so it become neutral");
                break;
            }
            case 2:{
                str = player.getName();
                str = str.concat(" attacked territory number ");
                str = str.concat(slot.getSerialNumber().toString());
                str = str.concat(", And won!! Cheer to the proud new owner of the Territory! ");
                break;
            }
            case 3:{
                str = player.getName();
                str = str.concat(" attacked territory number ");
                str = str.concat(slot.getSerialNumber().toString());
                str = str.concat(", And LOST!! But, The Defender lacks sufficient power to hold control of this territory.. so it become neutral ");
                break;
            }
            case 4:{
                str = player.getName();
                str = str.concat(" attacked territory number ");
                str = str.concat(slot.getSerialNumber().toString());
                str = str.concat(", And LOST!! The Defender stands victorious And remains in possession of the territory ");
                break;
            }
        }
        return str;
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

    class InfoForConcludingPopUp
    {
        private String serial;
        private String status;
        private String direct;
        private String playerName;
        private double [] color;
        private List<Unit> currentOccupyingArmy;
        private List<Unit> previousOccupyingArmy;
        private int luckyResults;
        private int spoils;
        private int playerTuring;


        public InfoForConcludingPopUp(String serial, String direct, String playerName,double[]color, List<Unit> previousOccupyingArmy)
        {
            this.serial = serial;
            this.direct = direct;
            this.playerName = playerName;
            this.color = color;
            this.luckyResults=0;
            this.previousOccupyingArmy = new ArrayList<>();
            for(int i=0; i<previousOccupyingArmy.size(); i++)
            {
                this.previousOccupyingArmy.add(previousOccupyingArmy.get(i));
            }
            this.currentOccupyingArmy=null;
            this.status=null;
            this.spoils=0;
            this.playerTuring=0;
        }

        public void setSpoils(int spoils) {
            this.spoils = spoils;
        }

        public void setPlayerTuring(int playerTuring) {
            this.playerTuring = playerTuring;
        }

        public void setLuckyResults(int luckyResults) {
            this.luckyResults = luckyResults;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setCurrentOccupyingArmy(List<Unit> currentOccupyingArmy) {
            this.currentOccupyingArmy = new ArrayList<>();
            for(int i=0; i<currentOccupyingArmy.size(); i++)
            {
                this.currentOccupyingArmy.add(currentOccupyingArmy.get(i));
            }
        }
    }
}