import axios from 'axios';
 
const api = axios.create({
  baseURL: 'https://restaurantapi-run8team7-sb-dev.development.krci-dev.cloudmentor.academy',
  headers: {
    'Content-Type': 'application/json',
  },
});
 
export default api;