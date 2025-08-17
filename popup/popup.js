function showPopup(id) {
  document.getElementById(id).classList.add("show");
}

function closePopup() {
  document
    .querySelectorAll(".popup")
    .forEach((p) => p.classList.remove("show"));
}