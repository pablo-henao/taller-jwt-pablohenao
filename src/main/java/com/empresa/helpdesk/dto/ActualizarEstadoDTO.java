package com.empresa.helpdesk.dto;

import com.empresa.helpdesk.model.EstadoTicket;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoDTO(
        @NotNull(message = "El nuevo estado es obligatorio")
        EstadoTicket estado,

        String tecnicoAsignado
) {}
