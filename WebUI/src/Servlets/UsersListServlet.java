package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.User;
import com.google.gson.Gson;
import top.UserManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class UsersListServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String action = request.getParameter("action");
        Gson gson = new Gson();
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        if (action.equals("allusers")) {
            try (PrintWriter out = response.getWriter()) {
                List<User> usersList = userManager.getUsers();
                List<String> usersNames = new ArrayList<>(usersList.size());
                for (int i = 0; i < usersList.size(); i++)
                    usersNames.add(usersList.get(i).getName());
                String json = gson.toJson(usersNames);
                out.println(json);
                out.flush();
            }
        }
        else if (action.equals("getuser"))
        {
            try (PrintWriter out = response.getWriter()) {
                String userName = SessionUtils.getUsername(request);
                String json = gson.toJson(userName);
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
}
