package com.empresa.helpdesk.service;

import com.empresa.helpdesk.dto.ActualizarEstadoDTO;
import com.empresa.helpdesk.dto.TicketRequestDTO;
import com.empresa.helpdesk.dto.TicketResponseDTO;
import com.empresa.helpdesk.exception.ResourceNotFoundException;
import com.empresa.helpdesk.model.EstadoTicket;
import com.empresa.helpdesk.model.Ticket;
import com.empresa.helpdesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    @Transactional
    public TicketResponseDTO crearTicket(TicketRequestDTO dto, String creadorUsername) {
        Ticket ticket = Ticket.builder()
                .titulo(dto.titulo())
                .descripcion(dto.descripcion())
                .prioridad(dto.prioridad())
                .estado(EstadoTicket.ABIERTO)
                .creadorUsername(creadorUsername != null ? creadorUsername : "anonimo")
                .build();

        Ticket guardado = ticketRepository.save(ticket);
        return TicketResponseDTO.fromEntity(guardado);
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDTO> listarTodos() {
        return ticketRepository.findAll()
                .stream()
                .map(TicketResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO buscarPorId(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + id));
        return TicketResponseDTO.fromEntity(ticket);
    }

    @Transactional
    public TicketResponseDTO actualizarEstado(Long id, ActualizarEstadoDTO dto) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket no encontrado con ID: " + id));

        ticket.setEstado(dto.estado());
        if (dto.tecnicoAsignado() != null && !dto.tecnicoAsignado().isBlank()) {
            ticket.setTecnicoAsignado(dto.tecnicoAsignado());
        }

        Ticket actualizado = ticketRepository.save(ticket);
        return TicketResponseDTO.fromEntity(actualizado);
    }

    @Transactional
    public void eliminarTicket(Long id) {
        if (!ticketRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ticket no encontrado con ID: " + id);
        }
        ticketRepository.deleteById(id);
    }
}
