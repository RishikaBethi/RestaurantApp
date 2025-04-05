import api from "./api";
 
interface LoginPayload {
  email: string;
  password: string;
}
 
export const loginUser = async (data: LoginPayload) => {
  const response = await api.post("/auth/sign-in", data);
  return response.data;
};