/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.proyectocitas.config;

/**
 *
 * @author centinel
 */

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springdoc.core.customizers.OpenApiCustomizer;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ComposedSchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.List;
import java.util.Map;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "CitaNova API",
        version = "1.0.0",
        description = "Documentación de la API REST del sistema de citas médicas CitaNova"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {

    /** Describe las estructuras existentes sin cambiar las respuestas del backend. */
    @Bean
    public OpenApiCustomizer documentarRespuestasCompartidas() {
        return api -> {
            Components components = api.getComponents();
            if (components == null) {
                components = new Components();
                api.setComponents(components);
            }

            components.addSchemas("RespuestaError", new ObjectSchema()
                    .description("Respuesta de error. Los campos opcionales dependen del manejador que la genera.")
                    .addProperty("status", new IntegerSchema().description("Código de estado HTTP."))
                    .addProperty("message", new StringSchema().description("Explicación del error."))
                    .addProperty("error", new StringSchema().description("Nombre del estado HTTP, cuando se incluye."))
                    .addProperty("path", new StringSchema().description("Ruta de la solicitud, cuando se incluye."))
                    .addProperty("timestamp", new StringSchema().description("Fecha y hora local del error, cuando se incluye."))
                    .addProperty("code", new StringSchema().description("Código funcional, por ejemplo PASSWORD_CHANGE_REQUIRED."))
                    .addProperty("errors", new ObjectSchema().additionalProperties(new StringSchema())
                            .description("Errores de validación por nombre de campo."))
                    .required(List.of("status", "message")));

            Map<Integer, String> descriptions = Map.of(
                    400, "Solicitud inválida: faltan datos, el JSON es incorrecto o no se cumplen las validaciones.",
                    401, "Autenticación rechazada: faltan credenciales válidas o la cuenta no puede acceder.",
                    403, "Acceso denegado: permiso insuficiente, recurso de otro usuario o cambio de contraseña obligatorio.",
                    404, "No se encontró el recurso solicitado o no está disponible para la operación.",
                    409, "Conflicto: registro duplicado, horario ocupado, estado incompatible o modificación concurrente.",
                    413, "El archivo de foto supera el límite de 1 MiB.",
                    429, "Se superó el límite de solicitudes; se debe esperar antes de volver a intentarlo.",
                    500, "Error interno del servidor al procesar la solicitud.");
            descriptions.forEach((code, description) -> api.getComponents().addResponses("Error" + code,
                    new ApiResponse().description(description).content(new Content().addMediaType(
                            "application/json", new MediaType()
                                    .schema(new Schema<>().$ref("#/components/schemas/RespuestaError"))
                                    .example(Map.of("status", code, "message", description))))));

            components.addSchemas("RespuestaMiPerfil", new ObjectSchema()
                    .description("Perfil y estado de cambio de contraseña del usuario autenticado.")
                    .addProperty("usuario", new Schema<>().$ref("#/components/schemas/LoginUsuarioResponseDTO"))
                    .addProperty("debeCambiarPassword", new BooleanSchema())
                    .required(List.of("usuario", "debeCambiarPassword")));

            ArraySchema roles = new ArraySchema().items(new Schema<>().$ref("#/components/schemas/RolResponseDTO"));
            components.addSchemas("PaginaRoles", new ObjectSchema()
                    .description("Página de roles devuelta al usar search, page o size.")
                    .addProperty("content", roles)
                    .addProperty("pageable", new ObjectSchema().description("Datos de paginación."))
                    .addProperty("totalElements", new IntegerSchema().format("int64"))
                    .addProperty("totalPages", new IntegerSchema())
                    .addProperty("number", new IntegerSchema().description("Página actual, desde cero."))
                    .addProperty("size", new IntegerSchema())
                    .addProperty("numberOfElements", new IntegerSchema())
                    .addProperty("first", new BooleanSchema())
                    .addProperty("last", new BooleanSchema())
                    .addProperty("empty", new BooleanSchema())
                    .addProperty("sort", new ObjectSchema().description("Información de ordenamiento.")));
            components.addSchemas("ListadoRoles", new ComposedSchema()
                    .description("Lista completa sin filtros, o página de resultados al utilizar filtros o paginación.")
                    .addOneOfItem(roles)
                    .addOneOfItem(new Schema<>().$ref("#/components/schemas/PaginaRoles")));
        };
    }
}
