// API Configuration - Central place for all backend URLs
// const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const API_BASE_URL = 'https://backend-s4i4.onrender.com';

const API_ENDPOINTS = {
  // Player endpoints
  LEAVE_GAME: (playerName) => `${API_BASE_URL}/players/leave?playerName=${encodeURIComponent(playerName)}`,
  JOIN_GAME: `${API_BASE_URL}/players/join`,
  GET_PLAYERS_IN_ROOM: (roomId) => `${API_BASE_URL}/players/room/${roomId}`,
  
  // Game endpoints
  START_GAME: (roomId, playerName) => `${API_BASE_URL}/game/startGame/${roomId}/${playerName}`,
  START_ROUND: (roomId, playerName) => `${API_BASE_URL}/game/startRound/${roomId}/${playerName}`,
  END_GAME: (roomId) => `${API_BASE_URL}/game/end/${roomId}`,
  SUBMIT_GUESS: (roomId) => `${API_BASE_URL}/game/${roomId}/guess`,
  
  // Leaderboard endpoints
  GET_LEADERBOARD: (roomId) => `${API_BASE_URL}/game/leaderboard/${roomId}`,
  
  // Room endpoints
  CREATE_ROOM: `${API_BASE_URL}/rooms/create`,
  GET_ROOM: (roomId) => `${API_BASE_URL}/rooms/${roomId}`,
  DELETE_ROOM: (roomId) => `${API_BASE_URL}/rooms/delete/${roomId}`,
  GET_ALL_ROOMS: `${API_BASE_URL}/rooms`,

  // WebSocket
  WS_BASE_URL: API_BASE_URL
};

// Log for debugging (remove in production)
console.log('API Base URL:', API_BASE_URL);

export default API_ENDPOINTS;