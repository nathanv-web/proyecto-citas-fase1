package com.proyectocitas.model;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Enumerated;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolPermisosMappingTest {

    @Test
    void rolDebeTenerColeccionDePermisos() throws Exception {

        Field campo =
                Rol.class.getDeclaredField("permisos");

        assertEquals(
                Set.class,
                campo.getType()
        );

        assertNotNull(
                campo.getAnnotation(ElementCollection.class)
        );

        assertNotNull(
                campo.getAnnotation(Enumerated.class)
        );
    }
}