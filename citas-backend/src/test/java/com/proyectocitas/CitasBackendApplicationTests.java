package com.proyectocitas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:citanova-context;MODE=MariaDB;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=never",
    "jwt.secret=clave-exclusiva-de-pruebas-sin-valor-productivo-01234567890123456789",
    "jwt.expiration=3600000"
})
class CitasBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
