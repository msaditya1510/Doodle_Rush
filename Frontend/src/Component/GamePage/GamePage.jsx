// GamePage.jsx
import React, { useRef, useState, useEffect } from "react";
import GameHeader from "../GameHeader.jsx";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import UI from "./UI.jsx";

function GamePage() {
  const canvasRef = useRef(null);
  const chatEndRef = useRef(null);

  const [isDrawer, setIsDrawer] = useState(false);
  const [currentWord, setCurrentWord] = useState("");
  const [showStartRound, setShowStartRound] = useState(false);
  // Canvas states
  const [drawing, setDrawing] = useState(false);
  const [color, setColor] = useState("#ff00ff");
  const [brushSize, setBrushSize] = useState(4);
  const lastPointRef = useRef(null);

  // Chat states
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState("");
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [roundActive, setRoundActive] = useState(false);
  const [gameActive, setGameActive] = useState(false);
  const gameActiveRef = useRef(false);
  useEffect(() => { gameActiveRef.current = gameActive; }, [gameActive]);
  // name -> { until: number, contents: Set<string> }
  const suppressGuessesRef = useRef(new Map());
  
  // Scoreboard state
  const [players, setPlayers] = useState([]);
  const [showGameOver, setShowGameOver] = useState(false);

  const playerName = localStorage.getItem("playerName") || "Anonymous";
  const roomId = localStorage.getItem("roomId");
/* ------------------------- CONNECT TO BACKEND ------------------------- */
  useEffect(() => {
    const socket = new SockJS(`${import.meta.env.VITE_API_URL}/ws`);
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log(str),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      setConnected(true);
      console.log(`✅ Connected as ${playerName} in room ${roomId}`);

 client.subscribe(`/topic/room/${roomId}`, (message) => {
  const msg = JSON.parse(message.body);
  console.log("📨 Received message:", msg); // Debug log

  // Handle SCORE_UPDATE messages
  if (msg.type === "SCORE_UPDATE") {
    try {
      const leaderboardData = JSON.parse(msg.content);
      console.log("📊 Real-time leaderboard update received");
      setPlayers(leaderboardData);
    } catch (e) {
      console.error("Failed to parse leaderboard update", e);
    }
    return;
  }

  // Handle TIME_UPDATE messages
  if (msg.type === "SYSTEM" && typeof msg.content === "string" && msg.content.startsWith("TIME_UPDATE:")) {
    const time = parseInt(msg.content.split(":")[1], 10);
    setTimeLeft(time);
    setTimerActive(time > 0);
    return;
  }

  // Handle private messages (for drawer word)
  if (msg.target && msg.target === playerName) {
    if (typeof msg.content === "string" && msg.content.trim().length > 0) {
      setCurrentWord(msg.content);
      setIsDrawer(true);
      setShowStartRound(false);
    }
    return;
  }

  if (msg.target && msg.target !== playerName) {
    return;
  }

  // Handle SYSTEM messages for game flow
  if (msg.type === "SYSTEM") {
    const content = msg.content || "";

    if (content.includes("Drawer for this round is")) {
      const newDrawerName = content.replace("Drawer for this round is ", "").trim();
      localStorage.setItem("drawerName", newDrawerName);
      const isNewDrawer = newDrawerName.toLowerCase() === playerName.toLowerCase();
      setShowStartRound(isNewDrawer);
      setIsDrawer(false);
      setCurrentWord("");
    }

    if (content.includes("Round-") && content.includes("started")) {
      const currentDrawer = localStorage.getItem("drawerName");
      const isCurrentDrawer = currentDrawer && currentDrawer.toLowerCase() === playerName.toLowerCase();
      setIsDrawer(isCurrentDrawer);
      setShowStartRound(false);
      setRoundActive(true);
    }

    if (content.includes("Round-") && content.includes("ended")) {
      const canvas = canvasRef.current;
      if (canvas) {
        canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height);
      }
      setIsDrawer(false);
      setCurrentWord("");
      setRoundActive(false);
    }

    if (content.includes("Game Over")) {
      setRoundActive(false);
      setGameActive(false);
      setIsDrawer(false);
      setCurrentWord("");
      setShowStartRound(false);
      setShowGameOver(true);
    }
  }

  // ✅ FIX: Handle JOIN messages properly
  if (msg.type === "JOIN" || msg.type === "LEAVE") {
    console.log("👥 Join/Leave message detected:", msg);
    // Add join/leave messages directly to chat
    setChatMessages((prev) => [...prev, msg]);
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
    return;
  }

  // Handle CHAT and other SYSTEM messages
  if (msg.type === "CHAT" || msg.type === "SYSTEM") {
    // Filter out messages that might reveal the word
    if (msg.content && typeof msg.content === "string") {
      const lowerContent = msg.content.toLowerCase();
      if (lowerContent.includes("guessed the word") || 
          lowerContent.includes("word is") ||
          lowerContent.includes("correct word")) {
        return;
      }
    }
    
    console.log("💬 Adding message to chat:", msg);
    setChatMessages((prev) => [...prev, msg]);
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }
});
      // ✅ Subscribe to drawing events
      client.subscribe(`/canvas/${roomId}`, (message) => {
        const draw = JSON.parse(message.body);
        drawOnCanvas(draw);
      });
      client.subscribe(`/topic/canvas/${roomId}`, (message) => {
        const draw = JSON.parse(message.body);
        drawOnCanvas(draw);
      });

      // Note: Private word is delivered on the room topic with target set to playerName
    };

    client.activate();
    setStompClient(client);

    return () => {
      client.deactivate();
    };
  }, [roomId]);

  /* ------------------------- CHAT FUNCTIONS ------------------------- */
  const sendMessage = () => {
    if (chatInput.trim() === "") return;
    
    // Check if current player is the drawer
    const currentDrawer = localStorage.getItem("drawerName");
    const isCurrentPlayerDrawer = currentDrawer && currentDrawer.toLowerCase() === (playerName || "").toLowerCase();
    
    // Drawer cannot send messages (check multiple conditions)
    if (isDrawer || currentWord || isCurrentPlayerDrawer) {
      alert("Drawer cannot guess the word!");
      setChatInput(""); // Clear input to prevent confusion
      return;
    }

    const chatMessage = {
      name: playerName,
      content: chatInput,
      type: "CHAT",
    };

    if (stompClient && connected) {
      stompClient.publish({
        destination: `/app/sendMessage/${roomId}`,
        body: JSON.stringify(chatMessage),
      });
    }

    setChatInput(""); // clear input after sending
  };

  const handleKeyPress = (e) => {
    if (e.key === "Enter") {
      sendMessage();
    }
  };

  /* ------------------------- CANVAS FUNCTIONS ------------------------- */
  const startDrawing = (e) => {
    if (!isDrawer) {
      alert("Wait for your turn to draw!");
      return; // Only drawer can draw
    }
    setDrawing(true);
    const rect = canvasRef.current.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    lastPointRef.current = { x, y };
  };

  const stopDrawing = () => {
    setDrawing(false);
    canvasRef.current.getContext("2d").beginPath();
    lastPointRef.current = null;
  };

  const draw = (e) => {
    if (!drawing || !isDrawer) {
      if (!isDrawer) {
        alert("Wait for your turn to draw!");
      }
      return; // Only drawer can draw
    }
    const canvas = canvasRef.current;
    const ctx = canvas.getContext("2d");
    const rect = canvas.getBoundingClientRect();
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;

    // Draw segment from last point to current
    const last = lastPointRef.current || { x, y };
    ctx.strokeStyle = color;
    ctx.lineWidth = brushSize;
    ctx.lineCap = "round";
    ctx.beginPath();
    ctx.moveTo(last.x, last.y);
    ctx.lineTo(x, y);
    ctx.stroke();
    lastPointRef.current = { x, y };

    // Send draw segment to backend (with backward-compatible fields)
    if (stompClient && connected) {
      const drawMessage = {
        startX: last.x,
        startY: last.y,
        endX: x,
        endY: y,
        color,
        thickness: brushSize,
        x,
        y,
        brushSize,
      };
      stompClient.publish({
        destination: `/app/canvas/${roomId}`,
        body: JSON.stringify(drawMessage),
      });
    }
  };

  // ✅ Apply received drawing event
  const drawOnCanvas = (draw) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext("2d");

    const { startX, startY, endX, endY, color: c, thickness } = draw;
    ctx.strokeStyle = c || draw.color;
    ctx.lineWidth = thickness || draw.brushSize || 4;
    ctx.lineCap = "round";
    ctx.beginPath();
    ctx.moveTo(startX ?? draw.x, startY ?? draw.y);
    ctx.lineTo(endX ?? draw.x, endY ?? draw.y);
    ctx.stroke();
  };

  const clearCanvas = () => {
    if (!isDrawer) return; // Only drawer can clear
    const canvas = canvasRef.current;
    canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height);
    // Optionally inform others to clear (if backend supports). Otherwise, each client clears on round end system message
  };

  /* ------------------------- UI ------------------------- */
  return (
    <UI
      playerName={playerName}
      roomId={roomId}
      canvasRef={canvasRef}
      chatEndRef={chatEndRef}
      chatMessages={chatMessages}
      chatInput={chatInput}
      setChatInput={setChatInput}
      handleKeyPress={handleKeyPress}
      sendMessage={sendMessage}
      startDrawing={startDrawing}
      stopDrawing={stopDrawing}
      draw={draw}
      clearCanvas={clearCanvas}
      color={color}
      setColor={setColor}
      brushSize={brushSize}
      setBrushSize={setBrushSize}
      isDrawer={isDrawer}
      setIsDrawer={setIsDrawer}
      currentWord={currentWord}
      setCurrentWord={setCurrentWord}
      setShowStartRound={setShowStartRound}
      showStartRound={showStartRound}
      players={players}
      showGameOver={showGameOver}
      setShowGameOver={setShowGameOver}
      roundActive={roundActive}
    />
  );
}

export default GamePage;