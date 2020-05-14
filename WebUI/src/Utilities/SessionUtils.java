package Utilities;

import Constants.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static Constants.Constants.USERNAME;

public class SessionUtils {

    public static String getUsername (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(USERNAME) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static String getTitle (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.GAME_TITLE) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }

    public static String getUserStat (HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Object sessionAttribute = session != null ? session.getAttribute(Constants.PLAYER_STAT) : null;
        return sessionAttribute != null ? sessionAttribute.toString() : null;
    }


    public static void clearSession (HttpServletRequest request)
    {
        request.getSession().invalidate();
    }

    public static void removeTitleAttribute(HttpServletRequest request)
    {
        request.getSession().removeAttribute(Constants.GAME_TITLE);
    }

    public static void removeUserStatAttribute(HttpServletRequest request)
    {
        request.getSession().removeAttribute(Constants.PLAYER_STAT);
    }
}
