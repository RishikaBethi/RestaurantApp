import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://r66jl7kizi.execute-api.ap-southeast-2.amazonaws.com/dev', // your base URL
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;