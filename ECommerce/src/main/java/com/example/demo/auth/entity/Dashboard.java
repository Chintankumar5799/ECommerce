package com.example.demo.auth.entity;

public class Dashboard {
	
	    private String sellerName;
	    private int totalOrders;
	    private double totalRevenue;
	    private int pendingOrders;
	    private int productsInStock;
	    
	 
		public Dashboard() {
			super();
			
		}	
		public Dashboard(String sellerName, int totalOrders, double totalRevenue, int pendingOrders,
				int productsInStock) {
			super();
			this.sellerName = sellerName;
			this.totalOrders = totalOrders;
			this.totalRevenue = totalRevenue;
			this.pendingOrders = pendingOrders;
			this.productsInStock = productsInStock;
		}
		
		public String getSellerName() {
			return sellerName;
		}
		public void setSellerName(String sellerName) {
			this.sellerName = sellerName;
		}
		public int getTotalOrders() {
			return totalOrders;
		}
		public void setTotalOrders(int totalOrders) {
			this.totalOrders = totalOrders;
		}
		public double getTotalRevenue() {
			return totalRevenue;
		}
		public void setTotalRevenue(double totalRevenue) {
			this.totalRevenue = totalRevenue;
		}
		public int getPendingOrders() {
			return pendingOrders;
		}
		public void setPendingOrders(int pendingOrders) {
			this.pendingOrders = pendingOrders;
		}
		public int getProductsInStock() {
			return productsInStock;
		}
		public void setProductsInStock(int productsInStock) {
			this.productsInStock = productsInStock;
		}
	    
}
