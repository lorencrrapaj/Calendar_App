import api from './api';

export async function register(email: string, password: string) {
  const res = await api.post('/auth/register', { email, password });
  return res.data;
}

export async function login(email: string, password: string) {
  const res = await api.post('/auth/login', { email, password });
  localStorage.setItem('token', res.data.token);
  return res.data;
}

export async function logout() {
  await api.post('/auth/logout');
  localStorage.removeItem('token');
}

export async function changePassword(oldPwd: string, newPwd: string) {
  await api.put('/auth/password', { oldPassword: oldPwd, newPassword: newPwd });
}
