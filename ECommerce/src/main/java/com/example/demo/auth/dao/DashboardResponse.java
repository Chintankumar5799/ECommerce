package com.example.demo.auth.dao;

public class DashboardResponse {
	
	 private int totalProducts;
	 private int productsInStock;
	 private int pendingOrders;
	 private double totalRevenue;
	 private boolean isApproved;

	 public int getProductsInStock() {
		return productsInStock;
	 }
	 public void setProductsInStock(int productsInStock) {
		this.productsInStock = productsInStock;
	 }
		public int getTotalProducts() {
			return totalProducts;
		}
		public void setTotalProducts(int totalProducts) {
			this.totalProducts = totalProducts;
		}
		public int getPendingOrders() {
			return pendingOrders;
		}
		public void setPendingOrders(int pendingOrders) {
			this.pendingOrders = pendingOrders;
		}
		public double getTotalRevenue() {
			return totalRevenue;
		}
		public void setTotalRevenue(double totalRevenue) {
			this.totalRevenue = totalRevenue;
		}
		public boolean isApproved() {
			return isApproved;
		}
		public void setApproved(boolean isApproved) {
			this.isApproved = isApproved;
		}
	    
	    
	    

}
