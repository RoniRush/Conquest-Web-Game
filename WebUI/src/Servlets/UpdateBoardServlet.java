package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import com.google.gson.Gson;
import top.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class UpdateBoardServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String playerVersion = request.getParameter("version");
        String gameTitle= SessionUtils.getTitle(request);
        BoardMaster boardMaster = ServletUtils.getBoardMaster(getServletContext());
        BoardManager boardManager = boardMaster.findBoardManager(gameTitle);
        int gameVersion;
        List<ChangeLog> changeLogs;
        synchronized (getServletContext())
        {
            gameVersion = boardManager.getBoardManagerVersion();
            changeLogs = boardManager.getChangeLogs(Integer.parseInt(playerVersion));
        }
        ChangeLogsAndVersion cav = new ChangeLogsAndVersion(changeLogs, gameVersion);
        try (PrintWriter out = response.getWriter())
        {
            Gson gson = new Gson();
            String json = gson.toJson(cav);
            out.print(json);
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


    class ChangeLogsAndVersion {
        private List<ChangeLog> changeLogs;
        private int version;

        public ChangeLogsAndVersion(List<ChangeLog> changeLog, int version)
        {
            this.changeLogs = changeLog;
            this.version=version;
        }
    }

}
