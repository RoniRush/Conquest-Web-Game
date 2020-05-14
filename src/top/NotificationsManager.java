package top;

import java.util.ArrayList;
import java.util.List;

public class NotificationsManager {

    private int notificationsVersion;
    private List<String> notifications;

    public NotificationsManager()
    {
        notifications = new ArrayList<>();
        notificationsVersion = 0;
    }

    public synchronized void clearData()
    {
        notifications.clear();
        notificationsVersion = 0;
    }

    public synchronized int getNotificationsManagerVersion() {
        return notificationsVersion;
    }

    public synchronized void addNotification(String message)
    {
        notifications.add(message);
        notificationsVersion++;
    }

    public synchronized List<String> getNotifications(int fromIndex){
        if (fromIndex < 0 || fromIndex > notifications.size()) {
            fromIndex = 0;
        }
        return notifications.subList(fromIndex, notifications.size());
    }
}
