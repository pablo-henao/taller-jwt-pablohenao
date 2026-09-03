package com.empresa.helpdesk.controller;

import com.empresa.helpdesk.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TicketService ticketService;

    @GetMapping("/auditoria")
    public ResponseEntity<Map<String, Object>> obtenerAuditoria() {
        int totalTickets = ticketService.listarTodos().size();
        return ResponseEntity.ok(Map.of(
                "sistema", "Helpdesk Enterprise Security API",
                "estado", "OPERACIONAL",
                "totalTickets", totalTickets,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
