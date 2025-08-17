function showCreateRoom() {
  const roomId = "ROOM" + Math.floor(Math.random() * 1000000);
  document.getElementById("generated-id").innerText = "#" + roomId;
  showPopup("create-popup");
  sessionStorage.setItem("roomId", roomId);
}

function showJoinRoom() {
  showPopup("join-popup");
}

function showPopup(id) {
  document.getElementById(id).classList.add("show");
}

function closePopup() {
  document
    .querySelectorAll(".popup")
    .forEach((p) => p.classList.remove("show"));
}

function enterGameRoom() {
  const username = document.getElementById("create-username").value.trim();
  const roomId = sessionStorage.getItem("roomId");
  if (username) {
    window.location.href = `../game page/game.html?room=${roomId}&user=${encodeURIComponent(
      username
    )}`;
  } else {
    alert("Enter your name to proceed.");
  }
}

function joinGameRoom() {
  const roomId = document.getElementById("join-room-id").value.trim();
  const username = document.getElementById("username").value.trim();
  if (username && roomId) {
    window.location.href = `../game page/game.html?room=${roomId}&user=${encodeURIComponent(
      username
    )}`;
  } else {
    alert("Enter both Room ID and your name.");
  }
}
