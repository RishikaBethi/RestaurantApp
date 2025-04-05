import api from "@/services/api";
 
export const registerUser = async (userData: {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  confirmPassword: string
}) => {
  const response = await api.post("/auth/sign-up", userData);
  return response.data;
};