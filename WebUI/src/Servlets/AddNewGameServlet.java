package Servlets;

import Utilities.ServletUtils;
import Utilities.SessionUtils;
import bottom.User;
import com.google.gson.Gson;
import generated.GameDescriptor;
import top.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;

public class AddNewGameServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //returning JSON objects, not HTML
        response.setContentType("application/json");
        String gameContent = request.getParameter("file");
        String username = SessionUtils.getUsername(request);
        String gameTitle="";
        CreatorAndTitle cat = new CreatorAndTitle(username,gameTitle);
        GameDescriptor gameDes = createGameDescriptor(gameContent,cat);
        if(gameDes!=null && username!=null)
        {
            BigBrother bigBrother = ServletUtils.getBigBrother(getServletContext());
            BoardMaster boardMaster = ServletUtils.getBoardMaster(getServletContext());
            NotificationsMaster notificationsMaster = ServletUtils.getNotificationsMaster(getServletContext());
            gameTitle = gameDes.getDynamicPlayers().getGameTitle();
            cat.setTitle(gameTitle);
            if (!bigBrother.isGameExists(gameTitle)) {
                bigBrother.addGame(gameDes, username);
                boardMaster.addBoardManager(gameTitle);
                notificationsMaster.addNotificationsManager(gameTitle);
            }
            else
                cat.setError("Game Title is already taken");
        }
        if(username==null)
            cat.setError("User/Session mismatch");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String json = gson.toJson(cat);
            out.println(json);
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public GameDescriptor createGameDescriptor(String gameContent,CreatorAndTitle cat)
    {
        GameDescriptor gameDes = loadXML(gameContent);
        if(gameDes==null)
            cat.setError("JaxB error");
        else
        {
            if(checkXmlInfo(gameDes,cat))
                return gameDes;
        }
        return null;
    }

    public boolean checkXmlInfo(GameDescriptor gameDes,CreatorAndTitle cat)
    {
        int report = GameManager.checkXmlInfo(gameDes);
        switch (report)
        {
            case 1: {
                cat.setError("Illegal game info. Columns must be between 3-30");
                break;
            }
            case 2:
            {
                cat.setError("Illegal game info. Rows must be between 2-30");
                break;
            }
            case 3:
            {
                cat.setError("Illegal game info. File must supply a full definition for each Territory or supply default values");
                break;
            }
            case 4:
            {
                cat.setError("Illegal game info. Territory id number must be between 0-"+gameDes.getGame().getBoard().getRows().intValue()*
                        gameDes.getGame().getBoard().getColumns().intValue());
                break;
            }
            case 5:
            {
                cat.setError("Illegal game info. Territory id must be unique and cannot repeat");
                break;
            }
            case 6:
            {
                cat.setError("Illegal game info. Number of players must be between 2-4");
                break;
            }
            case 7:
            {
                cat.setError("Illegal game info. Game must have a Title");
                break;
            }
            case 8:
            {
                cat.setError("Illegal game info. Unit rank must be between 1-"+gameDes.getGame().getArmy().getUnit().size());
                break;
            }
            case 9:
            {
                cat.setError("Illegal game info. Unit name must be unique and cannot repeat");
                break;
            }
            case 10:
            {
                cat.setError("Illegal game info. Unit rank must be unique and cannot repeat");
                break;
            }
            case 11:
                return true;
        }
        return false;
    }

    public GameDescriptor loadXML(String gameContent)
    {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(GameDescriptor.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader sr = new StringReader(gameContent);
            return (GameDescriptor) jaxbUnmarshaller.unmarshal(sr);
        }
        catch (JAXBException e)
        {
            return null;
        }
    }

    class CreatorAndTitle{
        private String creator;
        private String title;
        private String errorM;

        public CreatorAndTitle(String creator, String title)
        {
            this.creator = creator;
            this.title = title;
            errorM="";
        }

        public void setError(String error) {
            this.errorM = error;
        }

        public void setTitle(String title) {
            this.title = title;
        }
    }
}
