// UI.jsx
import React, { useState, useEffect } from "react";
import GameHeader from "../GameHeader.jsx";
import CanvasArea from "./CanvasArea.jsx";
import ChatArea from "./ChatArea.jsx";
import API_ENDPOINTS from "../../config/apiConfig";

function UI({
  playerName,
  roomId,
  canvasRef,
  chatEndRef,
  chatMessages,
  chatInput,
  setChatInput,
  handleKeyPress,
  sendMessage,
  startDrawing,
  stopDrawing,
  draw,
  clearCanvas,
  color,
  setColor,
  brushSize,
  setBrushSize,
  isDrawer,
  setIsDrawer,
  currentWord,
  setCurrentWord,
  setShowStartRound,
  showStartRound,
  players,
  showGameOver,
  setShowGameOver,
  roundActive,
}) {
  const [drawerName, setDrawerName] = useState(localStorage.getItem("drawerName"));
  const [gameStarted, setGameStarted] = useState(false);
  const hostName = localStorage.getItem("hostName");

 const handleLeaveGame = () => {
    if (window.confirm("Are you sure you want to leave the game?")) {
        const url = API_ENDPOINTS.LEAVE_GAME(playerName);
        console.log("🔄 Making DELETE request to:", url);
        
        fetch(url, {
            method: "DELETE", // Changed to DELETE
        })
        .then(response => {
            console.log("📡 Response status:", response.status);
            if (response.ok) {
                localStorage.removeItem("playerName");
                localStorage.removeItem("roomId");
                localStorage.removeItem("hostName");
                localStorage.removeItem("drawerName");
                window.location.href = "/";
            } else {
                return response.text().then(text => {
                    alert("Error leaving game: " + text);
                });
            }
        })
        .catch(error => {
            console.error("❌ Network error:", error);
            alert("Network error leaving game");
        });
    }
};
  const handleStartGame = () => {
    fetch(API_ENDPOINTS.START_GAME(roomId, playerName), {
      method: "POST",
    })
      .then((res) => res.text())
      .then((data) => {
        console.log("✅ Game started:", data);
        localStorage.setItem("drawerName", data);
        setDrawerName(data);
        setGameStarted(true);
      })
      .catch((err) => console.error("❌ Error starting game:", err));
  };

  const handleStartRound = () => {
    fetch(API_ENDPOINTS.START_ROUND(roomId, playerName), {
      method: "POST",
    })
      .then((res) => {
        if (!res.ok) {
          throw new Error("Only drawer can start the round");
        }
        return res.text();
      })
      .then((data) => {
        console.log("✅ Round started:", data);
        setCurrentWord(data);
        setIsDrawer(true);
        setShowStartRound(false);
      })
      .catch((err) => {
        console.error("❌ Error starting round:", err);
        alert(err.message);
      });
  };

  // Add this useEffect to handle automatic round transitions
  useEffect(() => {
    const handleRoundMessages = () => {
      const lastMessage = chatMessages[chatMessages.length - 1];
      if (lastMessage && lastMessage.type === "SYSTEM") {
        if (lastMessage.content.includes("Drawer for this round is")) {
          setGameStarted(true);
          const newDrawerName = lastMessage.content.replace("Drawer for this round is ", "");
          localStorage.setItem("drawerName", newDrawerName);
          setDrawerName(newDrawerName);
          setIsDrawer(false);
        }
        
        if (lastMessage.content.includes("Round-") && lastMessage.content.includes("started")) {
          setGameStarted(true);
        }

        if (lastMessage.content.includes("Round-") && lastMessage.content.includes("ended")) {
          if (canvasRef.current) {
            const canvas = canvasRef.current;
            canvas.getContext("2d").clearRect(0, 0, canvas.width, canvas.height);
          }
          setIsDrawer(false);
          setCurrentWord("");
        }
        
        if (lastMessage.content.includes("Game Over")) {
          setGameStarted(false);
        }
      }
    };

    handleRoundMessages();
  }, [chatMessages, playerName, roomId, canvasRef, setIsDrawer, setCurrentWord]);

  // Game Over Screen
  if (showGameOver) {
    return (
      <div className="min-h-screen bg-gray-900 text-white flex flex-col items-center justify-center">
        <GameHeader playerName={playerName} roomId={roomId} />
        
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-red-400 mb-4">Game Over!</h1>
          <h2 className="text-2xl font-semibold text-yellow-400 mb-6">Final Leaderboard</h2>
        </div>

        <div className="w-full max-w-md bg-gray-800 rounded-lg p-6 mb-8">
          {players.map((player, index) => (
            <div
              key={index}
              className={`flex justify-between items-center py-3 px-4 my-2 rounded-xl text-lg font-semibold ${
                index === 0
                  ? "text-yellow-400 bg-yellow-400/20"
                  : index === 1
                  ? "text-gray-300 bg-gray-300/20"
                  : index === 2
                  ? "text-orange-400 bg-orange-400/20"
                  : "text-white bg-gray-700/50"
              }`}
            >
              <span className="flex items-center gap-2">
                {index + 1}. {player.name}
                {index === 0 && " 🥇"}
                {index === 1 && " 🥈"}
                {index === 2 && " 🥉"}
              </span>
              <span>{player.score} pts</span>
            </div>
          ))}
        </div>

        <div className="flex gap-4">
          <button
            onClick={() => {
              setShowGameOver(false);
              setGameStarted(false);
            }}
            className="px-6 py-3 bg-green-500 hover:bg-green-600 rounded-lg text-white shadow-lg transition"
          >
            Play Again
          </button>
          <button
            onClick={handleLeaveGame}
            className="px-6 py-3 bg-red-500 hover:bg-red-600 rounded-lg text-white shadow-lg transition"
          >
            Leave Game
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-900 text-white relative">
      {/* Header */}
      <GameHeader playerName={playerName} roomId={roomId} />

      {/* Room ID display */}
      <div className="my-6 mx-auto w-max bg-yellow-400 text-black font-bold text-xl px-6 py-3 rounded-full shadow-lg text-center">
        Room ID: {roomId}
      </div>

      {/* Buttons */}
      <div className="flex justify-center items-center gap-4 mb-6 flex-wrap">
        {/* Start Game Button - Only for host when game hasn't started */}
        {playerName === hostName && !showStartRound && !isDrawer && !gameStarted && (
          <button
            onClick={handleStartGame}
            className="px-6 py-3 bg-gradient-to-r from-green-500 to-emerald-600 rounded-xl text-white font-bold shadow-lg hover:from-green-600 hover:to-emerald-700 transform hover:scale-105 transition-all duration-200 border-2 border-emerald-400"
          >
            Start Game
          </button>
        )}

        {/* Start Round Button - Only for drawer when round is ready to start */}
        {showStartRound && (
          <button
            onClick={handleStartRound}
            className="px-6 py-3 bg-gradient-to-r from-blue-500 to-purple-600 rounded-xl text-white font-bold shadow-lg hover:from-blue-600 hover:to-purple-700 transform hover:scale-105 transition-all duration-200 border-2 border-purple-400 animate-pulse"
          >
            Start Round
          </button>
        )}

        {/* Leave Game Button - Always visible for all players */}
        <button
          onClick={handleLeaveGame}
          className="px-6 py-3 bg-gradient-to-r from-red-500 to-pink-600 rounded-xl text-white font-bold shadow-lg hover:from-red-600 hover:to-pink-700 transform hover:scale-105 transition-all duration-200 border-2 border-pink-400"
        >
          Leave Game
        </button>

        {/* Drawer's Word Display - Only shown to the current drawer */}
        {isDrawer && currentWord && (
          <div className="px-6 py-3 bg-gradient-to-r from-purple-600 to-indigo-700 border-2 border-purple-400 rounded-xl text-white font-bold shadow-lg animate-pulse">
            🎨 Your Word: <span className="text-yellow-300">{currentWord}</span>
          </div>
        )}
        
        {/* Guesser's Prompt - Shown to non-drawers during active round */}
        {!isDrawer && roundActive && (
          <div className="px-6 py-3 bg-gradient-to-r from-gray-700 to-gray-800 border-2 border-gray-500 rounded-xl text-white font-bold shadow-lg">
            🔍 Guess the word!
          </div>
        )}

        {/* Waiting for Drawer - Shown when round is prepared but not started */}
        {!isDrawer && !roundActive && gameStarted && (
          <div className="px-6 py-3 bg-gradient-to-r from-amber-600 to-orange-700 border-2 border-amber-400 rounded-xl text-white font-bold shadow-lg">
            ⏳ Waiting for drawer to start...
          </div>
        )}
      </div>

      {/* Layout */}
      <div className="container mx-auto flex flex-col md:flex-row gap-6 px-4 py-6">
        <CanvasArea
          canvasRef={canvasRef}
          startDrawing={startDrawing}
          stopDrawing={stopDrawing}
          draw={draw}
          clearCanvas={clearCanvas}
          color={color}
          setColor={setColor}
          brushSize={brushSize}
          setBrushSize={setBrushSize}
          isDrawer={isDrawer}
          roundActive={roundActive}
        />

        <ChatArea
          chatMessages={chatMessages}
          chatEndRef={chatEndRef}
          chatInput={chatInput}
          setChatInput={setChatInput}
          handleKeyPress={handleKeyPress}
          sendMessage={sendMessage}
          isDrawer={isDrawer}
          playerName={playerName}
          currentWord={currentWord}
        />
      </div>
    </div>
  );
}

export default UI;