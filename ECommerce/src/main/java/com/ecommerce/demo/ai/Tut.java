package com.ecommerce.demo.ai;

public class Tut{
	private String title;
	private String content;
	private String createdYear;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getCreatedYear() {
		return createdYear;
	}
	public void setCreatedYear(String createdYear) {
		this.createdYear = createdYear;
	}
	public Tut(String title, String content, String createdYear) {
		super();
		this.title = title;
		this.content = content;
		this.createdYear = createdYear;
	}
	public Tut() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}