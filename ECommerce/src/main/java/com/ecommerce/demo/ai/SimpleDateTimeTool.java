package com.ecommerce.demo.ai;

import java.time.LocalDateTime;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SimpleDateTimeTool {
//To use tool along with AI
	
	@Tool(description="Get the current date and time in users zone.")
	public String getCurrentDateTime() {
		return LocalDateTime.now()
				.atZone(LocaleContextHolder.getTimeZone().toZoneId())
						.toString();
	}
}
