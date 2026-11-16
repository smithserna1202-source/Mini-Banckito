package application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI miniBanckitoOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Mini-Banckito API")
                .description("API académica para la gestión de clientes, empresas, cuentas, productos, préstamos y transferencias.")
                .version("1.0.0")
                .contact(new Contact().name("Equipo Mini-Banckito"))
                .license(new License().name("MIT")));
    }
}
