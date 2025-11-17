import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Voorziening } from '../models/voorziening.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class VoorzieningService {
  private apiUrl = `${environment.apiUrl}/voorzieningen`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Voorziening[]> {
    return this.http.get<Voorziening[]>(this.apiUrl);
  }

  getById(id: string): Observable<Voorziening> {
    return this.http.get<Voorziening>(`${this.apiUrl}/${id}`);
  }

  create(voorziening: Voorziening): Observable<Voorziening> {
    return this.http.post<Voorziening>(this.apiUrl, voorziening);
  }

  update(id: string, voorziening: Voorziening): Observable<Voorziening> {
    return this.http.put<Voorziening>(`${this.apiUrl}/${id}`, voorziening);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
