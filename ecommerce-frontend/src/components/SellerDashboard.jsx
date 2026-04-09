import React, { useState, useEffect } from 'react';
import { sellerService } from '../services/api';
import { motion } from 'framer-motion';
import { Package, TrendingUp, DollarSign, Clock, ShieldAlert, ShieldCheck } from 'lucide-react';

export const SellerDashboard = () => {
    const [stats, setStats] = useState({
        totalProducts: 0,
        productsInStock: 0,
        pendingOrders: 0,
        totalRevenue: 0,
        approved: false
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            const response = await sellerService.getDashboard();
            setStats(response.data);
            setLoading(false);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to authenticate store data');
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="dashboard-loading" style={{ textAlign: 'center', padding: '150px' }}>
                <div className="spinner" style={{ width: '50px', height: '50px', borderTopColor: 'var(--accent)' }}></div>
                <p style={{ marginTop: '20px', color: 'var(--text-muted)' }}>Retrieving Store Performance...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="dashboard-error glass" style={{ margin: '140px auto', maxWidth: '600px', color: 'white', padding: '40px', borderRadius: '24px', textAlign: 'center' }}>
                <ShieldAlert size={48} color="#fa755a" style={{ marginBottom: '20px' }} />
                <h3 style={{ fontSize: '1.5rem', marginBottom: '10px' }}>Access Restricted</h3>
                <p style={{ color: 'var(--text-muted)' }}>{error}</p>
            </div>
        );
    }

    return (
        <motion.div
            className="seller-dashboard-container"
            style={{ padding: '60px 4rem', maxWidth: '1600px', margin: '90px auto' }}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
        >
            <div className="dashboard-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '60px' }}>
                <div className="header-titles">
                    <h1 style={{ fontSize: '3rem', marginBottom: '10px', fontWeight: '800', letterSpacing: '-2px' }}>Store Overview</h1>
                    <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem' }}>Performance metrics and catalog distribution.</p>
                </div>

                <div className={`status-badge ${stats.approved ? 'approved' : 'pending'}`} style={{ 
                    display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 24px', borderRadius: '14px', 
                    background: stats.approved ? 'rgba(16, 185, 129, 0.1)' : 'rgba(245, 158, 11, 0.1)', 
                    color: stats.approved ? '#10b981' : '#f59e0b', fontWeight: '800', fontSize: '0.9rem',
                    border: '1px solid rgba(255,255,255,0.05)'
                }}>
                    {stats.approved ? <ShieldCheck size={20} /> : <ShieldAlert size={20} />}
                    {stats.approved ? 'Verified Merchant' : 'Verification In Progress'}
                </div>
            </div>

            <div className="dashboard-grid" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '30px' }}>
                {/* Revenue Card */}
                <motion.div
                    className="stat-card glass"
                    style={{ padding: '40px', borderRadius: '30px', display: 'flex', flexDirection: 'column', gap: '20px' }}
                    whileHover={{ y: -8, borderOutlineColor: 'var(--accent)' }}
                >
                    <div className="stat-icon-wrapper" style={{ background: 'rgba(124, 105, 255, 0.1)', width: '60px', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '18px' }}>
                        <DollarSign size={28} color="var(--accent)" />
                    </div>
                    <div className="stat-info">
                        <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '2px', marginBottom: '8px' }}>Estimated Revenue</h3>
                        <h2 style={{ fontSize: '2.5rem', fontWeight: '800', background: 'var(--gold-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                            ${stats.totalRevenue?.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                        </h2>
                    </div>
                </motion.div>

                {/* Total Products Card */}
                <motion.div
                    className="stat-card glass"
                    style={{ padding: '40px', borderRadius: '30px', display: 'flex', flexDirection: 'column', gap: '20px' }}
                    whileHover={{ y: -8 }}
                >
                    <div className="stat-icon-wrapper" style={{ background: 'rgba(59, 130, 246, 0.1)', width: '60px', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '18px' }}>
                        <Package size={28} color="#3b82f6" />
                    </div>
                    <div className="stat-info">
                        <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '2px', marginBottom: '8px' }}>Collection Size</h3>
                        <h2 style={{ fontSize: '2.5rem', fontWeight: '800' }}>{stats.totalProducts} <span style={{ fontSize: '1rem', color: 'var(--text-muted)', fontWeight: '400' }}>SKUs</span></h2>
                    </div>
                </motion.div>

                {/* Stock Level Card */}
                <motion.div
                    className="stat-card glass"
                    style={{ padding: '40px', borderRadius: '30px', display: 'flex', flexDirection: 'column', gap: '20px' }}
                    whileHover={{ y: -8 }}
                >
                    <div className="stat-icon-wrapper" style={{ background: 'rgba(16, 185, 129, 0.1)', width: '60px', height: '60px', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '18px' }}>
                        <TrendingUp size={28} color="#10b981" />
                    </div>
                    <div className="stat-info">
                        <h3 style={{ fontSize: '0.8rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '2px', marginBottom: '8px' }}>In-Stock Units</h3>
                        <h2 style={{ fontSize: '2.5rem', fontWeight: '800' }}>{stats.productsInStock} <span style={{ fontSize: '1rem', color: 'var(--text-muted)', fontWeight: '400' }}>items</span></h2>
                    </div>
                </motion.div>
            </div>
        </motion.div>
    );
};
