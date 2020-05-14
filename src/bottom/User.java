package bottom;

public class User {
    private String name;
    private int id;
    private static int counter=0;

    public User(String name)
    {
        this.name=name;
        id = counter;
        counter++;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
