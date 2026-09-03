package com.empresa.helpdesk.dto;

import com.empresa.helpdesk.model.PrioridadTicket;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TicketRequestDTO(
        @NotBlank(message = "El título no puede estar vacío")
        String titulo,

        @NotBlank(message = "La descripción no puede estar vacía")
        String descripcion,

        @NotNull(message = "La prioridad es obligatoria")
        PrioridadTicket prioridad
) {}
