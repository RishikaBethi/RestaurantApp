import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://ebgfmz7npj.execute-api.ap-southeast-2.amazonaws.com/dev',
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;