package com.empresa.helpdesk.controller;

import com.empresa.helpdesk.dto.ActualizarEstadoDTO;
import com.empresa.helpdesk.dto.TicketRequestDTO;
import com.empresa.helpdesk.dto.TicketResponseDTO;
import com.empresa.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponseDTO> crearTicket(@Valid @RequestBody TicketRequestDTO dto, Principal principal) {
        String username = (principal != null) ? principal.getName() : "anonimo";
        TicketResponseDTO respuesta = ticketService.crearTicket(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> listarTickets() {
        return ResponseEntity.ok(ticketService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.buscarPorId(id));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<TicketResponseDTO> actualizarEstado(@PathVariable Long id,
                                                              @Valid @RequestBody ActualizarEstadoDTO dto) {
        return ResponseEntity.ok(ticketService.actualizarEstado(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTicket(@PathVariable Long id) {
        ticketService.eliminarTicket(id);
        return ResponseEntity.noContent().build();
    }
}
