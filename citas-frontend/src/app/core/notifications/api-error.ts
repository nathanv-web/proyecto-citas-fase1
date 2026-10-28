import { HttpErrorResponse } from '@angular/common/http';

/** Los textos de errores HTTP proceden del contrato de la API, nunca del código de estado. */
export function apiErrorMessage(error: HttpErrorResponse): string {
  if (error.status === 0) return 'No se recibió respuesta de la API. Revisa la conexión.';
  const body = error.error;
  const messages: string[] = [];
  if (typeof body?.message === 'string') messages.push(body.message);
  if (body?.errors && typeof body.errors === 'object') {
    for (const value of Object.values(body.errors)) {
      if (typeof value === 'string') messages.push(value);
    }
  }
  return [...new Set(messages)].join('\n') || 'La API no devolvió un mensaje de error.';
}
