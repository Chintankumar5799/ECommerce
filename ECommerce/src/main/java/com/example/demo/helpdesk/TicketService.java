package com.example.demo.helpdesk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    
    private final static Logger log=LoggerFactory.getLogger(TicketService.class);

    // create
    @Transactional
    public Ticket createTicket(Ticket ticket) {
    	log.info("create ticket method for tool "+ticket.getId());
    	ticket.setId(null);
        return ticketRepository.save(ticket);
    }

    // update
    @Transactional
    public Ticket updateTicket(Ticket ticket) {
    	log.info("update ticket method for tool "+ticket.getId());
        return ticketRepository.save(ticket);
    }

    // get ticket

    public Ticket getTicket(Long ticketId) {
    	log.info("get ticket method for tool "+ticketId);
        return ticketRepository.findById(ticketId).orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

//    public Ticket getTicketByUserName(String username) {
//        return ticketRepository.findByUsername(username).orElse(null);
//    }
    
    public Ticket getTicketByUserName(String emailId) {
    	log.info("get ticket by username for tool with mail "+emailId);
        return ticketRepository.findByEmail(emailId).orElse(null);
    }

	public Ticket getTicketByEmailId(String emailId) {
		log.info("get ticket by username for tool with mail "+emailId);
		 return ticketRepository.findByEmail(emailId).orElse(null);
	}


    // delete

}
