package com.example.demo.helpdesk;

import java.time.LocalDateTime;

import org.hibernate.annotations.GeneratorType;

//import jakarta.annotation.Priority;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name="help_desk_tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Lob
	private String summary;
	
	@Enumerated(EnumType.STRING)
	private Priority priority;
	
	@Column(unique=true)
	private String username;
	
	private LocalDateTime createdOn;
	
	private LocalDateTime updatedOn;
	
	@Enumerated(EnumType.STRING)
	private Status status;
	
	@PrePersist
	void preSave() {
		if(this.createdOn==null) {
			this.createdOn=LocalDateTime.now();
		}
		
		this.updatedOn=LocalDateTime.now();
	}
	
	@PreUpdate
	void preUpdate() {
		this.updatedOn=LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public LocalDateTime getUpdatedOn() {
		return updatedOn;
	}

	public void setUpdatedOn(LocalDateTime updatedOn) {
		this.updatedOn = updatedOn;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public Ticket(Long id, String summary, Priority priority, String username, LocalDateTime createdOn,
			LocalDateTime updatedOn, Status status) {
		super();
		this.id = id;
		this.summary = summary;
		this.priority = priority;
		this.username = username;
		this.createdOn = createdOn;
		this.updatedOn = updatedOn;
		this.status = status;
	}

	public Ticket() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
