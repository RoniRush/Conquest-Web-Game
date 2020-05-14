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
import java.util.List;


public class PlayerActionServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String serialID = request.getParameter("serial");
        String action = request.getParameter("action");
        int serial = Integer.parseInt(serialID);
        String gameTitle = SessionUtils.getTitle(request);
        String userName = SessionUtils.getUsername(request);
        BigBrother bigBrother= ServletUtils.getBigBrother(getServletContext());
        GameManager gameManager = bigBrother.findGame(gameTitle);
        Round lastRound = gameManager.getRounds().get(gameManager.getRounds().size()-1);
        Player player = lastRound.getPlayerByName(userName);
        if (action.equals("getDirection"))
            getDirection(serial,player,lastRound,response);

    }

    protected void getDirection(int serial, Player player, Round lastRound,HttpServletResponse response)
            throws ServletException, IOException
    {
        ErrorAndDirect ead = new ErrorAndDirect(serial,lastRound.getModel());
        Slot slot=null;
        if (player.getConqueredSize() !=0) {
            if (!lastRound.isThisActionPossible(serial, player))
                ead.setError("Don't you know the rules?!, You can only move 1 slot vertically or horizontally From a territory you occupy");
            else
                slot = lastRound.cellToSlot(serial);
        }
        else
        {
            if(!lastRound.noNeutral())
            {
                slot = lastRound.cellToSlot(serial);
                if (slot.getOwner()!=null)
                {
                    slot=null;
                    ead.setError("Don't you know the rules?!, You can only select a neutral territory When you have no land of your own");
                }
            }
            else
            {
                ead.setError("Looks like the entire board is occupied.. and not by you, Your only hope is to wait and see if your competitors Display the same ineptitude");
            }
        }
        if (slot != null) {
            Player owner = slot.getOwner();
            if (owner == null)
                ead.setDirect("neutral");
            else if (owner.getId() == player.getId()) {
                ead.setDirect("owned");
                ead.setHealCost(slot.getHeal());
                ead.setOccupyingArmy(slot.getOccupyingArmy());
            }
            else {
                ead.setDirect("foreign");
                ead.setAttackerTuring(player.getTotalTuring());
                ead.setMinMight(slot.getMinimumMight());
            }
        }
        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            String json = gson.toJson(ead);
            out.println(json);
            out.flush();
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


    class ErrorAndDirect {
        private String error;
        private String direct;
        private int serial;
        private List<Unit> model;
        private List<Unit> occupyingArmy;
        private int healCost;
        private int attackerTuring;
        private int minMight;

        public ErrorAndDirect(int serial,List<Unit> model)
        {
            error="";
            direct="";
            this.serial= serial;
            this.model = model;
            occupyingArmy=null;
            attackerTuring=0;
            healCost=0;
            minMight=0;
        }

        public void setMinMight(int minMight) {
            this.minMight = minMight;
        }

        public void setAttackerTuring(int attackerTuring) {
            this.attackerTuring = attackerTuring;
        }

        public void setHealCost(int healCost) {
            this.healCost = healCost;
        }

        public void setOccupyingArmy(List<Unit> occupyingArmy) {
            this.occupyingArmy = occupyingArmy;
        }

        public void setError(String error) {
            this.error = error;
        }

        public void setDirect(String direct) {
            this.direct = direct;
        }
    }

}
