import React, { useState, useEffect } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import { paymentService, orderService } from '../services/api';
import { X, CreditCard, Lock } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';

// Replace with your actual Stripe publishable key
const stripePromise = loadStripe('pk_test_51SiTHVRdrzVkXTxr64opYlISOue6NrBBFurDNtl206E3PceilcMXSHXH1ipY4aaA2RYWeFIt4lE0N0mnSavY0vRX00QCWmm3g4');

const CARD_ELEMENT_OPTIONS = {
    style: {
        base: {
            color: '#ffffff',
            fontFamily: '"Inter", sans-serif',
            fontSmoothing: 'antialiased',
            fontSize: '16px',
            '::placeholder': {
                color: '#aab7c4',
            },
        },
        invalid: {
            color: '#fa755a',
            iconColor: '#fa755a',
        },
    },
};

const CheckoutForm = ({ amount, user, onCancel, onSuccess }) => {
    const stripe = useStripe();
    const elements = useElements();
    const [error, setError] = useState(null);
    const [processing, setProcessing] = useState(false);
    const [succeeded, setSucceeded] = useState(false);
    const [address, setAddress] = useState('');

    const handleSubmit = async (event) => {
        event.preventDefault();

        if (!stripe || !elements) return;
        if (!address) {
            setError("Please provide a shipping address.");
            return;
        }

        setProcessing(true);
        setError(null);

        try {
            // Step 1: Create Payment Intent
            const intentRes = await paymentService.createPaymentIntent({
                amount: Math.round(amount * 100),
                currency: 'usd',
                userId: user?.id,
            });

            const clientSecret = intentRes.data.clientSecret;

            // Step 2: Confirm Payment
            const payload = await stripe.confirmCardPayment(clientSecret, {
                payment_method: {
                    card: elements.getElement(CardElement),
                    billing_details: {
                        name: user?.email || 'Guest User',
                    },
                },
            });

            if (payload.error) {
                setError(`Payment failed: ${payload.error.message}`);
                setProcessing(false);
            } else {
                try {
                    // Step 3: Confirm Order on Backend
                    // Note: If you want to send address, the backend confirmOrder needs to be updated.
                    // For now, we call it as per the current backend signature.
                    await orderService.confirmOrder(user?.id, payload.paymentIntent.id);
                    
                    setSucceeded(true);
                    setProcessing(false);
                    setTimeout(() => onSuccess(payload.paymentIntent), 2000);
                } catch (orderErr) {
                    setError('Payment succeeded, but order sync failed. Contact support.');
                    setProcessing(false);
                }
            }
        } catch (err) {
            setError(err.message || 'An unexpected error occurred.');
            setProcessing(false);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="checkout-form">
            <div className="form-group">
                <label>Shipping Address</label>
                <textarea 
                    className="form-input" 
                    placeholder="Enter your full street address, city, and zip..."
                    rows="3"
                    required
                    value={address}
                    onChange={(e) => setAddress(e.target.value)}
                    style={{ resize: 'none' }}
                />
            </div>

            <div className="form-group">
                <label>Payment Information</label>
                <div className="card-input-container glass" style={{ background: 'var(--bg)', border: '1px solid var(--border)' }}>
                    <CardElement options={CARD_ELEMENT_OPTIONS} />
                </div>
            </div>

            {error && (
                <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="payment-error glass">
                    {error}
                </motion.div>
            )}

            {succeeded ? (
                <motion.div initial={{ scale: 0.9 }} animate={{ scale: 1 }} className="payment-success glass">
                    Payment Successful! Order Confirmed.
                </motion.div>
            ) : (
                <button
                    disabled={processing || !stripe || succeeded}
                    className="btn-solid fill-width"
                    style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '10px', height: '60px' }}
                >
                    {processing ? (
                        <span className="spinner"></span>
                    ) : (
                        <>
                            <Lock size={18} /> Complete Secure Payment
                        </>
                    )}
                </button>
            )}
            
            <p style={{ textAlign: 'center', fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '1rem' }}>
                Your payment is processed securely by Stripe. We never store your card details.
            </p>
        </form>
    );
};

export const CheckoutModal = ({ isOpen, onClose, amount, user, onSuccess }) => {
    return (
        <AnimatePresence>
            {isOpen && (
                <motion.div
                    key="checkout-modal-overlay"
                    className="modal-overlay"
                    onClick={onClose}
                    style={{ zIndex: 9999 }}
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                >
                    <motion.div
                        className="modal checkout-modal glass"
                        onClick={(e) => e.stopPropagation()}
                        initial={{ y: 50, opacity: 0 }}
                        animate={{ y: 0, opacity: 1 }}
                        exit={{ y: 50, opacity: 0 }}
                    >
                        <div className="modal-header">
                            <h2><CreditCard size={24} style={{ marginRight: '8px', verticalAlign: 'middle' }} /> Secure Checkout</h2>
                            <button className="icon-btn-text" onClick={onClose}><X size={24} /></button>
                        </div>

                        <div className="checkout-amount-display">
                            <span>Total to pay:</span>
                            <h3>${(amount || 0).toFixed(2)}</h3>
                        </div>

                        <Elements stripe={stripePromise}>
                            <CheckoutForm amount={amount} user={user} onCancel={onClose} onSuccess={onSuccess} />
                        </Elements>
                    </motion.div>
                </motion.div>
            )}
        </AnimatePresence>
    );
};
