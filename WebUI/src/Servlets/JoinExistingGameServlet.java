package Servlets;

import Constants.Constants;
import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.User;
import com.google.gson.Gson;
import generated.GameDescriptor;
import top.BigBrother;
import top.GameManager;
import top.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JoinExistingGameServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //response.setContentType("text/html;charset=UTF-8");
        String action = request.getParameter("action");
        String userName = SessionUtils.getUsername(request);
        String title = request.getParameter("gametitle");
        request.getSession(false).setAttribute(Constants.GAME_TITLE, title);
        if (action.equals("joinasplayer")) {
            GameManager game = ServletUtils.getBigBrother(getServletContext()).findGame(title);
            game.setRegistered(game.getRegistered() + 1);
            if (game.getRegistered() == game.getTotalPlayers())
                game.setActiveST("Active");
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            User user = userManager.findUser(userName);
            game.user2Player(user);
            request.getSession(false).setAttribute(Constants.PLAYER_STAT, "player");
            //response.sendRedirect(GAME_ROOM_URL);
        }
        if (action.equals("joinasobserver")) {
            GameManager game = ServletUtils.getBigBrother(getServletContext()).findGame(title);
            game.setNumOfObservers(game.getNumOfObservers() + 1);
            game.getObserversNames().add(userName);
            request.getSession(false).setAttribute(Constants.PLAYER_STAT, "observer");
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
}
