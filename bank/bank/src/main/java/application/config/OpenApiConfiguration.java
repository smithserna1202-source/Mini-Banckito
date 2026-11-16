package application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    public OpenAPI miniBanckitoOpenAPI() {
        final String basicAuth = "basicAuth";
        return new OpenAPI()
            .info(new Info()
                .title("Mini-Banckito API")
                .description("API académica para la gestión de clientes, empresas, cuentas, productos, préstamos y transferencias. Los endpoints protegidos requieren autenticación HTTP Basic.")
                .version("1.0.0")
                .contact(new Contact().name("Equipo Mini-Banckito"))
                .license(new License().name("MIT")))
            .components(new Components().addSecuritySchemes(basicAuth,
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic")))
            .addSecurityItem(new SecurityRequirement().addList(basicAuth));
    }
}
