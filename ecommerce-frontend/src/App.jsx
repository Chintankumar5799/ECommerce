import React, { useState, useEffect } from 'react';
import {
  ShoppingCart, Heart, Search, User, Menu, X, Filter,
  ChevronRight, ArrowRight, Star, LogIn, LogOut
} from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import { authService, productService, cartService } from './services/api';
import { CheckoutModal } from './components/Checkout';
import { SellerDashboard } from './components/SellerDashboard';
import './App.css';

const App = () => {
  const [categories, setCategories] = useState([]);
  const [products, setProducts] = useState([]);
  const [variants, setVariants] = useState([]);
  const [selectedProductId, setSelectedProductId] = useState(null);
  const [cartItems, setCartItems] = useState([]);
  const [wishlistItems, setWishlistItems] = useState([]);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [isWishlistOpen, setIsWishlistOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  // Filters & Pagination
  const [selectedCat, setSelectedCat] = useState(null);
  const [selectedSub, setSelectedSub] = useState(null);
  const [minPrice, setMinPrice] = useState(0);
  const [maxPrice, setMaxPrice] = useState(100000);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [sortBy, setSortBy] = useState('productName');
  const [direction, setDirection] = useState('asc');

  // UI States
  const [isLoginModalOpen, setIsLoginModalOpen] = useState(false);
  const [isRegistering, setIsRegistering] = useState(false);
  const [authForm, setAuthForm] = useState({ email: '', password: '', mobileNumber: '', passwordHash: '' });
  const [message, setMessage] = useState('');

  // Checkout States
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [checkoutAmount, setCheckoutAmount] = useState(0);

  // Dashboard Toggle State
  const [currentView, setCurrentView] = useState('shop');
  const [orderHistory, setOrderHistory] = useState([]);

  useEffect(() => {
    fetchCategories();
    // Try to load user from local storage
    const savedToken = localStorage.getItem('accessToken');
    const savedUserId = localStorage.getItem('userId');
    if (savedToken && savedUserId) {
      setUser({ id: savedUserId, token: savedToken });
      fetchCart(savedUserId);
      fetchOrderHistory(savedUserId);
    }
  }, []);

  const fetchOrderHistory = async (userId) => {
    try {
      const res = await orderService.orderHistory(userId);
      setOrderHistory(res.data);
    } catch (err) {
      console.error("Order history fetch failed", err);
    }
  };

  useEffect(() => {
    fetchProducts();
  }, [selectedCat, selectedSub, minPrice, maxPrice, page, sortBy, direction]);

  const fetchCategories = async () => {
    try {
      const res = await productService.getCategories();
      setCategories(res.data);
    } catch (err) {
      console.error("Categories fetch failed", err);
    }
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      if (selectedSub) {
        // Direct product fetch by subcategory (aligning with user request)
        const res = await productService.getProductsBySubCategoryId(selectedSub);
        setProducts(res.data);
      } else {
        // Filtered products (initial/all view)
        const res = await productService.filterProducts({
          categoryId: selectedCat || '',
          subCategoryId: selectedSub || '',
          minPrice,
          maxPrice,
          page,
          size: 8,
          sortBy,
          direction
        });
        setProducts(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err) {
      console.error("Products fetch failed", err);
    } finally {
      setLoading(false);
    }
  };

  const handleProductClick = async (productId) => {
    try {
      if (selectedProductId === productId) {
        setSelectedProductId(null);
        setVariants([]);
        return;
      }
      const res = await productService.getProductVariants(productId);
      setVariants(res.data);
      setSelectedProductId(productId);
    } catch (err) {
      console.error("Variants fetch failed", err);
      setMessage("Failed to load variants.");
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await authService.login(authForm);
      const { accessToken, refreshToken, userId } = res.data;
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('refreshToken', refreshToken);
      localStorage.setItem('userId', userId);
      setUser({ id: userId, token: accessToken });
      setIsLoginModalOpen(false);
      setMessage("Logged in successfully!");

      // Fetch data now that we are authorized
      fetchCategories();
      fetchProducts();
      fetchCart(userId);
      fetchOrderHistory(userId);
    } catch (err) {
      setMessage("Login failed. Check your credentials.");
    }
  };

  const fetchCart = async (userId) => {
    try {
      if (!userId) return;
      const res = await cartService.getCart(userId);
      const allItems = res.data || [];
      setCartItems(allItems.filter(item => item.status?.toUpperCase() === 'CART'));
      setWishlistItems(allItems.filter(item => item.status?.toUpperCase() === 'WISHLIST'));
    } catch (err) {
      console.warn("Cart fetch returned error (expected if cart is empty)", err);
      // If fetching fails (e.g. 404 cart empty), clear the lists
      setCartItems([]);
      setWishlistItems([]);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      // Backend RegisterRequest expects { email, mobileNumber, passwordHash, ... }
      await authService.register({
        email: authForm.email,
        mobileNumber: authForm.mobileNumber,
        passwordHash: authForm.password, // Frontend uses 'password', backend expects 'passwordHash'
        address: [] // Could add address form later
      });
      setIsRegistering(false);
      setMessage("Registration successful! Please login.");
    } catch (err) {
      setMessage("Registration failed.");
    }
  };

  const handleLogout = () => {
    localStorage.clear();
    setUser(null);
    setOrderHistory([]);
    setMessage("Logged out.");
  };

  const onAddToCart = async (variant) => {
    if (!user) {
      setIsLoginModalOpen(true);
      return;
    }
    try {
      await cartService.addToCart(user.id, variant.variantId, 1);
      setMessage(`Added to cart!`);
      fetchCart(user.id);
    } catch (err) {
      setMessage("Failed to add to cart.");
    }
  };

  const onWishlistToggle = async (variant) => {
    if (!user) {
      setIsLoginModalOpen(true);
      return;
    }
    try {
      // Check if it's already in the wishlist
      const existing = wishlistItems.find(item => item.variantId === variant.variantId);
      if (existing) {
        // Correctly toggle by removing if it already exists
        await cartService.removeItem(existing.id);
        setMessage("Removed from wishlist.");
      } else {
        await cartService.addToWishlist(user.id, variant.variantId, 1);
        setMessage(`Added to wishlist!`);
      }
      fetchCart(user.id);
    } catch (err) {
      setMessage("Failed to update wishlist.");
    }
  };

  const onRemoveItem = async (id) => {
    try {
      await cartService.removeItem(id);
      setMessage("Item removed.");
      if (user) fetchCart(user.id);
    } catch (err) {
      setMessage("Failed to remove item.");
    }
  };

  return (
    <div className="app-root">
      {/* Header */}
      <header className="header glass">
        <div className="logo" onClick={() => { setSelectedCat(null); setSelectedSub(null); setCurrentView('shop'); }}>
          <span>NEO</span>ECOMMERCE
        </div>

        <div className="nav-controls">
          <div className="icon-btn" onClick={() => setMessage("Search feature coming soon!")}>
            <Search size={22} />
          </div>
          <div className="icon-btn" style={{ position: 'relative' }} title="Wishlist" onClick={() => setIsWishlistOpen(true)}>
            <Heart size={22} />
            {wishlistItems.length > 0 && <span className="nav-badge">{wishlistItems.length}</span>}
          </div>
          <div className="icon-btn" style={{ position: 'relative' }} title="Cart" onClick={() => setIsCartOpen(true)}>
            <ShoppingCart size={22} />
            {cartItems.length > 0 && <span className="nav-badge">{cartItems.length}</span>}
          </div>
          {user ? (
            <>
              <div className="icon-btn" onClick={() => setCurrentView('history')} title="Order History">
                 <ArrowRight size={22} style={{ transform: 'rotate(-45deg)' }} />
              </div>
              <div className="icon-btn" onClick={() => setCurrentView(currentView === 'shop' ? 'dashboard' : 'shop')} title="Toggle Dashboard">
                <Menu size={22} />
              </div>
              <div className="icon-btn" onClick={handleLogout} title="Logout">
                <LogOut size={22} />
              </div>
            </>
          ) : (
            <div className="icon-btn" onClick={() => setIsLoginModalOpen(true)}>
              <LogIn size={22} />
            </div>
          )}
        </div>
      </header>

      {/* Main Layout or Dashboard */}
      {currentView === 'shop' ? (
        <div className="main-wrapper">
          {/* Sidebar */}
          <aside className="sidebar">
            <h3>Collections</h3>
            {categories.map(cat => (
              <div key={cat.id} className="category-group">
                <div
                  className={`cat-parent ${selectedCat === cat.id ? 'active' : ''}`}
                  onClick={() => { setSelectedCat(cat.id); setSelectedSub(null); setPage(0); }}
                >
                  <ChevronRight size={16} /> {cat.categoryName}
                </div>
                <div className="sub-list">
                  {cat.subCategoryResponse?.map(sub => (
                    <span
                      key={sub.id}
                      className={`sub-link ${selectedSub === sub.id ? 'active' : ''}`}
                      onClick={() => { setSelectedSub(sub.id); setSelectedCat(cat.id); setPage(0); }}
                    >
                      {sub.subCategoryName}
                    </span>
                  ))}
                </div>
              </div>
            ))}

            <h3 style={{ marginTop: '2rem' }}>Filters</h3>
            <div className="price-filters-sidebar">
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Price Range</p>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                <input
                  type="number"
                  placeholder="Min"
                  className="range-input"
                  style={{ width: '100%', padding: '0.8rem' }}
                  value={minPrice}
                  onChange={e => setMinPrice(e.target.value)}
                />
                <input
                  type="number"
                  placeholder="Max"
                  className="range-input"
                  style={{ width: '100%', padding: '0.8rem' }}
                  value={maxPrice}
                  onChange={e => setMaxPrice(e.target.value)}
                />
              </div>
            </div>
          </aside>

          {/* Content */}
          <main className="content">
            <AnimatePresence>
              {message && (
                <motion.div
                  initial={{ opacity: 0, y: -20 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  className="toast glass"
                  onAnimationComplete={() => setTimeout(() => setMessage(''), 3000)}
                  style={{ position: 'fixed', top: '110px', right: '4rem', padding: '1rem 2rem', background: 'var(--accent)', borderRadius: '15px', zIndex: 3000 }}
                >
                  {message}
                </motion.div>
              )}
            </AnimatePresence>

            <div className="filter-bar glass">
              <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <Filter size={18} />
                <span style={{ fontWeight: 600, fontSize: '1rem' }}>
                  {selectedSub ? categories.find(c => c.id === selectedCat)?.subCategoryResponse.find(s => s.id === selectedSub)?.subCategoryName : "All Collections"}
                </span>
              </div>
              <div className="sort-controls">
                <select onChange={e => setSortBy(e.target.value)} className="range-input" style={{ width: 'auto', border: 'none', background: 'transparent' }}>
                  <option value="productName">Sort by: Name</option>
                  <option value="price">Sort by: Price</option>
                </select>
              </div>
            </div>

            {loading ? (
              <div className="loading-state" style={{ textAlign: 'center', padding: '100px' }}>
                <div className="spinner" style={{ width: '40px', height: '40px', borderTopColor: 'var(--accent)' }}></div>
                <p style={{ marginTop: '20px', color: 'var(--text-muted)' }}>Curating products...</p>
              </div>
            ) : selectedProductId ? (
              /* Product Detail View */
              <motion.div
                key="detail"
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                className="product-detail-view"
              >
                <button className="icon-btn-text" onClick={() => setSelectedProductId(null)} style={{ marginBottom: '2rem', color: 'var(--accent)', fontWeight: 600 }}>
                  <ArrowRight size={18} style={{ transform: 'rotate(180deg)' }} /> Back to Collection
                </button>

                <div className="detail-layout">
                  <div className="detail-img glass" style={{ boxShadow: 'var(--shadow-lg)' }}>
                    {products.find(p => p.id === selectedProductId)?.image ? (
                      <img src={`data:image/png;base64,${products.find(p => p.id === selectedProductId).image}`} alt="Product" />
                    ) : (
                      <img src="https://via.placeholder.com/600x600?text=Product" alt="Placeholder" />
                    )}
                  </div>

                  <div className="detail-info">
                    <h2 className="detail-title">{products.find(p => p.id === selectedProductId)?.productName}</h2>
                    <div style={{ display: 'flex', gap: '5px', color: '#ffcc00', marginBottom: '1.5rem' }}>
                        <Star size={16} fill="#ffcc00" /><Star size={16} fill="#ffcc00" /><Star size={16} fill="#ffcc00" /><Star size={16} fill="#ffcc00" /><Star size={16} />
                        <span style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginLeft: '10px' }}>(120 Reviews)</span>
                    </div>
                    <p className="detail-desc">Experience luxury redefined. Our latest collection blends timeless style with contemporary craftsmanship for the modern lifestyle.</p>

                    <div className="variants-section">
                      <h4>Personalize Your Order</h4>
                      <div className="variants-list">
                        {variants.length > 0 ? variants.map(v => (
                          <div key={v.variantId} className="variant-item detail-v-item glass">
                            <div className="v-details-box">
                              <div className="v-attributes-row">
                                {v.variantAttributes && Object.entries(v.variantAttributes).map(([key, val]) => {
                                  const displayVal = typeof val === 'object' && val !== null ? (val.textValue || val.value || val.asText || Object.values(val)[0]?.textValue || '...') : String(val);
                                  return (
                                    <div key={key} className="v-option-chip glass">
                                      <span className="v-option-key">{key}:</span>
                                      <span className="v-option-val">{displayVal}</span>
                                    </div>
                                  );
                                })}
                              </div>
                              <div className="v-pricing-row">
                                <span className="v-price-now">${v.offerPrice || v.price}</span>
                                {v.discount > 0 && <span className="v-discount-badge">-{v.discount}%</span>}
                              </div>
                            </div>
                            <div className="v-actions">
                              <button className="icon-btn" onClick={() => onWishlistToggle(v)}><Heart size={18} /></button>
                              <button className="btn-solid" style={{ width: 'auto', padding: '0 2rem', height: '50px', display: 'flex', alignItems: 'center', gap: '10px' }} onClick={() => onAddToCart(v)}>
                                <ShoppingCart size={18} /> Add to Cart
                              </button>
                            </div>
                          </div>
                        )) : (
                          <div className="shimmer-variants">Unfolding options...</div>
                        )}
                      </div>
                    </div>
                  </div>
                </div>
              </motion.div>
            ) : (
              /* Standard Grid View */
              <div className="product-grid">
                {products.length === 0 ? (
                  <div style={{ gridColumn: '1/-1', textAlign: 'center', padding: '10rem' }}>
                    <p style={{ color: 'var(--text-muted)', fontSize: '1.2rem' }}>Pure silence. No products match your criteria.</p>
                  </div>
                ) : products.map((product, idx) => (
                  <motion.div
                    key={product.id || idx}
                    className="product-card"
                    initial={{ opacity: 0, scale: 0.95 }}
                    whileInView={{ opacity: 1, scale: 1 }}
                    viewport={{ once: true }}
                    onClick={() => handleProductClick(product.id)}
                  >
                    <div className="img-box">
                      {product.image ? (
                        <img src={`data:image/png;base64,${product.image}`} alt={product.productName} />
                      ) : (
                        <div className="placeholder-img" style={{ height: '300px' }}>
                          <X size={40} />
                        </div>
                      )}
                      <div className="product-overlay">
                        <button className="btn-solid" style={{ width: 'auto', padding: '0.8rem 2rem' }}>
                          Explore
                        </button>
                      </div>
                    </div>
                    <div className="product-info">
                      <h3 className="product-title">{product.productName}</h3>
                      <div className="price-box">
                        <span className="curr-price">View Options</span>
                        <ChevronRight size={20} color="var(--text-muted)" />
                      </div>
                    </div>
                  </motion.div>
                ))}
              </div>
            )}

            {/* Pagination */}
            {totalPages > 1 && !selectedProductId && (
                <div className="pagination">
                {Array.from({ length: totalPages }).map((_, i) => (
                    <button
                    key={i}
                    className={`page-btn ${page === i ? 'active' : ''}`}
                    onClick={() => { setPage(i); window.scrollTo({ top: 0, behavior: 'smooth' }); }}
                    >
                    {i + 1}
                    </button>
                ))}
                </div>
            )}
          </main>
        </div>
      ) : currentView === 'dashboard' ? (
        <SellerDashboard />
      ) : (
          /* Order History View */
          <main className="content" style={{ marginLeft: 0, maxWidth: '1200px', margin: '140px auto' }}>
              <div className="order-history-container">
                  <div className="order-history-header">
                      <h2>Order History</h2>
                      <button className="icon-btn-text" onClick={() => setCurrentView('shop')}>
                           <ArrowRight size={18} style={{ transform: 'rotate(180deg)' }} /> Back to Shop
                      </button>
                  </div>

                  <div className="order-list">
                      {orderHistory.length === 0 ? (
                          <div className="empty-state" style={{ height: '400px' }}>
                              <p>You haven't placed any orders yet.</p>
                              <button className="btn-solid" style={{ width: 'auto', marginTop: '20px' }} onClick={() => setCurrentView('shop')}>Start Shopping</button>
                          </div>
                      ) : (
                          orderHistory.map(order => (
                              <div key={order.transactionId} className="order-card glass">
                                  <div className="order-header-row">
                                      <div className="order-id">TXN: {order.transactionId.substring(0, 15)}...</div>
                                      <div className={`order-status-badge status-${order.orderStatus?.toLowerCase()}`}>
                                          {order.orderStatus}
                                      </div>
                                  </div>
                                  <div className="order-meta">
                                      <div className="meta-item">
                                          <label>Date Ordered</label>
                                          <p>April 12, 2024</p>
                                      </div>
                                      <div className="meta-item">
                                          <label>Total Amount</label>
                                          <p>${order.totalAmount?.toFixed(2)}</p>
                                      </div>
                                      <div className="meta-item">
                                          <label>Payment</label>
                                          <p>{order.paymentMethod} • {order.paymentStatus}</p>
                                      </div>
                                      <div className="meta-item">
                                          <label>Shipping To</label>
                                          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>{order.address}</p>
                                      </div>
                                  </div>
                              </div>
                          ))
                      )}
                  </div>
              </div>
          </main>
      )}

      {/* Checkout Modal */}
      <CheckoutModal
        isOpen={isCheckoutOpen}
        onClose={() => setIsCheckoutOpen(false)}
        amount={checkoutAmount}
        user={user}
        onSuccess={(paymentIntent) => {
          setIsCheckoutOpen(false);
          setMessage("Payment Success! Order Placed.");
          fetchCart(user?.id);
          fetchOrderHistory(user?.id);
          setCurrentView('history');
        }}
      />


      {/* Cart & Wishlist Drawers */}
      <AnimatePresence>
        {(isCartOpen || isWishlistOpen) && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="drawer-overlay"
            onClick={() => { setIsCartOpen(false); setIsWishlistOpen(false); }}
          >
            <motion.div
              initial={{ x: '100%' }}
              animate={{ x: 0 }}
              exit={{ x: '100%' }}
              className="drawer glass"
              onClick={e => e.stopPropagation()}
            >
              <div className="drawer-header">
                <h2>{isCartOpen ? 'Your Cart' : 'Your Wishlist'}</h2>
                <button className="icon-btn" onClick={() => { setIsCartOpen(false); setIsWishlistOpen(false); }}>
                  <ArrowRight size={24} />
                </button>
              </div>

              <div className="drawer-content">
                {(isCartOpen ? cartItems : wishlistItems).length === 0 ? (
                  <div className="empty-state">
                    <p>Your {isCartOpen ? 'cart' : 'wishlist'} is empty.</p>
                  </div>
                ) : (
                  (isCartOpen ? cartItems : wishlistItems).map(item => (
                    <div key={item.id} className="drawer-item glass">
                      <div className="item-info">
                        <h3>{item.productName || `Product #${item.variantId}`}</h3>
                        <div className="item-details">
                          <span className="v-price">${item.offerPrice || item.price}</span>
                          {item.variantAttributes && Object.entries(item.variantAttributes).map(([k, v]) => {
                            const displayVal = typeof v === 'object' && v !== null ? (v.textValue || v.value || v.asText || Object.values(v)[0]?.textValue || '...') : String(v);
                            return (
                              <span key={k} className="v-micro-tag">{k}: {displayVal}</span>
                            );
                          })}
                        </div>
                      </div>
                      <div className="item-actions">
                        <button className="icon-btn-text danger" onClick={() => onRemoveItem(item.id)}>
                          Remove
                        </button>
                      </div>
                    </div>
                  ))
                )}
              </div>

              {isCartOpen && cartItems.length > 0 && (
                <div className="drawer-footer">
                  <div className="total-row">
                    <span>Total</span>
                    <span>${cartItems.reduce((acc, item) => acc + (item.offerPrice || item.price) * (item.quantity || 1), 0)}</span>
                  </div>
                  <button className="btn-solid" onClick={() => {
                    console.log("Stripe Checkout Initiating...");
                    const amount = cartItems.reduce((acc, item) => acc + (item.offerPrice || item.price) * (item.quantity || 1), 0);
                    setCheckoutAmount(amount);
                    setIsCheckoutOpen(true);
                    setIsCartOpen(false);
                  }}>
                    Proceed to Checkout
                  </button>
                </div>
              )}
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
      <AnimatePresence>
        {isLoginModalOpen && (
          <div className="modal-overlay" onClick={() => setIsLoginModalOpen(false)}>
            <motion.div
              className="modal"
              onClick={e => e.stopPropagation()}
              initial={{ scale: 0.9, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.9, opacity: 0 }}
            >
              <h2>{isRegistering ? "Unfold Your Style" : "Welcome Back"}</h2>
              <p style={{ color: 'var(--text-muted)', marginBottom: '2rem' }}>
                {isRegistering ? "Create your account to start shopping." : "Sign in to access your curated shop."}
              </p>

              <form onSubmit={isRegistering ? handleRegister : handleLogin}>
                <div className="form-group">
                  <label>Email Address</label>
                  <input
                    type="email"
                    className="form-input"
                    required
                    value={authForm.email}
                    onChange={e => setAuthForm({ ...authForm, email: e.target.value })}
                  />
                </div>
                {isRegistering && (
                  <div className="form-group">
                    <label>Mobile Number</label>
                    <input
                      type="text"
                      className="form-input"
                      required
                      value={authForm.mobileNumber}
                      onChange={e => setAuthForm({ ...authForm, mobileNumber: e.target.value })}
                    />
                  </div>
                )}
                <div className="form-group">
                  <label>Password</label>
                  <input
                    type="password"
                    className="form-input"
                    required
                    value={authForm.password}
                    onChange={e => setAuthForm({ ...authForm, password: e.target.value })}
                  />
                </div>
                <button type="submit" className="btn-solid">
                  {isRegistering ? "Register" : "Sign In"}
                </button>
              </form>

              <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.9rem' }}>
                {isRegistering ? "Already have an account?" : "Don't have an account?"} {' '}
                <span
                  style={{ color: 'var(--accent)', cursor: 'pointer', fontWeight: 600 }}
                  onClick={() => setIsRegistering(!isRegistering)}
                >
                  {isRegistering ? "Login" : "Create one"}
                </span>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default App;
