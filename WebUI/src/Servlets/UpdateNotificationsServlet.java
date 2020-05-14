package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.Player;
import com.google.gson.Gson;
import top.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class UpdateNotificationsServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String playerVersion = request.getParameter("version");
        String gameTitle= SessionUtils.getTitle(request);
        String userName = SessionUtils.getUsername(request);
        String userStat = SessionUtils.getUserStat(request);
        BigBrother bigBrother = ServletUtils.getBigBrother(getServletContext());
        GameManager gameManager = bigBrother.findGame(gameTitle);
        Round round = gameManager.getRounds().get(gameManager.getRounds().size() - 1);
        NotificationsMaster notificationsMaster = ServletUtils.getNotificationsMaster(getServletContext());
        NotificationsManager notificationsManager = notificationsMaster.findNotificationsManager(gameTitle);
        int gameVersion;
        List<String> notifications;
        synchronized (getServletContext()) {
            gameVersion = notificationsManager.getNotificationsManagerVersion();
            notifications = notificationsManager.getNotifications(Integer.parseInt(playerVersion));
        }

        if(userStat.equals("player")) {
            Player player = round.getPlayerByName(userName);
            UpdateNotificationsServlet.NotificationsAndVersion nav = new UpdateNotificationsServlet.NotificationsAndVersion(notifications, gameVersion, player.getMessage());
            player.setMessage("");
            nav.setUserStat("player");
            try (PrintWriter out = response.getWriter()) {
                Gson gson = new Gson();
                String json = gson.toJson(nav);
                out.println(json);
                out.flush();
            }
        }
        else
        {
            UpdateNotificationsServlet.NotificationsAndVersion nav = new UpdateNotificationsServlet.NotificationsAndVersion(notifications,gameVersion, "");
            nav.setUserStat("observer");
            try (PrintWriter out = response.getWriter()) {
                Gson gson = new Gson();
                String json = gson.toJson(nav);
                out.println(json);
                out.flush();
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


    class NotificationsAndVersion {
        private List<String> notifications;
        private String playerMessage;
        private int version;
        private String userStat="";

        public NotificationsAndVersion(List<String> notifications, int version, String playerMessage)
        {
            this.notifications = notifications;
            this.version=version;
            this.playerMessage = playerMessage;
        }

        public void setUserStat(String userStat) {
            this.userStat = userStat;
        }
    }
}
