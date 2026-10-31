import { environment } from '../../../environments/environment';

/** Una única base para servicios e interceptor; admite proxy web o una API HTTPS externa. */
export const API_BASE_URL = environment.apiUrl.replace(/\/+$/, '');
