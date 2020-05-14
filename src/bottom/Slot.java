package bottom;

import javafx.beans.property.SimpleBooleanProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Slot implements Serializable {
    protected final int roundRewards;
    protected final int minimumMight;
    protected char status;
    protected Player owner;
    //protected final Integer serialNumber;
    protected final int serialNumber;
    protected List<Unit> occupyingArmy;
    protected int heal;

    public Slot(int roundRewards, int minimumMight, int serialNumber) {
        this.roundRewards = roundRewards;
        this.minimumMight = minimumMight;
        this.serialNumber = serialNumber;
        this.status = 'N';
        owner=null;
        heal =0;
        occupyingArmy = new ArrayList<>(10);
    }

    public Slot(Slot slot)
    {
        this.roundRewards = slot.roundRewards;
        this.minimumMight = slot.minimumMight;
        this.serialNumber = slot.serialNumber;
        this.status = slot.status;
        if (slot.getOwner()!=null)
            this.owner = slot.owner;
        else
            this.owner=null;
        occupyingArmy = new ArrayList<>(10);
        for (int i=0; i<slot.occupyingArmy.size(); i++)
            occupyingArmy.add(new Unit(slot.occupyingArmy.get(i)));
        this.heal= slot.heal;
    }

    @Override
    public String toString() {
        return  "Area Number: " + serialNumber + "\n"+
                "Rewards Per Round: " + roundRewards + "\n"+
                "Might Required For Conquest: " + minimumMight + "\n"+
                "Ruler: " + status +
                '}';
    }

    public List<Unit> getOccupyingArmy() {
        return occupyingArmy;
    }

    public int calcHealPerSlot()
    {
        int sum=0;
        for (int j = 0; j < occupyingArmy.size(); j++)
            sum+=occupyingArmy.get(j).calcHealToFullForm();
        setHeal(sum);
        return sum;
    }

    public void setHeal(int heal) {
        this.heal = heal;
    }

    public int getHeal() {
        return heal;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public int getRoundRewards() {
        return roundRewards;
    }

    public int getMinimumMight() {
        return minimumMight;
    }

    public char getStatus() {
        return status;
    }

    public Integer getSerialNumber() {
        return serialNumber;
    }

    public void addToOccupyingArmy(Unit unit) {
        occupyingArmy.add(unit);
    }

    public void healUnits()
    {
        for (int i=0;i<occupyingArmy.size();i++)
        {
            occupyingArmy.get(i).HP=occupyingArmy.get(i).maximumMightMulti;
        }
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public Player getOwner() {
        return owner;
    }

    public int getMaximumMightMulti(String type)
    {
        for(int i=0;i<occupyingArmy.size();i++)
        {
            if (occupyingArmy.get(i).getType().equals(type))
                return occupyingArmy.get(i).maximumMightMulti;
        }
        return occupyingArmy.get(0).maximumMightMulti;
    }

    public int getFatigueFactorMulti(String type)
    {
        for(int i=0;i<occupyingArmy.size();i++)
        {
            if (occupyingArmy.get(i).getType().equals(type))
                return occupyingArmy.get(i).fatigueFactorMulti;
        }
        return occupyingArmy.get(0).fatigueFactorMulti;
    }

    public void setNewMulti(int newMaxMulti, int newFatigueMulti, String type, int addHP)
    {
        for(int i=0;i<occupyingArmy.size();i++)
        {
            if (occupyingArmy.get(i).getType().equals(type))
            {
                occupyingArmy.get(i).setFatigueFactorMulti(newFatigueMulti);
                occupyingArmy.get(i).setMaximumMightMulti(newMaxMulti);
                occupyingArmy.get(i).setHP(occupyingArmy.get(i).getHP()+addHP);
            }
        }
    }

    public int getCurrentMight()
    {
        int sum=0;
        for(int i=0;i<occupyingArmy.size();i++)
        {
            sum+=occupyingArmy.get(i).HP;
        }
        return sum;
    }

    public void clearOccupyingArmy()
    {
        occupyingArmy.clear();
        heal=0;
    }

    public void updateOccupyingPostBattle(double cutFactor)
    {
        for (int i=0;i<occupyingArmy.size();i++)
            occupyingArmy.get(i).setHP((int)(cutFactor*occupyingArmy.get(i).getHP()+1));
    }

    public int calcLostUnitCostPostBattle(int remainingMight)
    {
        int sum=0;
        for (int i=0;i<occupyingArmy.size();i++)
            sum+= ((occupyingArmy.get(i).cost/occupyingArmy.get(i).maximumMight)*remainingMight);
        return sum;
    }

}
