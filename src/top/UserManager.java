package top;

import bottom.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserManager {

    private List<User> users;

    public UserManager()
    {
        users = new ArrayList<>();
    }

    public synchronized void addUser(String username)
    {
        users.add(new User(username));
    }

    public synchronized void removeUser(String username) {
        boolean flag = false;
        for (int i=0;i<users.size() && !flag; i++)
            if (users.get(i).getName().equals(username)) {
                users.remove(i);
                flag = true;
            }
    }

    public synchronized List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public boolean isUserExists(String username) {
        for (int i=0;i<users.size(); i++)
            if (users.get(i).getName().equals(username))
                return true;
        return false;
    }

    public synchronized User findUser(String userName)
    {
        for (int i=0;i<users.size(); i++)
            if (users.get(i).getName().equals(userName))
                return users.get(i);
        return null;
    }


}
