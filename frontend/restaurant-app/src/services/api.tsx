import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://w3lzda6lxi.execute-api.ap-southeast-2.amazonaws.com/dev', // your base URL
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;