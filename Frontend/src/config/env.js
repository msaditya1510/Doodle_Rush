export const config = {
  API_URL: import.meta.env.VITE_API_URL || "http://localhost:8080",
  IS_PRODUCTION: import.meta.env.MODE === 'production'
};