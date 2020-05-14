package bottom;

import generated.GameDescriptor;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player implements Serializable {
    //transient protected SimpleIntegerProperty totalTuring;
    protected int tempTotalTuring;
    protected List<Slot> conquered;
    protected final char sign;
    protected String name;
    protected List<String> casualties;
    protected int id;
    protected List<String> notifications;
    protected double[] color; // red, green, blue
    protected String message;
    protected boolean inTurn;

    public Player(char sign, String name, int initialFunds) {
        this.sign = sign;
        this.name = name;
        color = new double[3];
        id = 0;
        tempTotalTuring=0;
        /*totalTuring = new SimpleIntegerProperty();
        if (initialFunds>=0)
            totalTuring.set(initialFunds);
        else
            totalTuring.set(1000);*/
        if (initialFunds>=0)
            tempTotalTuring=(initialFunds);
        else
            tempTotalTuring=(1000);
        conquered = new ArrayList<>(10);
        casualties = new ArrayList<>(10);
        notifications = new ArrayList<>(10);
        message="";
        inTurn=false;
    }

    public Player(GameDescriptor gameDes,int index)
    {
        //totalTuring = new SimpleIntegerProperty();
        color = new double[3];
        sign = (char)(index+'#');
        if (!gameDes.getGameType().equals("DynamicMultiPlayer")) {
            name = gameDes.getPlayers().getPlayer().get(index).getName();
            id = gameDes.getPlayers().getPlayer().get(index).getId().intValue();
            chooseColor(index);
        }
        else
            chooseColorMultiPlayer(index);
        /*if (gameDes.getGame().getInitialFunds().intValue()>=0)
            totalTuring.set(gameDes.getGame().getInitialFunds().intValue());
        else
            totalTuring.set(1000);*/
        if (gameDes.getGame().getInitialFunds().intValue()>=0)
            tempTotalTuring = gameDes.getGame().getInitialFunds().intValue();
        else
            tempTotalTuring = 1000;
        //tempTotalTuring=totalTuring.get();
        message="";
        conquered = new ArrayList<>();
        casualties = new ArrayList<>();
        notifications = new ArrayList<>();
        inTurn= false;
    }

    public Player(Player player)
    {
        this.sign = player.sign;
        this.name = player.name;
        //this.totalTuring=player.getTotalTuringProperty();
        this.id = player.id;
        casualties = new ArrayList<>(10);
        conquered = new ArrayList<>(10);
        notifications = new ArrayList<>(10);
        this.tempTotalTuring=player.tempTotalTuring;
        color = new double[3];
        for(int i=0;i<color.length;i++)
            color[i]=player.color[i];
        this.message=player.message;
        this.inTurn= player.inTurn;
    }

    public boolean isInTurn() {
        return inTurn;
    }

    public void setInTurn(boolean inTurn) {
        this.inTurn = inTurn;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double[] getColor() {
        return color;
    }


    public void chooseColor(int index)
    {
        switch (index)
        {
            case 0:
            {
                color[0]=(1.0);
                color[1]=(1.0);
                color[2]= (0.6);
                break;
            }
            case 1:
            {
                color[0]=(0.4);
                color[1]=(1.0);
                color[2]= (0.7);
                break;
            }
            case 2:
            {
                color[0]=1.0;
                color[1]=0.4;
                color[2]=0.4;
                break;
            }
            case 3:
            {
                color[0]=0.8;
                color[1]=0.4;
                color[2]=1.0;
                break;
            }
        }
    }

    public void chooseColorMultiPlayer(int index)
    {
        switch (index)
        {
            case 0:
            {
                color[0]=255;
                color[1]=255;
                color[2]=153;
                break;
            }
            case 1:
            {
                color[0]=102;
                color[1]=255;
                color[2]=178;
                break;
            }
            case 2:
            {
                color[0]=255;
                color[1]=102;
                color[2]=102;
                break;
            }
            case 3:
            {
                color[0]=204;
                color[1]=102;
                color[2]=255;
                break;
            }
        }
    }

    public List<String> getNotifications() {
        return notifications;
    }

    public char getSign() {
        return sign;
    }

    public String getName() {
        return name;
    }

    public int getTotalTuring() {
        return tempTotalTuring;
    }

    //public SimpleIntegerProperty getTotalTuringProperty(){return totalTuring;}

    /*public void setTotalTuring(int totalTuring) {
        if (this.totalTuring==null)
            this.totalTuring = new SimpleIntegerProperty();
        this.totalTuring.set(totalTuring);
        tempTotalTuring=totalTuring;
    }*/

    public void setTotalTuring(int tempTotalTuring) {
        this.tempTotalTuring = tempTotalTuring;
    }

    public int getTempTotalTuring() {
        return tempTotalTuring;
    }

    public int getConqueredSize() {
        return conquered.size();
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name + "'s stats\n" +
                "Turing: " + tempTotalTuring + "\n" +
                "Conquered Territories: ";
    }

    public List<Slot> getConquered() {
        return conquered;
    }

    public List<String> getCasualties() {
        return casualties;
    }

    public int totalValOfTerritories() {
        int sum = 0;
        for (int i = 0; i < conquered.size(); i++)
            sum = sum + conquered.get(i).roundRewards;
        return sum;
    }

    public void addRewards()
    {
        setTotalTuring(totalValOfTerritories()+getTotalTuring());
    }

     public List<Slot> calcFatiguePerPlayer(SimpleIntegerProperty[]unitsOnboard, List<Unit> model)
     {
        List<Slot> deadAndBuried = new ArrayList<>(10);
        boolean marker;
        int k;
        for(int i=0;i<conquered.size();i++)
        {
            Slot s= conquered.get(i);
            int sum=0;
            int[] flags = new int[model.size()];
            for(int r=0;r<flags.length;r++)
                flags[r]=0;
            String notify = "The units: ";
            for (int j=0; j<s.occupyingArmy.size(); j++) {
                marker=false;
                s.occupyingArmy.get(j).HP -= s.occupyingArmy.get(j).fatigueFactorMulti;
                int num= s.occupyingArmy.get(j).getMaximumMightMulti()/s.occupyingArmy.get(j).getMaximumMight();
                for(k=0; k<model.size()&&!marker; k++) {
                    if (model.get(k).getRank() == s.occupyingArmy.get(j).rank) {
                        flags[k] = num;
                        marker = true;
                    }
                }
                k--;
                if (s.occupyingArmy.get(j).HP<=0) {
                    String str = "";
                    str = str.concat(Integer.toString(conquered.get(i).serialNumber));
                    str = str.concat(" : ");
                    if (!notify.equals("The units: "))
                        notify = notify.concat(", ");
                    notify = notify.concat(s.occupyingArmy.get(j).type);
                    str = str.concat(s.occupyingArmy.remove(j).type);
                    casualties.add(str);
                    unitsOnboard[k].set(unitsOnboard[k].get() - num);
                    flags[k] = 0;
                    j--; //check if subtracted twice!
                }
                else
                    sum+=s.occupyingArmy.get(j).HP;
            }
            if (!notify.equals("The units: "))
            {
                notify= notify.concat("\n");
                notify=notify.concat("in Territory "+Integer.toString(conquered.get(i).serialNumber) + "are dead\n");
                notify = notify.concat("Since you're such a great warlord");
                notifications.add(notify);
            }
            if (sum<conquered.get(i).minimumMight)
            {
                for(int r=0;r<flags.length;r++)
                {
                    if(flags[r]>0)
                        unitsOnboard[r].set(unitsOnboard[r].get() - flags[r]);
                }
                deadAndBuried.add(conquered.remove(i));
                i--;
            }
        }
        return deadAndBuried;
     }

     public void addConquered(Slot slot)
     {
         conquered.add(slot);
     }


     public void removeFromConquered(Slot slot)
     {
         conquered.remove(slot);
     }


}
