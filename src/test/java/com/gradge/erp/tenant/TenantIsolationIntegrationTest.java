package com.gradge.erp.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradge.erp.auth.dto.LoginRequestDto;
import com.gradge.erp.auth.dto.TenantRegisterRequestDto;
import com.gradge.erp.customer.entity.Customer;
import com.gradge.erp.customer.repository.CustomerRepository;
import com.gradge.erp.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TenantIsolationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomerRepository customerRepository;

    @MockitoBean
    private NotificationService notificationService;

    private String tokenTenantA;
    private String tokenTenantB;
    private UUID tenantIdA;
    private UUID tenantIdB;

    @BeforeEach
    public void setUp() throws Exception {
        customerRepository.deleteAll();

        // 1. Register Tenant A & Admin A
        TenantRegisterRequestDto regA = new TenantRegisterRequestDto();
        regA.setTenantName("TenantA");
        regA.setUsername("adminA");
        regA.setPassword("passwordA");
        regA.setEmail("admina@test.com");

        String resA = mockMvc.perform(post("/api/auth/register-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> mapA = objectMapper.readValue(resA, Map.class);
        Map<?, ?> dataA = (Map<?, ?>) mapA.get("data");
        tenantIdA = UUID.fromString((String) dataA.get("tenantId"));

        // Login Tenant A
        LoginRequestDto loginA = new LoginRequestDto();
        loginA.setEmail("admina@test.com");
        loginA.setPassword("passwordA");

        String loginResA = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> loginMapA = objectMapper.readValue(loginResA, Map.class);
        Map<?, ?> dataLoginA = (Map<?, ?>) loginMapA.get("data");
        tokenTenantA = (String) dataLoginA.get("token");

        // 2. Register Tenant B & Admin B
        TenantRegisterRequestDto regB = new TenantRegisterRequestDto();
        regB.setTenantName("TenantB");
        regB.setUsername("adminB");
        regB.setPassword("passwordB");
        regB.setEmail("adminb@test.com");

        String resB = mockMvc.perform(post("/api/auth/register-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regB)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();;

        Map<?, ?> mapB = objectMapper.readValue(resB, Map.class);
        Map<?, ?> dataB = (Map<?, ?>) mapB.get("data");
        tenantIdB = UUID.fromString((String) dataB.get("tenantId"));

        // Login Tenant B
        LoginRequestDto loginB = new LoginRequestDto();
        loginB.setEmail("adminb@test.com");
        loginB.setPassword("passwordB");

        String loginResB = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginB)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> loginMapB = objectMapper.readValue(loginResB, Map.class);
        Map<?, ?> dataLoginB = (Map<?, ?>) loginMapB.get("data");
        tokenTenantB = (String) dataLoginB.get("token");
    }

    @Test
    public void testTenantIsolationOnCustomerAccess() throws Exception {
        // Create customer in Tenant A using Tenant A's token
        Customer customerA = Customer.builder()
                .name("John Doe Tenant A")
                .phone("1234567890")
                .email("john@tenantA.com")
                .address("123 Main St")
                .nic("12345V")
                .build();

        mockMvc.perform(post("/api/customers")
                .header("Authorization", "Bearer " + tokenTenantA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerA)))
                .andExpect(status().isOk());

        // Get customers for Tenant A using Tenant A's token -> should succeed and return Tenant A's customer
        mockMvc.perform(get("/api/customers/" + tenantIdA)
                .header("Authorization", "Bearer " + tokenTenantA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name", is("John Doe Tenant A")));

        // Get customers for Tenant A using Tenant B's token -> should return 403 Forbidden
        mockMvc.perform(get("/api/customers/" + tenantIdA)
                .header("Authorization", "Bearer " + tokenTenantB))
                .andExpect(status().isForbidden());

        // Get customers for Tenant B using Tenant A's token -> should return 403 Forbidden
        mockMvc.perform(get("/api/customers/" + tenantIdB)
                .header("Authorization", "Bearer " + tokenTenantA))
                .andExpect(status().isForbidden());
    }
}
