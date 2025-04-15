import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://hq36jqkij1.execute-api.ap-southeast-2.amazonaws.com/dev',
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;