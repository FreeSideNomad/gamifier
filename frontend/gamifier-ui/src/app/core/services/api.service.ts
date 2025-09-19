import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpHeaders } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

export interface ApiResponse<T> {
  data: T;
  message?: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private baseUrl = environment.apiUrl || 'http://localhost:8080/api';
  private loadingSubject = new BehaviorSubject<boolean>(false);

  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Generic GET request
   */
  get<T>(endpoint: string, params?: any): Observable<T> {
    this.setLoading(true);
    const httpParams = this.buildParams(params);

    return this.http.get<T>(`${this.baseUrl}/${endpoint}`, {
      params: httpParams,
      headers: this.getHeaders()
    }).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        throw error;
      })
    );
  }

  /**
   * Generic POST request
   */
  post<T>(endpoint: string, data: any): Observable<T> {
    this.setLoading(true);

    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, data, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        throw error;
      })
    );
  }

  /**
   * Generic PUT request
   */
  put<T>(endpoint: string, data: any): Observable<T> {
    this.setLoading(true);

    return this.http.put<T>(`${this.baseUrl}/${endpoint}`, data, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        throw error;
      })
    );
  }

  /**
   * Generic DELETE request
   */
  delete<T>(endpoint: string): Observable<T> {
    this.setLoading(true);

    return this.http.delete<T>(`${this.baseUrl}/${endpoint}`, {
      headers: this.getHeaders()
    }).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        throw error;
      })
    );
  }

  /**
   * File upload request
   */
  upload<T>(endpoint: string, file: File, additionalData?: any): Observable<T> {
    this.setLoading(true);

    const formData = new FormData();
    formData.append('file', file);

    if (additionalData) {
      Object.keys(additionalData).forEach(key => {
        formData.append(key, additionalData[key]);
      });
    }

    return this.http.post<T>(`${this.baseUrl}/${endpoint}`, formData, {
      headers: this.getUploadHeaders()
    }).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        throw error;
      })
    );
  }

  /**
   * Build HTTP params from object
   */
  private buildParams(params?: any): HttpParams {
    let httpParams = new HttpParams();

    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key] !== null && params[key] !== undefined) {
          httpParams = httpParams.set(key, params[key].toString());
        }
      });
    }

    return httpParams;
  }

  /**
   * Get standard headers
   */
  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });

    // Add authentication token if available
    const token = this.getAuthToken();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  /**
   * Get headers for file upload
   */
  private getUploadHeaders(): HttpHeaders {
    let headers = new HttpHeaders();

    // Add authentication token if available
    const token = this.getAuthToken();
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }

    return headers;
  }

  /**
   * Get authentication token from storage
   */
  private getAuthToken(): string | null {
    return localStorage.getItem('auth_token');
  }

  /**
   * Set loading state
   */
  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  /**
   * Check if currently loading
   */
  isLoading(): boolean {
    return this.loadingSubject.value;
  }

  /**
   * Get current user (placeholder method for tests)
   */
  getCurrentUser(): Observable<any> {
    return this.get('users/me');
  }

  /**
   * Get user statistics (placeholder method for tests)
   */
  getUserStats(): Observable<any> {
    return this.get('dashboard/stats');
  }

  /**
   * Get recent activity (placeholder method for tests)
   */
  getRecentActivity(): Observable<any> {
    return this.get('activity/recent');
  }

  /**
   * Get active missions (placeholder method for tests)
   */
  getActiveMissions(): Observable<any> {
    return this.get('missions/active');
  }

  /**
   * Get leaderboard preview (placeholder method for tests)
   */
  getLeaderboardPreview(): Observable<any> {
    return this.get('leaderboards/preview');
  }
}