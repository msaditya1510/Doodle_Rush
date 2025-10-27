# 🎨 Doodle Rush  
**Real-Time Multiplayer Drawing & Guessing Game**

---

## 📖 Project Overview  
Doodle Rush is a Pictionary-style web game where players join a room, one player draws a hidden word, and others guess in real time. The game features instant synchronization, private word delivery, automated rounds, and live score tracking.

---

## 🎯 Objective  
To deliver a fast, fun, and responsive real-time drawing and guessing experience with a clean UI, reliable backend, and seamless multiplayer interaction.

---

## 🛠️ Tech Stack

### Frontend:
- **Original:** HTML5, CSS3, JavaScript, HTML5 Canvas  
- **Converted:** React.js (functional components, hooks), Tailwind CSS  

### Backend:
- Spring Boot (Java) with Java WebSockets (STOMP)  

### Real-Time Communication:
- SockJS + STOMP.js (WebSocket with fallback)  

### Build Tools:
- npm (Frontend), Maven (Backend)  

### Development Tools:
- VS Code, Spring Tool Suite (STS), Postman, Chrome DevTools, GitHub  

---

## 🎮 How It Works

### Player Flow:
1. **Join or Create a Room** – Enter a room code and see the player list.
2. **Lobby & Ready** – Host starts the game when all players are ready.
3. **Round Begins** – One player becomes the drawer and receives a secret word privately.
4. **Draw & Guess** – The drawer sketches on a shared canvas while others type guesses in the chat.
5. **Correct Guess** – System announces the correct guess, awards points, and updates the leaderboard.
6. **Round Transition** – The game automatically rotates the drawer and starts the next round.
7. **Game End** – After all rounds, final scores are shown. Host can restart or exit.

### System Flow:
- **Session Setup** – Tracks participants and assigns a host.
- **Real-Time Sync** – Broadcasts drawing strokes, chat messages, and game events.
- **Authoritative Control** – Server manages timers, drawer assignment, and scoring.
- **Private Delivery** – Only the drawer receives the secret word.
- **State Handling** – Active game state is stored temporarily; final results are saved.
- **Cleanup & Resilience** – Inactive players are removed; reconnect logic is supported.

---

## ✅ Key Features

- ✅ Real-time drawing synchronization  
- ✅ Private word delivery to the drawer  
- ✅ In-game chat for guesses and social interaction  
- ✅ Automated rounds and drawer rotation  
- ✅ Live updating leaderboard  
- ✅ Responsive UI (React + Tailwind CSS)  

---

## 🚀 How to Run

### Prerequisites:
- Node.js & npm (for frontend)
- Java & Maven (for backend)

### Steps:
1. Clone the repository  
2. Start the backend Spring Boot server  
3. Start the React frontend  
4. Open the app in a browser and create/join a room  

---

## 📌 Conclusion  
Doodle Rush successfully demonstrates how real-time interactive games can be built using a modern web stack. The transition from vanilla JS to React improved maintainability without compromising performance. The game offers smooth synchronization, fair scoring, and a fully automated gameplay flow.

---

## 📚 References  
- [React.js Docs](https://reactjs.org/)  
- [HTML5 Canvas API](https://developer.mozilla.org/en-US/docs/Web/API/Canvas_API)  
- [Spring Boot WebSocket Guide](https://spring.io/guides/gs/messaging-stomp-websocket/)  
- [SockJS & STOMP.js Docs](https://github.com/sockjs/sockjs-client)  

---

Let the doodling begin! 🖌️🎉
```

This README is structured to be clear, engaging, and informative — perfect for GitHub or any project hosting platform. Let me know if you'd like a version for a specific platform or if you want to add badges, screenshots, or a demo link!
