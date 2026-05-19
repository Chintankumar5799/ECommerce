package com.example.demo.helpdesk;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailTool {

	@Tool(description = "This tool helps to send email to suppost team regarding new ticket.")
	@Async
	public void sendEmailToSupportTeam(
			@ToolParam(description = "Email id associated with ticker for contact information") String email,
			@ToolParam(description = "Short description of ticket summary.") String message) {

	}
}
