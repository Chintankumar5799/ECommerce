// package com.example.demo.helpdesk;

// import org.springframework.stereotype.Service;

// @Service
// public class TicketService {

// private final TicketRepository ticketRepository;

// public TicketService(TicketRepository ticketRepository) {
// this.ticketRepository = ticketRepository;
// }

// // create
// public Ticket createTicket(Ticket ticket) {
// return ticketRepository.save(ticket);
// }

// // update

// public Ticket updateTicket(Ticket ticket) {
// return ticketRepository.save(ticket);
// }

// // get ticket

// public Ticket getTicket(Long ticketId) {
// return ticketRepository.findById(ticketId).orElseThrow(() -> new
// IllegalArgumentException("Ticket not found"));
// }

// public Ticket getTicketByUserName(String username) {
// return ticketRepository.findByUsername(username).orElse(null);
// }

// // delete

// }
