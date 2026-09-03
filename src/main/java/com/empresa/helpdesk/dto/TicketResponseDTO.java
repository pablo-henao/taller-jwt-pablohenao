package com.empresa.helpdesk.dto;

import com.empresa.helpdesk.model.EstadoTicket;
import com.empresa.helpdesk.model.PrioridadTicket;
import com.empresa.helpdesk.model.Ticket;

import java.time.LocalDateTime;

public record TicketResponseDTO(
        Long id,
        String titulo,
        String descripcion,
        PrioridadTicket prioridad,
        EstadoTicket estado,
        String creadorUsername,
        String tecnicoAsignado,
        LocalDateTime fechaCreacion
) {
    public static TicketResponseDTO fromEntity(Ticket ticket) {
        return new TicketResponseDTO(
                ticket.getId(),
                ticket.getTitulo(),
                ticket.getDescripcion(),
                ticket.getPrioridad(),
                ticket.getEstado(),
                ticket.getCreadorUsername(),
                ticket.getTecnicoAsignado(),
                ticket.getFechaCreacion()
        );
    }
}
