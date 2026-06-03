import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || `http://${window.location.hostname}:8081/api`;

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor for Auth (Adding Token)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor for Auto-Refresh Token on 401
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    // If error is 401 Unauthorized and we haven't already retried this request
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) throw new Error("No refresh token");

        // Call the backend refresh endpoint
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, { refreshToken });
        
        // Save the new access token
        const newAccessToken = response.data.accessToken;
        localStorage.setItem('accessToken', newAccessToken);
        
        // Update the failed request's header and retry it
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return api(originalRequest);
      } catch (refreshError) {
        // If refresh fails (e.g., refresh token expired), log the user out
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('userId');
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
};

export const productService = {
  getCategories: () => api.get('/category/getCategory'),
  getProductsBySubCategoryId: (subId) => api.get(`/product/getProduct?subcategoryId=${subId}`),
  getProductVariants: (productId) => api.get(`/product/getProductVariant?productId=${productId}`),
  filterProducts: (params) => {
    // Axios takes care of URLSearchParams if we pass params option,
    // but the backend expects @RequestParam in a POST, so we'll append to URL.
    const query = new URLSearchParams(params).toString();
    return api.post(`/product/filter?${query}`);
  },
  buyProduct: (variantId, quantity) =>
    api.post(`/product/buyProduct?variantId=${variantId}&quantity=${quantity}`),
};

export const cartService = {
  addToCart: (userId, variantId, quantity) =>
    api.post(`/cart/addToCart?userId=${userId}&variantId=${variantId}&quantity=${quantity}`),
  addToWishlist: (userId, variantId, quantity) =>
    api.post(`/cart/wishlist?userId=${userId}&variantId=${variantId}&quantity=${quantity}`),
  changeStatus: (userId, variantId) =>
    api.put(`/cart/changeStatus?userId=${userId}&variantId=${variantId}`),
  getCart: (userId) => api.get(`/cart/getCart?userId=${userId}`),
  removeItem: (id) => api.delete(`/cart/remove?id=${id}`),
};

export const paymentService = {
  createPaymentIntent: (data) => api.post(`/payment/create-payment-intent`, data)
};

export const orderService = {
  confirmOrder: (userId, paymentIntentId) => 
    api.post(`/order/confirm?userId=${userId}&paymentIntentId=${paymentIntentId}`)
};

export const sellerService = {
  getDashboard: () => api.get(`/seller/dashboard`),
  registerSeller: (userData) => api.post(`/admin/sellerRegister`, userData)
};

export default api;
