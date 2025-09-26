import {GenericResponseDto} from 'components/dto/GenericResponseDto.ts';
import axios from 'axios';

export function email() {

  const api = axios.create({
    baseURL: '/',
    headers: { 'Content-Type': 'application/json', },
    maxRedirects: 0
  });

  const sendEmailResetPassword = async (email: string): Promise<GenericResponseDto> => {
    return (await api.get<GenericResponseDto>('/email/send-token', {
      params: {
        email: email
      },
      paramsSerializer: {
        indexes: null
      }
    })).data;
  }

  const setNewPassword = async (token: string,
                                password: string,
                                passwordRepeated: string):
    Promise<GenericResponseDto> => {

    return (await api.get<GenericResponseDto>(
      '/email/set-new-password', {
      params: {
        token: token,
        password: password,
        passwordRepeated: passwordRepeated
      },
      paramsSerializer: {
        indexes: null
      }
    })).data;
  }

  return { sendEmailResetPassword, setNewPassword };
}
