import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://5b4szpnw27.execute-api.ap-southeast-2.amazonaws.com/dev',
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;