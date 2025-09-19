import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { HttpParams, HttpHeaders } from '@angular/common/http';

import { ApiService, ApiResponse, PaginatedResponse } from './api.service';
import { environment } from '../../../environments/environment';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  let originalLocalStorage: Storage;

  const baseUrl = environment.apiUrl || 'http://localhost:8080/api';
  const mockToken = 'test-jwt-token';

  beforeEach(() => {
    // Mock localStorage
    originalLocalStorage = window.localStorage;
    const mockLocalStorage = {
      getItem: jasmine.createSpy('getItem'),
      setItem: jasmine.createSpy('setItem'),
      removeItem: jasmine.createSpy('removeItem'),
      clear: jasmine.createSpy('clear'),
      length: 0,
      key: jasmine.createSpy('key')
    };

    Object.defineProperty(window, 'localStorage', {
      value: mockLocalStorage,
      writable: true
    });

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    Object.defineProperty(window, 'localStorage', {
      value: originalLocalStorage,
      writable: true
    });
  });

  describe('Service Initialization', () => {
    it('should be created', () => {
      expect(service).toBeTruthy();
    });

    it('should initialize with correct base URL', () => {
      expect((service as any).baseUrl).toBe(baseUrl);
    });

    it('should initialize loading state as false', () => {
      expect(service.isLoading()).toBe(false);
    });

    it('should provide loading observable', (done) => {
      service.loading$.subscribe(loading => {
        expect(loading).toBe(false);
        done();
      });
    });
  });

  describe('GET Requests', () => {
    const endpoint = 'users';
    const mockResponse = { id: 1, name: 'Test User' };

    it('should make GET request with correct URL', () => {
      service.get(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should include query parameters', () => {
      const params = { page: 0, size: 10, sort: 'name' };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.get('page') === '0' &&
               request.params.get('size') === '10' &&
               request.params.get('sort') === 'name';
      });
      expect(req.request.method).toBe('GET');
      req.flush(mockResponse);
    });

    it('should filter out null and undefined parameters', () => {
      const params = {
        page: 0,
        size: null,
        sort: undefined,
        filter: 'active'
      };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.get('page') === '0' &&
               request.params.get('filter') === 'active' &&
               !request.params.has('size') &&
               !request.params.has('sort');
      });
      req.flush(mockResponse);
    });

    it('should include Content-Type header', () => {
      service.get(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      req.flush(mockResponse);
    });

    it('should include Authorization header when token exists', () => {
      (window.localStorage.getItem as jasmine.Spy).and.returnValue(mockToken);

      service.get(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
      req.flush(mockResponse);
    });

    it('should not include Authorization header when no token', () => {
      (window.localStorage.getItem as jasmine.Spy).and.returnValue(null);

      service.get(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Authorization')).toBeNull();
      req.flush(mockResponse);
    });

    it('should set loading state during request', () => {
      let loadingStates: boolean[] = [];

      service.loading$.subscribe(loading => {
        loadingStates.push(loading);
      });

      service.get(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(loadingStates).toContain(true); // Should be true during request

      req.flush(mockResponse);
      expect(loadingStates).toContain(false); // Should be false after completion
    });

    it('should handle successful response', () => {
      service.get<typeof mockResponse>(endpoint).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(mockResponse);
    });

    it('should handle error response and reset loading state', () => {
      let errorOccurred = false;
      let finalLoadingState = true;

      service.get(endpoint).subscribe({
        error: () => {
          errorOccurred = true;
          finalLoadingState = service.isLoading();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush('Error', { status: 500, statusText: 'Internal Server Error' });

      expect(errorOccurred).toBe(true);
      expect(finalLoadingState).toBe(false);
    });
  });

  describe('POST Requests', () => {
    const endpoint = 'users';
    const postData = { name: 'New User', email: 'test@example.com' };
    const mockResponse = { id: 1, ...postData };

    it('should make POST request with correct data', () => {
      service.post(endpoint, postData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(postData);
      req.flush(mockResponse);
    });

    it('should include correct headers', () => {
      service.post(endpoint, postData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      req.flush(mockResponse);
    });

    it('should handle successful POST response', () => {
      service.post<typeof mockResponse>(endpoint, postData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(mockResponse);
    });

    it('should manage loading state for POST', () => {
      let loadingStates: boolean[] = [];

      service.loading$.subscribe(loading => {
        loadingStates.push(loading);
      });

      service.post(endpoint, postData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(service.isLoading()).toBe(true);

      req.flush(mockResponse);
      expect(service.isLoading()).toBe(false);
    });
  });

  describe('PUT Requests', () => {
    const endpoint = 'users/1';
    const putData = { id: 1, name: 'Updated User', email: 'updated@example.com' };
    const mockResponse = { ...putData };

    it('should make PUT request with correct data', () => {
      service.put(endpoint, putData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(putData);
      req.flush(mockResponse);
    });

    it('should include correct headers for PUT', () => {
      service.put(endpoint, putData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      req.flush(mockResponse);
    });

    it('should handle successful PUT response', () => {
      service.put<typeof mockResponse>(endpoint, putData).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(mockResponse);
    });
  });

  describe('DELETE Requests', () => {
    const endpoint = 'users/1';
    const mockResponse = { success: true, message: 'User deleted' };

    it('should make DELETE request', () => {
      service.delete(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(mockResponse);
    });

    it('should include correct headers for DELETE', () => {
      service.delete(endpoint).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Content-Type')).toBe('application/json');
      req.flush(mockResponse);
    });

    it('should handle successful DELETE response', () => {
      service.delete<typeof mockResponse>(endpoint).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(mockResponse);
    });
  });

  describe('File Upload', () => {
    const endpoint = 'upload';
    const mockFile = new File(['test content'], 'test.txt', { type: 'text/plain' });
    const additionalData = { description: 'Test upload' };
    const mockResponse = { id: 1, filename: 'test.txt', uploaded: true };

    it('should upload file with FormData', () => {
      service.upload(endpoint, mockFile).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body instanceof FormData).toBe(true);

      // Check if file is in FormData
      const formData = req.request.body as FormData;
      expect(formData.get('file')).toBe(mockFile);

      req.flush(mockResponse);
    });

    it('should include additional data in FormData', () => {
      service.upload(endpoint, mockFile, additionalData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      const formData = req.request.body as FormData;

      expect(formData.get('file')).toBe(mockFile);
      expect(formData.get('description')).toBe('Test upload');

      req.flush(mockResponse);
    });

    it('should not include Content-Type header for uploads', () => {
      service.upload(endpoint, mockFile).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      // Content-Type should not be set for FormData uploads
      expect(req.request.headers.get('Content-Type')).toBeNull();
      req.flush(mockResponse);
    });

    it('should include Authorization header for uploads when token exists', () => {
      (window.localStorage.getItem as jasmine.Spy).and.returnValue(mockToken);

      service.upload(endpoint, mockFile).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
      req.flush(mockResponse);
    });

    it('should handle successful upload response', () => {
      service.upload<typeof mockResponse>(endpoint, mockFile).subscribe(response => {
        expect(response).toEqual(mockResponse);
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(mockResponse);
    });

    it('should manage loading state for uploads', () => {
      service.upload(endpoint, mockFile).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(service.isLoading()).toBe(true);

      req.flush(mockResponse);
      expect(service.isLoading()).toBe(false);
    });
  });

  describe('Parameter Building', () => {
    const endpoint = 'test';

    it('should handle empty parameters', () => {
      service.get(endpoint, {}).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      expect(req.request.params.keys().length).toBe(0);
      req.flush({});
    });

    it('should handle string parameters', () => {
      const params = { name: 'test', status: 'active' };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.get('name') === 'test' &&
               request.params.get('status') === 'active';
      });
      req.flush({});
    });

    it('should handle number parameters', () => {
      const params = { page: 1, size: 10, id: 123 };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.get('page') === '1' &&
               request.params.get('size') === '10' &&
               request.params.get('id') === '123';
      });
      req.flush({});
    });

    it('should handle boolean parameters', () => {
      const params = { active: true, deleted: false };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.get('active') === 'true' &&
               request.params.get('deleted') === 'false';
      });
      req.flush({});
    });

    it('should handle complex object parameters', () => {
      const params = {
        filter: { type: 'user', role: 'admin' },
        options: [1, 2, 3]
      };

      service.get(endpoint, params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/${endpoint}` &&
               request.params.has('filter') &&
               request.params.has('options');
      });
      req.flush({});
    });
  });

  describe('Authentication Token Management', () => {
    const endpoint = 'protected';

    it('should call localStorage.getItem for auth token', () => {
      service.get(endpoint).subscribe();

      expect(window.localStorage.getItem).toHaveBeenCalledWith('auth_token');

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush({});
    });

    it('should handle multiple requests with cached token', () => {
      (window.localStorage.getItem as jasmine.Spy).and.returnValue(mockToken);

      service.get(endpoint).subscribe();
      service.post(endpoint, {}).subscribe();

      expect(window.localStorage.getItem).toHaveBeenCalledTimes(2);

      const getReq = httpMock.expectOne(req => req.method === 'GET');
      const postReq = httpMock.expectOne(req => req.method === 'POST');

      expect(getReq.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
      expect(postReq.request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);

      getReq.flush({});
      postReq.flush({});
    });

    it('should handle token changes between requests', () => {
      const newToken = 'new-token';

      (window.localStorage.getItem as jasmine.Spy).and.returnValue(mockToken);
      service.get(endpoint).subscribe();

      (window.localStorage.getItem as jasmine.Spy).and.returnValue(newToken);
      service.get(endpoint).subscribe();

      const requests = httpMock.match(`${baseUrl}/${endpoint}`);
      expect(requests.length).toBe(2);

      expect(requests[0].request.headers.get('Authorization')).toBe(`Bearer ${mockToken}`);
      expect(requests[1].request.headers.get('Authorization')).toBe(`Bearer ${newToken}`);

      requests.forEach(req => req.flush({}));
    });
  });

  describe('Loading State Management', () => {
    const endpoint = 'test';

    it('should emit loading states in correct order', () => {
      const loadingStates: boolean[] = [];

      service.loading$.subscribe(loading => {
        loadingStates.push(loading);
      });

      service.get(endpoint).subscribe();

      // Should start with false (initial), then true (during request)
      expect(loadingStates[0]).toBe(false);
      expect(loadingStates[1]).toBe(true);

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush({});

      // Should end with false (after completion)
      expect(loadingStates[2]).toBe(false);
    });

    it('should handle concurrent requests correctly', () => {
      service.get('endpoint1').subscribe();
      service.get('endpoint2').subscribe();

      expect(service.isLoading()).toBe(true);

      const req1 = httpMock.expectOne(`${baseUrl}/endpoint1`);
      req1.flush({});

      // Should still be loading because of second request
      expect(service.isLoading()).toBe(true);

      const req2 = httpMock.expectOne(`${baseUrl}/endpoint2`);
      req2.flush({});

      // Should be false after both complete
      expect(service.isLoading()).toBe(false);
    });

    it('should reset loading state on error', () => {
      service.get(endpoint).subscribe({
        error: () => {} // Ignore error for this test
      });

      expect(service.isLoading()).toBe(true);

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush('Error', { status: 500, statusText: 'Server Error' });

      expect(service.isLoading()).toBe(false);
    });

    it('should provide isLoading method', () => {
      expect(service.isLoading()).toBe(false);

      service.get(endpoint).subscribe();
      expect(service.isLoading()).toBe(true);

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush({});
      expect(service.isLoading()).toBe(false);
    });
  });

  describe('Error Handling', () => {
    const endpoint = 'error-test';

    it('should propagate HTTP errors', () => {
      let errorCaught = false;
      const errorResponse = { message: 'Not found' };

      service.get(endpoint).subscribe({
        error: (error) => {
          errorCaught = true;
          expect(error.status).toBe(404);
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.flush(errorResponse, { status: 404, statusText: 'Not Found' });

      expect(errorCaught).toBe(true);
    });

    it('should reset loading state on all error types', () => {
      const errorScenarios = [
        { status: 400, statusText: 'Bad Request' },
        { status: 401, statusText: 'Unauthorized' },
        { status: 403, statusText: 'Forbidden' },
        { status: 404, statusText: 'Not Found' },
        { status: 500, statusText: 'Internal Server Error' }
      ];

      errorScenarios.forEach((errorScenario, index) => {
        const testEndpoint = `${endpoint}${index}`;

        service.get(testEndpoint).subscribe({
          error: () => {} // Ignore error
        });

        expect(service.isLoading()).toBe(true);

        const req = httpMock.expectOne(`${baseUrl}/${testEndpoint}`);
        req.flush('Error', errorScenario);

        expect(service.isLoading()).toBe(false);
      });
    });

    it('should handle network errors', () => {
      let networkErrorCaught = false;

      service.get(endpoint).subscribe({
        error: (error) => {
          networkErrorCaught = true;
          expect(error.error).toBeInstanceOf(ProgressEvent);
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/${endpoint}`);
      req.error(new ProgressEvent('Network error'));

      expect(networkErrorCaught).toBe(true);
      expect(service.isLoading()).toBe(false);
    });
  });

  describe('Type Safety and Generics', () => {
    interface User {
      id: number;
      name: string;
      email: string;
    }

    interface ApiUser extends ApiResponse<User> {}

    it('should maintain type safety for responses', () => {
      const mockUser: User = { id: 1, name: 'Test', email: 'test@example.com' };

      service.get<User>('users/1').subscribe(user => {
        expect(user.id).toBe(1);
        expect(user.name).toBe('Test');
        expect(user.email).toBe('test@example.com');
      });

      const req = httpMock.expectOne(`${baseUrl}/users/1`);
      req.flush(mockUser);
    });

    it('should handle API response wrapper types', () => {
      const mockApiResponse: ApiUser = {
        data: { id: 1, name: 'Test', email: 'test@example.com' },
        message: 'Success',
        success: true
      };

      service.get<ApiUser>('users/1').subscribe(response => {
        expect(response.success).toBe(true);
        expect(response.data.id).toBe(1);
        expect(response.message).toBe('Success');
      });

      const req = httpMock.expectOne(`${baseUrl}/users/1`);
      req.flush(mockApiResponse);
    });

    it('should handle paginated responses', () => {
      const mockPaginatedResponse: PaginatedResponse<User> = {
        content: [
          { id: 1, name: 'User 1', email: 'user1@example.com' },
          { id: 2, name: 'User 2', email: 'user2@example.com' }
        ],
        totalElements: 2,
        totalPages: 1,
        size: 10,
        number: 0
      };

      service.get<PaginatedResponse<User>>('users').subscribe(response => {
        expect(response.content.length).toBe(2);
        expect(response.totalElements).toBe(2);
        expect(response.content[0].name).toBe('User 1');
      });

      const req = httpMock.expectOne(`${baseUrl}/users`);
      req.flush(mockPaginatedResponse);
    });
  });

  describe('Edge Cases and Boundary Conditions', () => {
    it('should handle empty endpoint string', () => {
      service.get('').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/`);
      req.flush({});
    });

    it('should handle endpoint with leading slash', () => {
      service.get('/users').subscribe();

      const req = httpMock.expectOne(`${baseUrl}//users`);
      req.flush({});
    });

    it('should handle very large payload', () => {
      const largeData = {
        items: new Array(10000).fill(0).map((_, i) => ({ id: i, value: `item-${i}` }))
      };

      service.post('large-data', largeData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/large-data`);
      expect(req.request.body).toEqual(largeData);
      req.flush({ success: true });
    });

    it('should handle special characters in parameters', () => {
      const params = {
        query: 'test & special chars: @#$%',
        encoded: 'test+encoded%20value'
      };

      service.get('search', params).subscribe();

      const req = httpMock.expectOne(request => {
        return request.url === `${baseUrl}/search` &&
               request.params.has('query') &&
               request.params.has('encoded');
      });
      req.flush({});
    });

    it('should handle null response body', () => {
      service.delete('users/1').subscribe(response => {
        expect(response).toBeNull();
      });

      const req = httpMock.expectOne(`${baseUrl}/users/1`);
      req.flush(null);
    });

    it('should handle empty array responses', () => {
      service.get<any[]>('empty-list').subscribe(response => {
        expect(response).toEqual([]);
        expect(Array.isArray(response)).toBe(true);
      });

      const req = httpMock.expectOne(`${baseUrl}/empty-list`);
      req.flush([]);
    });
  });
});