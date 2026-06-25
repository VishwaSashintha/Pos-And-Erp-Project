export async function apiFetch(url: string, options: RequestInit = {}): Promise<any> {
  const token = localStorage.getItem('token');
  
  const headers = new Headers(options.headers || {});
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    localStorage.removeItem('token');
    window.location.href = '/login';
    throw new Error('Unauthorized. Logging out...');
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.message || `Request failed with status ${response.status}`);
  }

  
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    const res = await response.json();
    return res && typeof res === 'object' && 'success' in res && 'data' in res ? res.data : res;
  }
  return null;
}
