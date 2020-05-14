package top;

import bottom.Player;
import bottom.Slot;
import bottom.Unit;
import bottom.User;
import generated.GameDescriptor;
import generated.Teritory;
import javafx.beans.property.SimpleIntegerProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Round implements Serializable {

    protected Slot[][] board;
    protected List<Player> players;
    protected int numOfPlayers;
    protected final int columns;
    protected final int rows;
    protected List<Unit> model;
    transient protected SimpleIntegerProperty[] unitsOnBoard;
    protected int[] tempUnitsOnBoard;

    public static final int BUY = 1;
    public static final int HEAL = 2;
    public static final int STRENGTHEN = 3;
    public static final int CONQUER = 4;
    public static final int LUCKY = 5;
    public static final int ORCHESTRATED = 6;

    public Round(GameDescriptor gameDes) {
        rows = gameDes.getGame().getBoard().getRows().intValue();
        columns = gameDes.getGame().getBoard().getColumns().intValue();
        board = new Slot[rows][columns];
        if (gameDes.getGameType().equals("DynamicMultiPlayer")) {
            numOfPlayers = gameDes.getDynamicPlayers().getTotalPlayers().intValue();
            players = new ArrayList<>();
        }
        else
        {
            numOfPlayers = gameDes.getPlayers().getPlayer().size();
            players = new ArrayList<>(numOfPlayers);
            for (int i=0; i<numOfPlayers; i++)
                players.add(new Player(gameDes, i));
        }
        model = new ArrayList<>(gameDes.getGame().getArmy().getUnit().size());
        for (int i=0; i<gameDes.getGame().getArmy().getUnit().size(); i++)
        {
            model.add(new Unit(gameDes.getGame().getArmy().getUnit().get(i).getRank(),
                gameDes.getGame().getArmy().getUnit().get(i).getType(),
                gameDes.getGame().getArmy().getUnit().get(i).getPurchase().intValue(),
                gameDes.getGame().getArmy().getUnit().get(i).getMaxFirePower().intValue(),
                gameDes.getGame().getArmy().getUnit().get(i).getCompetenceReduction().intValue()));
        }
        tempUnitsOnBoard = new int[model.size()];
        unitsOnBoard=new SimpleIntegerProperty[model.size()];
        for (int i=0; i<unitsOnBoard.length;i++)
            unitsOnBoard[i]=new SimpleIntegerProperty(0);
        makeEmptyBoard(gameDes);
    }

    public Round(Round round)
    {
        this.numOfPlayers=round.numOfPlayers;
        this.model=round.model;
        this.rows= round.rows;
        this.columns= round.columns;
        this.players= new ArrayList<>();
        board = new Slot[rows][columns];
        for (int i=0; i<rows; i++)
            for (int j=0 ;j<columns; j++)
                board[i][j]= new Slot(round.board[i][j]);
        for (int i=0; i<numOfPlayers; i++)
        {
            players.add(new Player(round.players.get(i)));
            copyConquered(players.get(i));
        }
        tempUnitsOnBoard = new int[model.size()];
        unitsOnBoard=new SimpleIntegerProperty[model.size()];
        for (int i=0; i<unitsOnBoard.length;i++)
            unitsOnBoard[i] = new SimpleIntegerProperty(round.unitsOnBoard[i].get());
    }

    public synchronized void setNumOfPlayers(int numOfPlayers) {
        this.numOfPlayers = numOfPlayers;
    }

    public int[] getTempUnitsOnBoard() {
        return tempUnitsOnBoard;
    }

    public void user2Player(User user, GameDescriptor gameDes)
    {
        players.add(new Player(gameDes,players.size()));
        players.get(players.size()-1).setName(user.getName());
        players.get(players.size()-1).setId(user.getId());
    }

    public Player getPlayerByName(String name)
    {
        for (int i=0; i<players.size(); i++)
        {
            if (players.get(i).getName().equals(name))
                return players.get(i);
        }
        return null;
    }

    public void createUnitsOnBoard()
    {
        unitsOnBoard=new SimpleIntegerProperty[tempUnitsOnBoard.length];
        for(int i=0;i<unitsOnBoard.length;i++) {
            unitsOnBoard[i] = new SimpleIntegerProperty();
            unitsOnBoard[i].set(tempUnitsOnBoard[i]);
        }
    }



    public SimpleIntegerProperty[] getUnitsOnBoard() {
        return unitsOnBoard;
    }

    public synchronized int getNumOfPlayers() {
        return numOfPlayers;
    }

    public void copyConquered(Player player)
    {
        for (int i=0; i< rows; i++)
            for(int j=0; j<columns; j++)
            {
                if (board[i][j].getOwner()!=null) {
                    if (board[i][j].getOwner().getId() == player.getId())
                        player.addConquered(board[i][j]);
                }
            }
    }


    public void makeEmptyBoard(GameDescriptor gameDes)
    {
        List<Teritory> gen = gameDes.getGame().getTerritories().getTeritory();
        int genIndex=0;
        int boardId=1;
        for(int i=0;i<rows;i++)
        {
            for (int j = 0; j < columns; j++)
            {
                if (genIndex<gen.size() && boardId == gen.get(genIndex).getId().intValue()) {
                    board[i][j] = new Slot(gen.get(genIndex).getProfit().intValue(),
                            gen.get(genIndex).getArmyThreshold().intValue(),
                            gen.get(genIndex).getId().intValue());
                    genIndex++;
                }
                else
                    board[i][j]=new Slot(gameDes.getGame().getTerritories().getDefaultProfit().intValue(),
                            gameDes.getGame().getTerritories().getDefaultArmyThreshold().intValue(),boardId);
                boardId++;
            }
        }
    }

    public void healUnitsInSlot(Player player,Slot slot)
    {
        player.setTotalTuring(player.getTotalTuring()-slot.getHeal());
        slot.healUnits();
    }

    public boolean purchaseUnits(Player player, Slot slot,int action, int purchase, int might, int[] numOfUnitsToBuy) {
        switch (action) {
            case BUY: {
                if (might >= slot.getMinimumMight() && purchase <= player.getTotalTuring()) {
                    createUnits(slot, numOfUnitsToBuy);
                    updateUnitsOnBoard(numOfUnitsToBuy);
                    player.setTotalTuring(player.getTotalTuring() - purchase);
                    player.addConquered(slot);
                    slot.setStatus(player.getSign());
                    slot.setOwner(player);
                    return true;
                }
                break;
            }
            case STRENGTHEN: {
                if (purchase <= player.getTotalTuring() && purchase != 0) {
                    strengthenUnits(slot, numOfUnitsToBuy);
                    updateUnitsOnBoard(numOfUnitsToBuy);
                    player.setTotalTuring(player.getTotalTuring() - purchase);
                    return true;
                }
                break;
            }
            case CONQUER: {
                if (purchase <= player.getTotalTuring()) {
                    player.setTotalTuring(player.getTotalTuring() - purchase);
                    return true;
                }
                break;
            }
            default:
                break;
        }
        return false;
    }

    public int calculatedAttack(Player player,Slot slot,int[]numOfUnitsToBuy)
    {
        int rank=0;
        int[]currentNumOfUnits= getCurrentNumOfUnitsInSlot(slot);
        int check=0;
        for(int i=0;i<model.size(); i++)
        {
            if (numOfUnitsToBuy[i]>currentNumOfUnits[i])//attacker
            {
                numOfUnitsToBuy[i]-=currentNumOfUnits[i];
                currentNumOfUnits[i]=0;
                if(model.get(i).getRank()>rank)
                {
                    rank = model.get(i).getRank();
                    check = i;
                }
            }
            else if(numOfUnitsToBuy[i]<currentNumOfUnits[i])//defender
            {
                currentNumOfUnits[i]-=numOfUnitsToBuy[i];
                numOfUnitsToBuy[i]=0;
                if(model.get(i).getRank()>rank)
                {
                    rank = model.get(i).getRank();
                    check = i;
                }
            }
            else { //tie
                numOfUnitsToBuy[i]=0;
                currentNumOfUnits[i]=0;
            }
        }
        int remainingMight;
        if(rank==0) //defender wins but no refund
        {
            updateUnitsOnBoardAfterBattle(slot);
            slot.clearOccupyingArmy();
            slot.getOwner().removeFromConquered(slot);
            slot.setStatus('N');
            slot.setOwner(null);
        }
        else
        {
            if(currentNumOfUnits[check]>numOfUnitsToBuy[check]) //defender wins
            {
                remainingMight=calcRemainingMight(currentNumOfUnits);
                calcBattleOutcomeOrcDefender(slot.getOwner(),slot,currentNumOfUnits,remainingMight);
                return 2;
            }
            else //attacker wins
            {
                remainingMight = calcRemainingMight(numOfUnitsToBuy);
                calcBattleOutcomeOrcAttacker(player,slot,remainingMight ,numOfUnitsToBuy);
                return 1;
            }
        }
        return 2;
    }

    public void calcBattleOutcomeOrcDefender(Player player,Slot slot, int[] currentNumOfUnits ,int remainingMight)
    {
        if (remainingMight >= slot.getMinimumMight())
        {
            updateUnitsOnBoardAfterBattle(slot);
            slot.clearOccupyingArmy();
            calcNewUnits(currentNumOfUnits,slot);
        }
        else
        {
            int sum=calcRefundOrc(currentNumOfUnits);
            player.setTotalTuring(player.getTotalTuring()+sum);
            updateUnitsOnBoardAfterBattle(slot);
            slot.clearOccupyingArmy();
            player.removeFromConquered(slot);
            slot.setStatus('N');
            slot.setOwner(null);
        }
    }

    public void calcNewUnits(int[] units,Slot slot)
    {
        for (int i = 0; i < units.length; i++) {
            if (units[i] != 0)
            {
                Unit unit = new Unit(units[i]*model.get(i).getMaximumMight(),
                        units[i]*model.get(i).getFatigueFactor(),model,i);
                slot.addToOccupyingArmy(unit);
            }
        }
        updateUnitsOnBoard(units);
    }

    public int calcRemainingMight(int[] numOfUnitsToBuy)
    {
        int sum=0;
        for(int i=0;i<numOfUnitsToBuy.length;i++)
            sum+=numOfUnitsToBuy[i]*model.get(i).getHP();
        return sum;
    }

    public void calcBattleOutcomeOrcAttacker (Player player,Slot slot,int remainingMight,int[] numOfUnitsToBuy)
    {
        slot.getOwner().removeFromConquered(slot);
        updateUnitsOnBoardAfterBattle(slot);
        slot.clearOccupyingArmy();
        if (remainingMight >= slot.getMinimumMight())
        {
            slot.setStatus(player.getSign());
            slot.setOwner(player);
            player.addConquered(slot);
            calcNewUnits(numOfUnitsToBuy,slot);
        }
        else
        {
            slot.setStatus('N');
            slot.setOwner(null);
            int sum=calcRefundOrc(numOfUnitsToBuy);
            player.setTotalTuring(player.getTotalTuring()+sum);
            slot.setHeal(sum); //place holder for refund
        }
    }

    public int calcRefundOrc(int[] units)
    {
        int sum=0;
        for (int i=0; i<units.length; i++)
        {
            if(units[i]!=0)
                sum+=units[i]*model.get(i).getCost();
        }
        return sum;
    }

    public int[] getCurrentNumOfUnitsInSlot(Slot slot)
    {
        int[] currentNumOfUnits= new int[model.size()];
        for (int i=0; i<slot.getOccupyingArmy().size(); i++)
        {
            for(int j=0; j<model.size(); j++)
                if (model.get(j).getRank()== slot.getOccupyingArmy().get(i).getRank())
                    currentNumOfUnits[j]= slot.getOccupyingArmy().get(i).getMaximumMightMulti()/slot.getOccupyingArmy().get(i).getMaximumMight();
        }
        return currentNumOfUnits;
    }

    public int attackOccupied(Player player,Slot slot,int attackerMight,int[] numOfUnitsToBuy)
    {
        int ownerMight=slot.getCurrentMight();
        int sum= attackerMight + ownerMight;
        Player owner= getTerritoryOwner(slot);
        int random = (int)(Math.random() * sum + 1);
        if(random <= attackerMight) //attacker wins
        {
            updateUnitsOnBoardAfterBattle(slot);
            owner.removeFromConquered(slot);
            slot.clearOccupyingArmy();
            if (attackerMight> ownerMight)
                calcBattleOutcomeWinner(player, slot,attackerMight, attackerMight - ownerMight,numOfUnitsToBuy);
            else
                calcBattleOutcomeWinner(player, slot,attackerMight, attackerMight/2,numOfUnitsToBuy);
            return 1;
        }
        else //defender wins
        {
            if (ownerMight >attackerMight)
                calcBattleOutcomeDefender(owner, slot,ownerMight, ownerMight - attackerMight);
            else
                calcBattleOutcomeDefender(owner, slot,ownerMight, ownerMight/2);
            return 2;
        }
    }

    public void calcBattleOutcomeDefender(Player player,Slot slot, int might,int remainingMight)
    {
        double cutFactor = remainingMight / (double)might;
        if (remainingMight >= slot.getMinimumMight())
            slot.updateOccupyingPostBattle(cutFactor);
        else
        {
            int sum=slot.calcLostUnitCostPostBattle(remainingMight);
            player.setTotalTuring(player.getTotalTuring()+sum);
            updateUnitsOnBoardAfterBattle(slot);
            slot.clearOccupyingArmy();
            player.removeFromConquered(slot);
            slot.setStatus('N');
            slot.setOwner(null);
        }
    }

    public void updateUnitsOnBoardAfterBattle(Slot slot)
    {
        int num;
        for (int i=0; i<slot.getOccupyingArmy().size(); i++)
        {
            for (int j=0; j<model.size(); j++)
            {
                if(model.get(j).getRank()==slot.getOccupyingArmy().get(i).getRank())
                {
                    num = slot.getOccupyingArmy().get(i).getMaximumMightMulti()/
                        slot.getOccupyingArmy().get(i).getMaximumMight();
                    unitsOnBoard[j].set(unitsOnBoard[j].get()-num);
                }
            }
        }
    }

    public void calcBattleOutcomeWinner (Player player,Slot slot, int might,int remainingMight,int[] numOfUnitsToBuy)
    {
        double cutFactor = remainingMight / (double)might;
        if (remainingMight >= slot.getMinimumMight())
        {
            slot.setStatus(player.getSign());
            slot.setOwner(player);
            player.addConquered(slot);
            for (int i = 0; i < numOfUnitsToBuy.length; i++) {
                if (numOfUnitsToBuy[i] != 0)
                {
                    Unit unit = new Unit(numOfUnitsToBuy[i]*model.get(i).getMaximumMight(),
                            numOfUnitsToBuy[i]*model.get(i).getFatigueFactor(),model,i);
                    unit.setHP((int)(cutFactor*unit.getHP()+1));
                    slot.addToOccupyingArmy(unit);
                }
            }
            updateUnitsOnBoard(numOfUnitsToBuy);
        }
        else
        {
            slot.setStatus('N');
            slot.setOwner(null);
            int sum=0;
            for (int i=0; i<numOfUnitsToBuy.length; i++)
            {
                if(numOfUnitsToBuy[i]!=0)
                    sum+= ((model.get(i).getCost()/model.get(i).getMaximumMight())*remainingMight);
            }
            player.setTotalTuring(player.getTotalTuring()+sum);
            slot.setHeal(sum);
        }
    }

    public void strengthenUnits(Slot slot, int[] numOfUnitsToBuy)
    {
        for(int i=0; i< numOfUnitsToBuy.length;i++)
        {
            if (numOfUnitsToBuy[i]!=0)
            {
                int newMaximumMight = (numOfUnitsToBuy[i]*model.get(i).getMaximumMight())+
                        slot.getMaximumMightMulti(model.get(i).getType());
                int newFatigueFactor = (numOfUnitsToBuy[i]*model.get(i).getFatigueFactor())+
                        slot.getFatigueFactorMulti(model.get(i).getType());
                slot.setNewMulti(newMaximumMight, newFatigueFactor, model.get(i).getType(),
                        numOfUnitsToBuy[i]*model.get(i).getMaximumMight());
            }
        }
    }

    public void createUnits(Slot slot, int[] numOfUnitsToBuy)
    {
        for(int i=0; i< numOfUnitsToBuy.length;i++)
        {
            if (numOfUnitsToBuy[i]!=0)
            {
                Unit unit = new Unit(numOfUnitsToBuy[i]*model.get(i).getMaximumMight(),
                        numOfUnitsToBuy[i]*model.get(i).getFatigueFactor(),model,i);
                slot.addToOccupyingArmy(unit);
            }
        }

    }

    public void updateUnitsOnBoard(int[]numOfUnitsToBuy)
    {
        for(int i=0; i<numOfUnitsToBuy.length; i++)
        {
            if (numOfUnitsToBuy[i]!=0)
                unitsOnBoard[i].set(unitsOnBoard[i].get()+numOfUnitsToBuy[i]);
        }
    }

    public Slot cellToSlot(int cell)
    {
        for (int i=0; i<rows ; i++)
            for (int j=0; j<columns ; j++)
            {
                if (board[i][j].getSerialNumber()== cell)
                    return board[i][j];
            }
        return null;
    }


    public boolean noNeutral() {
        for (int i = 0; i < rows ; i++)
            for (int j = 0; j < columns ; j++) {
                if ((board[i][j].getStatus() == 'N')||(board[i][j].getOwner()==null))
                    return false;
            }
        return true;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public List<Slot> calcFatigue(int i)
    {
        List<Slot> deadAndBuried;
        deadAndBuried = players.get(i).calcFatiguePerPlayer(unitsOnBoard, model);
        for (int j = 0; j < deadAndBuried.size(); j++)
        {
            deadAndBuried.get(j).setStatus('N');
            deadAndBuried.get(j).setOwner(null);
        }
        return deadAndBuried;
    }

    public Slot[][] getBoard() {
        return board;
    }

    public int getColumns() {
        return columns;
    }

    public int getRows() {
        return rows;
    }

    public List<Unit> getModel() {
        return model;
    }

    public boolean isThisActionPossible(int num, Player player) {
        int r = 0, c = 0;
        boolean flag = false;
        for (int i = 0; i < rows && !flag; i++)
            for (int j = 0; j < columns && !flag; j++) {
                if (board[i][j].getSerialNumber() == num) {
                    r = i;
                    c = j;
                    flag = true;
                    if (board[i][j].getStatus() == player.getSign())
                        return true;
                    if (board[i][j].getOwner()!=null)
                        if(board[i][j].getOwner().getId()==player.getId())
                            return true;
                }
            }
        int id = player.getId();
        char sign = player.getSign();
        if (r - 1 >= 0) {
            if (board[r - 1][c].getStatus() == sign)
                return true;
            if (board[r-1][c].getOwner()!=null)
                if(board[r - 1][c].getOwner().getId()==id)
                    return true;
        }
        if (c + 1 < columns) {
            if (board[r][c + 1].getStatus() == sign)
                return true;
            if (board[r][c+1].getOwner()!=null)
                if(board[r][c + 1].getOwner().getId()==id)
                    return true;
        }
        if (c - 1 >= 0) {
            if (board[r][c - 1].getStatus() == sign)
                return true;
            if (board[r][c-1].getOwner()!=null)
                if(board[r][c - 1].getOwner().getId()==id)
                    return true;
        }
        if (r + 1 < rows){
            if (board[r+1][c].getStatus()== sign)
                return true;
            if (board[r+1][c].getOwner()!=null)
                if(board[r+1][c].getOwner().getId()==id)
                    return true;
        }
        return false;
    }

    public Player getTerritoryOwner (Slot slot)
    {
        Player player= null;
        for (int i=0; i<numOfPlayers; i++)
        {
            if (slot.getStatus()== players.get(i).getSign())
                player= players.get(i);
            if (slot.getOwner()!=null)
                if(slot.getOwner().getId()==players.get(i).getId())
                    player= players.get(i);
        }
        return player;
    }
}


