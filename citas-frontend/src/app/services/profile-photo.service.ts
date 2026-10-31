import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { API_BASE_URL } from '../core/config/api.config';

@Injectable({ providedIn: 'root' })
export class ProfilePhotoService {
  private readonly http = inject(HttpClient);
  private readonly url = `${API_BASE_URL}/me/photo`;

  getPhoto() {
    return this.http.get(this.url, { responseType: 'blob' });
  }

  savePhoto(file: File) {
    const form = new FormData();
    form.append('foto', file);
    return this.http.put<void>(this.url, form);
  }

  removePhoto() {
    return this.http.delete<void>(this.url);
  }
}
