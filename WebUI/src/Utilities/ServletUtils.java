package Utilities;

import generated.Board;
import top.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import static Constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

    private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
    private static final String BIG_BROTHER_ATTRIBUTE_NAME = "bigBrother";
    private static final String BOARD_MASTER_ATTRIBUTE_NAME = "boardMaster";
    private static final String NOTIFICATIONS_MASTER_ATTRIBUTE_NAME = "notificationsManager";

    /*
    Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
    the actual fetch of them is remained un-synchronized for performance POV
     */
    private static final Object userManagerLock = new Object();
    private static final Object bigBrotherLock = new Object();
    private static final Object boardMasterLock = new Object();
    private static final Object notificationsMasterLock = new Object();

    public static UserManager getUserManager(ServletContext servletContext) {

        synchronized (userManagerLock) {
            if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
            }
        }
        return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
    }

    public static BigBrother getBigBrother(ServletContext servletContext) {

        synchronized (bigBrotherLock) {
            if (servletContext.getAttribute(BIG_BROTHER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(BIG_BROTHER_ATTRIBUTE_NAME, new BigBrother());
            }
        }
        return (BigBrother) servletContext.getAttribute(BIG_BROTHER_ATTRIBUTE_NAME);
    }

    public static BoardMaster getBoardMaster(ServletContext servletContext) {

        synchronized (boardMasterLock) {
            if (servletContext.getAttribute(BOARD_MASTER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(BOARD_MASTER_ATTRIBUTE_NAME, new BoardMaster());
            }
        }
        return (BoardMaster) servletContext.getAttribute(BOARD_MASTER_ATTRIBUTE_NAME);
    }

    public static NotificationsMaster getNotificationsMaster(ServletContext servletContext) {

        synchronized (notificationsMasterLock) {
            if (servletContext.getAttribute(NOTIFICATIONS_MASTER_ATTRIBUTE_NAME) == null) {
                servletContext.setAttribute(NOTIFICATIONS_MASTER_ATTRIBUTE_NAME, new NotificationsMaster());
            }
        }
        return (NotificationsMaster) servletContext.getAttribute(NOTIFICATIONS_MASTER_ATTRIBUTE_NAME);
    }

    public static int getIntParameter(HttpServletRequest request, String version) {
        String value = request.getParameter(version);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException numberFormatException) {
            }
        }
        return INT_PARAMETER_ERROR;
    }
}
