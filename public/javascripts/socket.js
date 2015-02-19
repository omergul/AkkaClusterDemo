
var websocket = null;
var myUsername = null;
var activeChatUser = null;
var allUsers = [];
var chatCards = {};
var talks = {};

function init() {
	$("#reset-link").hide();

    myUsername = getParameterByName("username");
    if(myUsername.trim().length == 0) {
        alert("Enter a username");
    }
    else {
		$("#username-link").html(myUsername);
        startSocketConnection(myUsername)
    }
}

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function startSocketConnection(username) {

	var wsUri = "ws://"+window.location.host+"/socket?username="+username;

    websocket = new WebSocket(wsUri);
    websocket.onopen = function(evt) {

        writeToScreen("<b>CONNECTED</b>");

        doSend({"event":"hi"});
        doSend({"event":"users"});
    };

    websocket.onclose = function(evt) {
        writeToScreen("<b>DISCONNECTED</b>");
    };

    websocket.onmessage = function(evt) {
		writeToScreen('<span style="color: blue;">'+evt.data+'</span>');

		var json = JSON.parse(evt.data);
		if(json.error) {
			return;
		}

		if(json.event == "talk") {
			addMessage(json.from, $("<p><b>"+json.from+":</b> "+json.message+"</p>"));
		}
		if(json.event == "users") {
			var $users = $("#users");
   			$users.empty();

   			allUsers = json.users;

			json.users.forEach(function(user) {

				var $li, spanStr;

				if(user.isOnline == "1") {
					spanStr = '<span class="dot" style="background-color:green;color:green;"></span>';
				}
				else {
					spanStr = '<span class="dot" style="background-color:red;color:red;"></span>';
				}

				$li = $('<li class="list-group-item" data-username="'+user.username+'">'+user.username+spanStr+'</li>');
				$li.click(function(){
					changeActiveUser(user);
				});

				$users.append($li);
			});

			// assign the firs user to the active user if we don't have one
			if(!activeChatUser && allUsers.length > 0) {
				changeActiveUser(allUsers[0]);
			}
			else {
				changeActiveUser(activeChatUser);
			}
		}
    };

    websocket.onerror = function(evt) {
        writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
    };

}

function changeActiveUser(user) {

	activeChatUser = user;

	$("#chat-column .panel-title").html("Chat <span style='float:right;'>"+user.username+"</span>");

	$("#chat .messages").hide();

	// store the old one
	if(!chatCards[user.username]) {
		$messages = $("<div class='messages' data='user-"+user.username+"'></div>");
		$messages.appendTo($("#chat"));
		chatCards[user.username] = $messages;
	}

	chatCards[user.username].show();
	$messages = chatCards[user.username];
	$messages.scrollTop($messages.prop("scrollHeight"));

	$("#users li").removeClass("active");
	$("#users li").each(function() {
		var usernameAttr = $(this).attr("data-username");
		if(usernameAttr.localeCompare(user.username) == 0) {
			$(this).addClass("active");
		}
	});
}

function doSend(message) {
	writeToScreen(JSON.stringify(message));
	websocket.send(JSON.stringify(message));
}

function writeToScreen(message) {
	$p = $("<p></p>");
	$p.html(message);

	$output = $("#output");
	$output.append($p);
	$output.scrollTop($output.prop("scrollHeight"));
}

function addMessage(username, html) {
	$("#chat .messages").each(function() {
		if($(this).attr("data") == "user-"+username) {
			$(this).append(html);
			$(this).scrollTop($(this).prop("scrollHeight"));
		}
	});
}

function submitForm() {

	var text = $("#input-message").val();
	if(text.trim().length == 0) {
		$("#input-message").val("");
		return false;
	}

	// send to servers
	doSend({"event":"talk", "message":text, "from":myUsername, "to":activeChatUser.username});

	// append to screen
	addMessage(activeChatUser.username, $("<p><b>me:</b> "+text+"</p>"));

	// clear input
	$("#input-message").val("");

    $messages.scrollTop($messages.prop("scrollHeight"));

	return false;
}

window.addEventListener("load", init, false);