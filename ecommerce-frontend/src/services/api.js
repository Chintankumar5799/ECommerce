import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/api';

const api = axios.create({
  baseURL: API_BASE_URL,
});

// Interceptor for Auth
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

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
