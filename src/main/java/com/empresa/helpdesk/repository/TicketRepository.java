package com.empresa.helpdesk.repository;

import com.empresa.helpdesk.model.EstadoTicket;
import com.empresa.helpdesk.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreadorUsername(String creadorUsername);
    List<Ticket> findByEstado(EstadoTicket estado);
}
