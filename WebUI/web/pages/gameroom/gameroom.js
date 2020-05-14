var boardVersion = 0;
var notificationsVersion = 0;
var refreshRate = 2000; //milli seconds
var GAME_INFO_URL = buildUrlWithContextPath("gameinfo");
var GAMES_LIST_URL = buildUrlWithContextPath("gameslist");
var UPDATE_BOARD_URL = buildUrlWithContextPath("updateboard");
var UPDATE_NOTIFICATIONS_URL = buildUrlWithContextPath("updatenotifications");
var PLAYER_ACTION_URL = buildUrlWithContextPath("playeraction");
var SHOP_URL = buildUrlWithContextPath("/pages/gameroom/shop");
var GAME_FLOW_URL = buildUrlWithContextPath("gameflow");
var LOBBY_URL = "../lobby/lobby.html";



function notActiveYet() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "getStatus"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(json) {
            if (json.stat==="Not Active")
                if(json.userStat==="player")
                    alert("Welcome, we're still waiting on your competition");
        }
    });
}

$(function() {
    helloPlayer();
    setSurrenderAndPassTurnButtons();
    triggerStatusTimeOut();
    showUnits();
    initBoard();
    setInterval(ajaxPlayersList, refreshRate);
    setInterval(ajaxObserversList, refreshRate);
    setInterval(ajaxGameInfo, refreshRate);
    setInterval(ajaxBoard, refreshRate);
    notActiveYet();
    setInterval(ajaxGameFlow, refreshRate);
    setInterval(ajaxNotification, refreshRate);
});


$(function() { // onload...do
    //add a function to the submit event
    $("#shopform").submit(function() {
        removeShopPopUp();
        $.ajax({
            data: $(this).serialize(),
            url: this.action,
            timeout: 2000,
            error: function() {
                console.error("Failed to submit");
            },
            success: function(json) {
                if(json.status==="success")
                   buildConcludingPopUp(json);
            }
        });
        // by default - we'll always return false so it doesn't redirect the user.
        return false;
    });
});

function setSurrenderAndPassTurnButtons() {
    var passTurnBtn = document.createElement("button");
    passTurnBtn.id = "passTurnBtn";
    passTurnBtn.classList.add("button");
    passTurnBtn.innerHTML = "Pass Turn";
    var surrenderBtn = document.createElement("button");
    surrenderBtn.innerHTML = "Surrender";
    surrenderBtn.classList.add("button");
    surrenderBtn.id = "surrenderBtn";
    document.getElementById("passTurn").appendChild(passTurnBtn);
    document.getElementById("surrender").appendChild(surrenderBtn);
    passTurnBtn.onclick = function(){
        passTurnOnClick();
    };
    surrenderBtn.onclick = function(){
        surrenderOnClick();
    };
}

function passTurnOnClick() {
    removeConcludingPopUp();
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                status:"passturn",
                action: "turnEnd"
            },
            type: 'POST',
            success: function(r) {
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}

function surrenderOnClick() {
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                action: "surrender"
            },
            type: 'POST',
            success: function(r) {
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
    window.location = LOBBY_URL;
}

function buildConcludingPopUp(json)
{
    //serial, status, direct(strengthen/heal/neutral/lucky/orchestrated),
    // player, currentOccupyingArmy, previousOccupyingArmy
    $("#stationedUnits").empty();
    $("#messageFromRushkin").empty();
    var div = $('.concludingDiv')[0];
    div.style.display = "block";
    var direct = json.direct;
    var message = document.getElementById("messageFromRushkin");
    message.setAttribute('style','white-space:pre;');
    var occupying = json.currentOccupyingArmy;
    switch (direct) {
        case "neutral": {
            message.textContent = "Congratulations!\r\n You've conquered territory numbered " + json.serial + " Such Prowess!";
            occupying.forEach(function (unit) {
                var li = document.createElement('li');
                var div1 = document.createElement('div');
                div1.classList.add("row");
                var div2 = document.createElement('div');
                div2.classList.add("row");
                div1.textContent = "Type: " + unit.typeOfUnit;
                div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
                var space = document.createElement('h2');
                space.textContent = "   ";
                li.appendChild(div1);
                li.appendChild(div2);
                li.appendChild(space);
                document.getElementById("stationedUnits").appendChild(li);
            });
            break;
        }
        case "strengthen": {
            message.textContent = "You've fortified your hold on territory numbered " + json.serial;
            occupying.forEach(function (unit) {
                var li = document.createElement('li');
                var div1 = document.createElement('div');
                div1.classList.add("row");
                var div2 = document.createElement('div');
                div2.classList.add("row");
                div1.textContent = "Type: " + unit.typeOfUnit;
                div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
                var space = document.createElement('h2');
                space.textContent = "   ";
                li.appendChild(div1);
                li.appendChild(div2);
                li.appendChild(space);
                document.getElementById("stationedUnits").appendChild(li);
            });
            break;
        }
        case "heal": {
            message.textContent = "I've healed all Units stationed in territory numbered " + json.serial + " back to full form";
            occupying.forEach(function (unit) {
                var li = document.createElement('li');
                var div1 = document.createElement('div');
                div1.classList.add("row");
                var div2 = document.createElement('div');
                div2.classList.add("row");
                div1.textContent = "Type: " + unit.typeOfUnit;
                div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
                var space = document.createElement('h2');
                space.textContent = "   ";
                li.appendChild(div1);
                li.appendChild(div2);
                li.appendChild(space);
                document.getElementById("stationedUnits").appendChild(li);
            });
            break;
        }
        case "lucky": {
            getBattleMessage(json);
            var luckyResults = json.luckyResults;
            if (luckyResults===2) {
                occupying.forEach(function (unit) {
                    var li = document.createElement('li');
                    var div1 = document.createElement('div');
                    div1.classList.add("row");
                    var div2 = document.createElement('div');
                    div2.classList.add("row");
                    div1.textContent = "Type: " + unit.typeOfUnit;
                    div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
                    var space = document.createElement('h2');
                    space.textContent = "   ";
                    li.appendChild(div1);
                    li.appendChild(div2);
                    li.appendChild(space);
                    document.getElementById("stationedUnits").appendChild(li);
                });
            }
            break;
        }
        case "orchestrated": {
            getBattleMessage(json);
            var luckyResults = json.luckyResults;
            if (luckyResults===2)
            {
                occupying.forEach(function (unit) {
                    var li = document.createElement('li');
                    var div1 = document.createElement('div');
                    div1.classList.add("row");
                    var div2 = document.createElement('div');
                    div2.classList.add("row");
                    div1.textContent = "Type: " + unit.typeOfUnit;
                    div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
                    var space = document.createElement('h2');
                    space.textContent = "   ";
                    li.appendChild(div1);
                    li.appendChild(div2);
                    li.appendChild(space);
                    document.getElementById("stationedUnits").appendChild(li);
                });
            }
            break;
        }
    }
}

function getBattleMessage(json)
{
    var luckyResults = json.luckyResults;
    var serial = json.serial;
    var spoils= json.spoils;
    var playerTuring = json.playerTuring;
    var p = document.createElement('p');
    p.setAttribute('style','white-space:pre;');
    switch (luckyResults){
        case 1:
        {
            p.textContent ="VICTORY! Your troops have defeated the rival army!\r\n" +
                "Here is the final report of the attack:\r\n" +
                "You lack sufficient power to hold control of this territory\r\n" +
                "You have no choice but to pull back\r\n" +
                "It's not all bad though, The troops who survived have brought some Turing with them\r\n" +
                "Here are your spoils: "+ spoils+ "\r\n"+
                "Your total Turing is now: "+ playerTuring+ "\r\n"+
                "Territory number: "+ serial+" is now neutral\r\n";
            break;
        }
        case 2:
        {
            p.textContent ="VICTORY! Your troops have defeated the rival army!\r\n" +
                "Here is the final report of the attack:\r\n " +
                "Splendid! you have sufficient power to maintain hold of this territory\r\n" +
                "Territory number" + serial + " is under your control\r\n";
                break;
        }
        case 3:
        {
            p.textContent ="BATTLE LOST\r\n" +
                "Poor planning has led to your troops' demise, The Defender stands victorious\r\n" +
                "But.. The Defender lacks sufficient power to hold control of this territory\r\n" +
                "Looks like he's retreating with his spoils\r\n";
            break;
        }
        case 4:
        {
            p.textContent ="BATTLE LOST\r\n" +
                "Poor planning has led to your troops' demise, The Defender stands victorious\r\n" +
                "And remains in possession of the territory\r\n";
            break;
        }
    }
    document.getElementById("messageFromRushkin").appendChild(p);
}

function turnEnd() {
    removeConcludingPopUp();
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                status:"",
                action: "turnEnd"
            },
            type: 'POST',
            success: function(r) {
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}

function ajaxGameFlow() {
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                action: "checkFlow"
            },
            type: 'POST',
            success: function(json) {
                var status = json.status;
                if(status==="gameEnded")
                    showWinners(json);
                if(status==="myTurn")
                    myTurnEnableClick(json);
                if(status==="otherTurn")
                    notMyTurnDisableClick(json);
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}

function myTurnEnableClick(json) {
    var passTurnButton = document.getElementById("passTurnBtn");
    passTurnButton.disabled= false;
    //var square = document.getElementsByClassName("square");
    //square.onclick = slotAction;
    var board = json.board;
    var rows = json.rows;
    var cols = json.columns;
    for (var k = 0; k < rows; k++) {
        for( var m=0; m<cols; m++ )
        {
            var serial = board[k][m];
            var square = document.getElementById(serial);
            square.onclick = slotAction;
        }
    }
}

function notMyTurnDisableClick(json) {
    var passTurnButton = document.getElementById("passTurnBtn");
    passTurnButton.disabled= true;
    //var square = document.getElementsByClassName("square");
    //square.onclick= null;
    var board = json.board;
    var rows = json.rows;
    var cols = json.columns;
    for (var k = 0; k < rows; k++) {
        for( var m=0; m<cols; m++ )
        {
            var serial = board[k][m];
            var square = document.getElementById(serial);
            square.onclick = null;
        }
    }
}


function showWinners(json){
    //json:
    // String status
    // String winners;
    $("#winnersNames").empty();
    var div = $('.winnersDiv')[0];
    div.style.display = "block";
    var names =document.getElementById("winnersNames");
    names.textContent = json.winners;

    // reset gameInfo, clear boardManager
}

function backToLobby() {
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                action: "backToLobby"
            },
            type: 'POST',
            success: function(r) {
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
    window.location = LOBBY_URL;
}

function ajaxNotification() {
    $.ajax(
        {
            url: UPDATE_NOTIFICATIONS_URL,
            data: {
                version: notificationsVersion
            },
            type: 'POST',
            success: function(json) {
                updateNotifications(json);
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}

function updateNotifications(json) {
    if(json.userStat==="player") {
        if (json.playerMessage !== "")
            alert(json.playerMessage);
    }
    if (notificationsVersion !== json.version) {
        notificationsVersion = json.version;
        var notifications = json.notifications;
        notifications.forEach(function (notification) {
            alert(notification);
        })
    }
}

function ajaxBoard() {
    $.ajax(
        {
            url: UPDATE_BOARD_URL,
            data: {
                version: boardVersion
            },
            type: 'POST',
            success: function(json) {
                updateBoard(json);
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}



function updateBoard(json) {
    /*data will arrive as:
    {
    list changeLogs - each item will have serial and owner
    new version
    } */
    if (boardVersion!== json.version)
    {
        boardVersion = json.version;
        var changes = json.changeLogs;
        changes.forEach(function (change) {
            var square = document.getElementById(change.serial);
            if (change.neutral === "no")
            {
                var color = change.color;
                square.style.backgroundColor = "rgb(" +color[0] + "," + color[1] + "," + color[2] + ")";
            }
            else
                square.style.backgroundColor = "rgb(255,255,255)";
    })
    }
}

function initBoard() {
    $.ajax(
        {
            url: GAMES_LIST_URL,
            data: {
                action: "showBoard",
                page : "three"
            },
            type: 'POST',
            error: function() {
                console.error("Failed to submit");
            },
            success: function(boardInfo) {
                createBoard(boardInfo);
            }
        }
    );
}

function createBoard(boardInfo)
{
    var board = boardInfo.board;
    var rows = boardInfo.rows;
    var cols = boardInfo.columns;

    for (var i = 0; i < rows; i++) { // creates squares + row blocks.
        var rowDiv = document.createElement('div');
        rowDiv.classList.add('rowDiv');
        var rowSquares = document.createElement('div');
        rowSquares.classList.add('rowSquares');
        rowDiv.appendChild(rowSquares);

        for (var j = 0; j < cols; j++) { // add the squares.
            var squareDiv = document.createElement('div');
            squareDiv.classList.add('square');
            squareDiv.style.backgroundColor = "rgb(255,255,255)";
            squareDiv.id = board[i][j].serialNumber;
            var idDiv = document.createElement('div');
            idDiv.classList.add('squareData');
            idDiv.textContent = "ID: " + board[i][j].serialNumber;
            var minMightDiv = document.createElement('div');
            minMightDiv.textContent = "Min Might: " + board[i][j].minimumMight;
            minMightDiv.classList.add('squareData');
            var rewardsDiv = document.createElement('div');
            rewardsDiv.textContent = "Rewards: " + board[i][j].roundRewards;
            rewardsDiv.classList.add('squareData');
            squareDiv.appendChild(idDiv);
            squareDiv.appendChild(minMightDiv);
            squareDiv.appendChild(rewardsDiv);
            rowSquares.appendChild(squareDiv);
        }
        document.getElementById("board").appendChild(rowDiv);
    }

    /*for (var k = 0; k < rows; k++) {
        for( var m=0; m<cols; m++ )
        {
            var serial = board[k][m].serialNumber;
            var square = document.getElementById(serial);
            square.onclick = slotAction;
        }
    }*/
}

function slotAction(event) {
    var slot = event.currentTarget;
    var serialId = slot.id;
    $.ajax(
        {
            url: PLAYER_ACTION_URL,
            data: {
                serial: serialId,
                action: "getDirection"
            },
            type: 'GET',
            error: function() {
                console.error("Failed to submit");
            },
            success: function(json) {
                directAction(json);
            }
        }
    );
}


function directAction(json) {
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    if (json.error==="")
    {
        var direct = json.direct;
        switch(direct){
            case "neutral":
                buildShop(json);
                break;
            case "owned":
                actionOnOwned(json);
                break;
            case "foreign":
                attackOccupied(json);
                break;
        }
    }
    else
        alert(json.error);
}

function buildShop(json) {
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    var model = json.model;
    $("#shopform").empty();
    var div = $('.shopDiv')[0];
    div.style.display = "block";
    var input1 = document.createElement("input");
    input1.type = "hidden";
    input1.name = "serial";
    input1.value = json.serial;
    input1.id = "input1";
    var input2 = document.createElement("input");
    input2.type = "hidden";
    input2.name = "direct";
    input2.value = json.direct;
    input2.id = "input2";
    document.getElementById("shopform").appendChild(input1);
    document.getElementById("shopform").appendChild(input2);
    model.forEach(function (unit) {
        var div1 = document.createElement("div");
        div1.classList.add("row");
        div1.textContent = "Unit Type: "+unit.typeOfUnit;
        var div2 = document.createElement("div");
        div2.classList.add("row");
        div2.textContent = "Maximum Might: "+unit.maximumMight;
        var div3 = document.createElement("div");
        div3.classList.add("row");
        div3.textContent = "Cost: "+unit.cost;
        var div4 = document.createElement("div");
        div4.classList.add("row");
        div4.textContent = "How Many Units Whould You Like To Buy?" ;
        var input = document.createElement("input");
        input.type = "number";
        input.name = unit.typeOfUnit;
        var h4 = document.createElement("h4");
        h4.classList.add("row");
        h4.textContent= "  ";
        document.getElementById("shopform").appendChild(div1);
        document.getElementById("shopform").appendChild(div2);
        document.getElementById("shopform").appendChild(div3);
        document.getElementById("shopform").appendChild(div4);
        document.getElementById("shopform").appendChild(input);
        document.getElementById("shopform").appendChild(h4);
    })
    var button = document.createElement("input");
    button.classList.add("button");
    button.type = "submit";
    button.value = "BUY";
    document.getElementById("shopform").appendChild(button);
}

function removeAttackPopUp() {
    $('.attackDiv')[0].style.display = "none";
}

function removeShopPopUp() {
    $('.shopDiv')[0].style.display = "none";
}

function removeOnOwnedPopUp() {
    $('.actionOnOwnedDiv')[0].style.display = "none";
}

function removeConcludingPopUp() {
    $('.concludingDiv')[0].style.display = "none";
}

function actionOnOwned(json)
{
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    var occupying = json.occupyingArmy;
    $("#territoryUnits").empty();
    var div = $('.actionOnOwnedDiv')[0];
    div.style.display = "block";
    occupying.forEach(function (unit) {
        var li = document.createElement('li');
        var div1 = document.createElement('div');
        div1.classList.add("row");
        var div2 = document.createElement('div');
        div2.classList.add("row");
        div1.textContent = "Type: " + unit.typeOfUnit;
        div2.textContent = "HP/Max: " + unit.HP + "/" + unit.maximumMightMulti;
        var space = document.createElement('h4');
        space.textContent = "   ";
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(space);
        document.getElementById("territoryUnits").appendChild(li);
    });
    var healDiv = document.createElement("div");
    healDiv.classList.add("row");
    healDiv.textContent = "Cost to heal all units to full form: " + json.healCost;
    var strengthenButton = document.getElementById("strengthen");
    strengthenButton.onclick = function () {
        strengthenTerritory(json);
    };
    var healButton = document.getElementById("heal");
    healButton.onclick= function () {
        healTerritoryUnits(json);
    };
    strengthenButton.classList.add("button");
    healButton.classList.add("button");


}

function strengthenTerritory(json)
{
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    json.direct = "strengthen";
    removeOnOwnedPopUp();
    buildShop(json);
}


function healTerritoryUnits(json)
{
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;

    removeOnOwnedPopUp();
    $.ajax(
        {
            url: SHOP_URL,
            data: {
                serial: json.serial,
                direct: "heal"
            },
            type: 'GET',
            error: function() {
                console.error("Failed to submit");
            },
            success: function(json) {
                buildConcludingPopUp(json);
            }
        }
    );
}


function attackOccupied(json)
{
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    var turing = json.attackerTuring;
    var minMight = json.minMight;
    $("#attackerTuring").empty();
    $("#minMightRequired").empty();
    var div = $('.attackDiv')[0];
    div.style.display = "block";
    var attackerTuring = document.getElementById("attackerTuring");
    attackerTuring.textContent = "Your current Turing: "+ turing;
    var minMightRequired = document.getElementById("minMightRequired");
    minMightRequired.textContent = "Minimum Might Required: "+ minMight;
    var luckyButton = document.getElementById("lucky");
    luckyButton.onclick = function () {
        luckyAttack(json);
    };
    var orchestratedButton = document.getElementById("orchestrated");
    orchestratedButton.onclick= function () {
        orchestratedAttack(json);
    };
    luckyButton.classList.add("button");
    orchestratedButton.classList.add("button");
}

function luckyAttack(json) {
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    json.direct = "lucky";
    removeAttackPopUp();
    buildShop(json);

}

function orchestratedAttack(json) {
    //             error;
    //             direct;
    //             serial;
    //             model;
    //             occupyingArmy;
    //             attackerTuring;
    //             healCost;
    //             minMight;
    json.direct = "orchestrated";
    removeAttackPopUp();
    buildShop(json);
}


function showUnits() {
    $.ajax(
        {
            url: GAMES_LIST_URL,
            data: {
                action: "showUnits",
                gametitle:""
            },
            type: 'POST',
            error: function() {
                console.error("Failed to submit");
            },
            success: function(model) {
                createUnits(model);
            }
        }
    );
}

function createUnits(model)
{
    $("#unitslist").empty();
    model.forEach(function (unit) {
        var li = document.createElement('li');
        var div1 = document.createElement('div');
        div1.classList.add("row");
        var div2 = document.createElement('div');
        div2.classList.add("row");
        var div3 = document.createElement('div');
        div3.classList.add("row");
        var div4 = document.createElement('div');
        div4.classList.add("row");
        var div5 = document.createElement('div');
        div5.classList.add("row");
        var div6 = document.createElement('div');
        div6.classList.add("row");
        div1.textContent = "Type: " + unit.typeOfUnit;
        div2.textContent = "Rank: " + unit.rank;
        div3.textContent = "Unit Cost: "+unit.cost;
        div4.textContent = "Cost Per Single FP: "+unit.costPerSingle;
        div5.textContent = "Maximum Fire Power: "+unit.maximumMight;
        div6.textContent = "Fatigue Factor: " + unit.fatigueFactor;
        var space = document.createElement('h2');
        space.textContent = "   ";
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(div3);
        li.appendChild(div4);
        li.appendChild(div5);
        li.appendChild(div6);
        li.appendChild(space);
        document.getElementById("unitslist").appendChild(li);
    })
}



function helloPlayer() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "getplayer"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(json) {
            var helloDiv = document.getElementById("hello");
            helloDiv.textContent = "Hello " + json.playerName + ", Welcome to " + json.currentGameTitle;
        }
    });
}

function triggerStatusTimeOut()
{
    setTimeout(ajaxStatus, refreshRate);
}

function ajaxStatus() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "getStatus"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(json) {
            var stat = document.getElementById("gameStat");
            var status = json.stat;
            if (status ==="Not Active") {
                stat.textContent= "Game Status: Not Active";
                triggerStatusTimeOut();
            }
            else{
                stat.textContent = "Game Status: Active";
                if(json.userStat==="player")
                {
                    alert("WOO-HOO! FINALLY! Let's get CONQUERING!");
                    startGame();
                }
            }
        }
    });
}

function startGame() {
    $.ajax(
        {
            url: GAME_FLOW_URL,
            data:{
                action: "startGame"
            },
            type: 'POST',
            success: function(r) {
            },
            error: function() {
                console.error("Failed to submit");
            }
        }
    );
}

function ajaxPlayersList() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "allplayers"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(players) {
            refreshPlayersList(players);
        }
    });
}

function ajaxObserversList() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "allobservers"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(observers) {
            refreshObserversList(observers);
        }
    });
}

function ajaxGameInfo() {
    $.ajax({
        url: GAME_INFO_URL,
        data:{
            action: "playerAndRound"
        },
        error: function() {
            console.error("Failed to submit");
        },
        success: function(json) {
            refreshGameInfo(json);
        }
    });
}

function refreshGameInfo(json)
{
    var currentPlayer = document.getElementById("currentplayer");
    var rounds = document.getElementById("rounds");
    currentPlayer.textContent = "Current Player: " + json.currentPlayer;
    rounds.textContent = "Round: " + json.currentRound + "/" + json.maxRound;
}

function refreshPlayersList(players) {
    //clear all current users
    $("#playerslist").empty();
    players.forEach(function (player) {
        var li = document.createElement('li');
        var div1 = document.createElement('div');
        div1.classList.add("row");
        var div2 = document.createElement('div');
        div2.classList.add("row");
        var div3 = document.createElement('div');
        div3.classList.add("row");
        var squareDiv = document.createElement('div');
        squareDiv.classList.add('colorSquare');
        div1.textContent = "Name: " + player.name;
        div2.textContent = "Color: ";
        squareDiv.style.backgroundColor = "rgb(" + player.color[0] + "," + player.color[1]+","+player.color[2]+")";
        div2.appendChild(squareDiv);
        div3.textContent = "Total Turing: "+player.tempTotalTuring;
        var space = document.createElement("h3");
        space.textContent = "    ";
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(div3);
        li.appendChild(space);
        document.getElementById("playerslist").appendChild(li);
    })
}

function refreshObserversList(observers) {
    $("#observerslist").empty();
    observers.forEach(function (observer) {
        var li = document.createElement('li');
        var div1 = document.createElement('div');
        div1.textContent = "Name: " + observer;
        var space = document.createElement("h3");
        space.textContent = "    ";
        li.appendChild(div1);
        li.appendChild(space);
        document.getElementById("observerslist").appendChild(li);
    })
}