var refreshRate = 2000; //milli seconds
var USER_LIST_URL = buildUrlWithContextPath("userslist");
var GAMES_LIST_URL = buildUrlWithContextPath("gameslist");
var NEW_GAME_URL = buildUrlWithContextPath("newgame");
var JOIN_AS_PLAYER_URL = buildUrlWithContextPath("joingameasplayer");
var GAME_ROOM_URL = "../gameroom/gameroom.html";


function loadGameClicked(event) {
    var file = event.target.files[0];
    var reader = new FileReader();
    reader.onload = function () {
        var content = reader.result;
        $.ajax(
            {
                url: NEW_GAME_URL,
                data: {
                    action: "loadGame",
                    file: content
                },
                type: 'POST',
                success: addGameToList
            }
        );
    };
    reader.readAsText(file);
}

function addGameToList(json) {
    document.getElementById("fileInput").value = "";
    if (json.errorM ==="")
        alert(json.creator + " Uploaded a new game successfully! Wow!");
    else
        alert(json.errorM);
}

function ajaxGamesList() {
    $.ajax({
        url: GAMES_LIST_URL,
        data:{
            action: "refresh"
        },
        success: function(games) {
            refreshGamesList(games);
        }
    });
}

function refreshGamesList(games) {
    $("#gameslist").empty();
    games.forEach(function (game) {
        var first = document.createElement('div');
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
        div6.classList.add("playerAndObserverBtns");
        var div7 = document.createElement('div');
        div7.classList.add("row");
        div5.classList.add("clickable");
        div4.classList.add("clickable");
        var firstCol= document.createElement('div');
        firstCol.classList.add("col-md-4");
        firstCol.classList.add("titleAndStatusCol");
        firstCol.classList.add("titleClass");
        var content = document.createTextNode(game.gameTitle);
        firstCol.appendChild(content);
        var secondCol= document.createElement('div');
        secondCol.classList.add("col-md-4");
        secondCol.classList.add("statusClass");
        secondCol.classList.add("titleAndStatusCol");
        content = document.createTextNode(game.activeST);
        secondCol.appendChild(content);
        div2.textContent = "Added By " + game.creator;
        div3.textContent = "Registered/Required:  "+game.registered+"/"+game.totalPlayers;
        div7.textContent = "Observers: "+ game.observers;
        div4.textContent = "Click to view playable army units";
        div5.textContent = "Click to view game board";
        div4.id = "clickUnits";
        div5.id = "clickBoard";
        var playerButton = document.createElement("button");
        playerButton.innerHTML = "Join as Player";
        playerButton.classList.add("playerBtns");
        var observerButton = document.createElement("button");
        observerButton.innerHTML = "Join as Observer";
        observerButton.classList.add("observerBtns");
        if(game.activeST==="Active")
            playerButton.disabled=true;
        else
            playerButton.disabled=false;
        div6.appendChild(playerButton);
        div6.appendChild(observerButton);
        playerButton.onclick = function(){
            joinAsPlayer(game.gameTitle);
        };
        observerButton.onclick = function(){
            joinAsObserver(game.gameTitle);
        };
        div4.onclick = function(){
            showUnitsPopUp(game.gameTitle);
        };
        div5.onclick = function (){
            showBoardPopUp(game.gameTitle);
        };
        var space = document.createElement("h3");
        space.textContent= "  ";
        div1.appendChild(firstCol);
        div1.appendChild(secondCol);
        first.appendChild(div1);
        first.appendChild(div2);
        first.appendChild(div3);
        first.appendChild(div7);
        first.appendChild(div4);
        first.appendChild(div5);
        first.appendChild(div6);
        first.appendChild(space);
        document.getElementById("gameslist").appendChild(first);
    })
}

function joinAsPlayer(title) {
    $.ajax(
        {
            url: JOIN_AS_PLAYER_URL,
            data: {
                action: "joinasplayer",
                gametitle: title
            },
            type: 'POST',
            success: function (json) {
                window.location = GAME_ROOM_URL;
            }
        }
    );
}


function joinAsObserver(title) {
    $.ajax(
        {
            url: JOIN_AS_PLAYER_URL,
            data: {
                action: "joinasobserver",
                gametitle: title
            },
            type: 'POST',
            success: function (json) {
                window.location = GAME_ROOM_URL;
            }
        }
    );
}

function showUnitsPopUp(title) {
    $.ajax(
        {
            url: GAMES_LIST_URL,
            data: {
                action: "showUnits",
                gametitle: title
            },
            type: 'POST',
            success: function(model) {
                createUnitsPopUp(model);
            }
        }
    );
}

function removeUnitsPopUp() {
    $('.unitsDiv')[0].style.display = "none";
}

function removeBoardPopUp() {
    $('.boardDiv')[0].style.display = "none";
}

function createUnitsPopUp(model)
{
    $("#unitsList").empty();
    var div = $('.unitsDiv')[0];
    div.style.display = "block";
    model.forEach(function (unit) {
        var li = document.createElement('div');
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
        var space =document.createElement("h3");
        space.textContent = "  ";
        li.appendChild(div1);
        li.appendChild(div2);
        li.appendChild(div3);
        li.appendChild(div4);
        li.appendChild(div5);
        li.appendChild(div6);
        li.appendChild(space);
        document.getElementById("unitsList").appendChild(li);
    })
}


function showBoardPopUp(title) {
    $.ajax(
        {
            url: GAMES_LIST_URL,
            data: {
                action: "showBoard",
                gametitle: title,
                page: "two"
            },
            type: 'POST',
            success: function(boardInfo) {
                createBoardPopUp(boardInfo);
            }
        }
    );
}

function createBoardPopUp(boardInfo)
{
    $("#board").empty();
    var div = $('.boardDiv')[0];
    div.style.display = "block";
    var board = boardInfo.board;
    var rows = boardInfo.rows;
    var cols = boardInfo.columns;

    for (i = 0; i < rows; i++) { // creates squares + row blocks.
        var rowDiv = document.createElement('div');
        rowDiv.classList.add('rowDiv');
        var rowSquares = document.createElement('div');
        rowSquares.classList.add('rowSquares');
        rowDiv.appendChild(rowSquares);

        for (j = 0; j < cols; j++) { // add the squares.
            var squareDiv = document.createElement('div');
            squareDiv.classList.add('square');
            var idDiv = document.createElement('div');
            //idDiv.classList.add('row');
            idDiv.classList.add('squareData');
            idDiv.textContent = "ID: " +  board[i][j].serialNumber;
            var minMightDiv = document.createElement('div');
            //minMightDiv.classList.add('row');
            minMightDiv.textContent = "Min Might: " + board[i][j].minimumMight;
            minMightDiv.classList.add('squareData');
            var rewardsDiv = document.createElement('div');
            //rewardsDiv.classList.add('row');
            rewardsDiv.textContent = "Rewards: " + board[i][j].roundRewards;
            rewardsDiv.classList.add('squareData');
            squareDiv.appendChild(idDiv);
            squareDiv.appendChild(minMightDiv);
            squareDiv.appendChild(rewardsDiv);
            rowSquares.appendChild(squareDiv);
        }
        document.getElementById("board").appendChild(rowDiv);
    }
}

function refreshUsersList(users) {
    //clear all current users
    $("#userslist").empty();

    // rebuild the list of users: scan all users and add them to the list of users
    $.each(users || [], function(index, username) {
        $('<li>' + username + '</li>').appendTo($("#userslist"));
    });
}

function ajaxUsersList() {
    $.ajax({
        url: USER_LIST_URL,
        data:{
            action: "allusers"
        },
        success: function(users) {
            refreshUsersList(users);
        }
    });
}


//activate the timer calls after the page is loaded
$(function() {
    //The users list is refreshed automatically every second
    $.ajax({
        url: USER_LIST_URL,
        data:{
            action: "getuser"
        },
        success: function(username) {
            var nameDiv = document.getElementById("username");
            nameDiv.textContent = "You Are Logged In as: " + username;
        }
    });
    setInterval(ajaxUsersList, refreshRate);
    setInterval(ajaxGamesList,refreshRate);
});