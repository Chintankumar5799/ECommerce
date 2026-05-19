package com.example.demo.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class TicketDatabaseTool {

	private final TicketService ticketService;

	public TicketDatabaseTool(TicketService ticketService) {
		this.ticketService = ticketService;
	}

	private final static Logger log = LoggerFactory.getLogger(TicketDatabaseTool.class);

	// @Tool(description="This tool helps to create new ticket in database")
	// public Ticket createTicketTool(@ToolParam (description="Ticket details.")
	// Ticket ticket) {
	// return ticketService.createTicket(ticket);
	// }

	@Tool(description = "Create a new support ticket in the database")
	public Ticket createTicketTool(
			@ToolParam(description = "Short summary of the issue") String summary,
			@ToolParam(description = "Detailed description") String description,
			@ToolParam(description = "Issue category: technical/account/hardware/software") String category,
			@ToolParam(description = "Priority: P1 for urgent, P2 for high, P3 for low") String priority,
			@ToolParam(description = "User email address") String email) {

		log.info("Call customize tool for create new support ticket");
		Ticket ticket = new Ticket();
		ticket.setSummary(summary);
		ticket.setDescription(description);
		ticket.setCategory(category);
		ticket.setPriority(parsePriority(priority));
		ticket.setEmail(email);
		ticket.setStatus(Status.OPEN);
		return ticketService.createTicket(ticket);
	}

	private Priority parsePriority(String priority) {
		try {
			return Priority.valueOf(priority.toUpperCase());
		} catch (IllegalArgumentException e) {
			log.warn("Invalid priority: {}", priority);
			return Priority.P3;
		}
	}

	@Tool(description = "This tool helps to get ticket by username.")
	public Ticket getTicketByUserName(@ToolParam(description = "username whoose ticket is required") String emailId) {
		log.info("Call customize tool to get ticket by username");
		return ticketService.getTicketByEmailId(emailId);
	}

	@Tool(description = "This tool helps to update ticket.")
	public Ticket updateTicket(@ToolParam(description = "new ticket details with ticket id.") Ticket ticket) {
		log.info("Call customize tool to update");
		return ticketService.updateTicket(ticket);
	}
}
