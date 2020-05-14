# Conquest-Web-Game
Conquest is a strategy web game developed with java and java script (using HTML and css) , running on tomcat server

before we dive in, there are important setups:
install java and intelliJ IDEA (it will be easier to use beacuse all the setups i already made in my project), install tomcat 8.5.55, hit run in the inteliJ

In an imaginary world, there is a map of territories that players must occupy. The progression of time in this world is measured in regular cycles instead of events happening. (You can treat the cycle as a round in the game where players perform)

Each surface cell is characterized by the amount of ore that provide a certain yield over each time cycle.
The ore extraction in the various territories eventually translates into a currency transfer to the merchant, who is accumulated for the benefit of the surface cell owners.
Each territory cell, accordingly, is defined by the amount of military force required to protect and include it within the state's borders.

Each participant runs an army of units of various types, with varying "firepower".
By placing and displaying sufficient military capability, he protects his territories as well as purports to conquer territories from his rivals.
At the beginning of the game each competitor starts with a certain amount of money (the same as all players).
He must choose which army he wants to assemble, as well as which territories he takes over by placing a sufficient number of military units according to the requirements of each field cell.

The game ends when several rounds are set as predefined.
The winner, then, will be the competitor who controls at the end of the game in the amount of terrain cells that yields the maximum money.

SCREENS:

1. welcome screen

![alt text](https://github.com/RoniRush/Conquest-Web-Game/blob/master/entryscreen.png?raw=true)

2. login page

![alt text](https://github.com/RoniRush/Conquest-Web-Game/blob/master/loginscreen.png?raw=true)

3. lobby page - here you can see all available games and uploade new games. also you can join existing games (as a player or a viewr), you can see your online friend, and you can loggout

![alt text](https://github.com/RoniRush/Conquest-Web-Game/blob/master/lobbyscreen.png?raw=true)

4. game screen - here is where the magic is happened, the game beggins only after the correct amount of players are signed in.
int this screen you can see all players data (also observers), you have the game board and the data about the available units in the game.
each turn you can choose - 
  * attack (you can win and get the territory or lose)
  * pass turn and do nothing
  * surrender, so you quit the game
  
![alt text](https://github.com/RoniRush/Conquest-Web-Game/blob/master/gamescreen.png?raw=true)
