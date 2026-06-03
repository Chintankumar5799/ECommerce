package com.ecommerce.demo.helpdesk;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Long>{
//	Optional<Ticket> findByUsername(String username);
	Optional<Ticket> findByEmail(String email);

}
