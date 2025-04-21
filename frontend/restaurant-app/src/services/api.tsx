import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://oxod3ip4sf.execute-api.ap-southeast-2.amazonaws.com/dev',
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;