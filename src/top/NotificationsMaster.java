package top;

import java.util.ArrayList;
import java.util.List;

public class NotificationsMaster {

    private List<ManagerAndTitle> mats;

    public NotificationsMaster()
    {
        mats = new ArrayList<>();
    }

    public synchronized void addNotificationsManager(String title)
    {
        mats.add(new NotificationsMaster.ManagerAndTitle(new NotificationsManager(),title));
    }

    public synchronized void clearNotificationsManager(String title) {
        boolean flag = false;
        for (int i=0;i<mats.size() && !flag; i++)
            if (mats.get(i).getTitle().equals(title)) {
                mats.get(i).notificationsManager.clearData();
                flag = true;
            }
    }

    public synchronized NotificationsManager findNotificationsManager(String title)
    {
        for (int i=0;i<mats.size(); i++)
            if (mats.get(i).getTitle().equals(title))
                return mats.get(i).notificationsManager;
        return null;
    }

    class ManagerAndTitle
    {
        private NotificationsManager notificationsManager;
        private String title;

        ManagerAndTitle(NotificationsManager notificationsManager, String title)
        {
            this.notificationsManager = notificationsManager;
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
