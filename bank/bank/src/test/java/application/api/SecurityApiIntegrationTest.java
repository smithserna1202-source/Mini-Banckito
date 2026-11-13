package application.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "BANK_DEFAULT_PASSWORD=Integration-Only-Password-123",
    "spring.datasource.url=jdbc:h2:mem:security-test;DB_CLOSE_DELAY=-1"
})
class SecurityApiIntegrationTest {
    @Autowired private MockMvc mvc;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
    }

    @Test
    void accountEndpointRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/accounts/0000000001/balance")).andExpect(status().isUnauthorized());
    }

    @Test
    void customerCannotOpenAccount() throws Exception {
        String body = """
            {"accountNumber":"1234567890","ownerId":"C001","type":"SAVINGS","initialBalance":0,"currency":"COP"}
            """;
        mvc.perform(post("/api/accounts")
            .with(SecurityMockMvcRequestPostProcessors.httpBasic("C001", "Integration-Only-Password-123"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
            .andExpect(status().isForbidden());
    }
}
