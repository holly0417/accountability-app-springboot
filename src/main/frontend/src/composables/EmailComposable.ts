import {GenericResponseDto} from 'components/dto/GenericResponseDto.ts';
import axios from 'axios';
import {ResetPasswordDto} from 'components/dto/ResetPasswordDto.ts';
import {ref} from 'vue';
import {SuccessfulPasswordChangeResponse} from 'components/dto/SuccessfulPasswordChangeResponse.ts';
import {ErrorResponse} from 'components/dto/ErrorResponse.ts';

export function email() {

  const api = axios.create({
    baseURL: '/', // Base URL for all requests
    headers: { 'Content-Type': 'application/json', }, // Send data as JSON
    maxRedirects: 0 // Don't follow redirects automatically
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

  const error = ref<ErrorResponse>();

  const setNewPassword = async (resetPasswordDto: ResetPasswordDto) => {

    try {
        const response = await api.post<SuccessfulPasswordChangeResponse>('/email/set-new-password', resetPasswordDto);
        return response.data.message; // Return the success message

    } catch (err) {

      if(axios.isAxiosError(err) && err.response){
        const errorData = err.response.data as ErrorResponse;

        // Create enhanced error with backend information
        const enhancedError: ErrorResponse = {
          message: errorData.message,
          errors: errorData.errors,
          errorCode: errorData.errorCode
        };

        error.value = enhancedError;
        throw enhancedError;
      } else {
        // Network or other errors
        const networkError: ErrorResponse = {
          message: 'Network error. Please check your connection.'
        };
        error.value = networkError;
        throw networkError;
      }
    }

  }

  return { sendEmailResetPassword, setNewPassword, error };
}
