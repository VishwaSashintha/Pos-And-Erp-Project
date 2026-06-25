package com.gradge.erp.tenant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gradge.erp.auth.dto.LoginRequestDto;
import com.gradge.erp.auth.dto.OnboardRequestDto;
import com.gradge.erp.auth.dto.TenantRegisterRequestDto;
import com.gradge.erp.auth.entity.Employee;
import com.gradge.erp.auth.entity.User;
import com.gradge.erp.auth.enums.UserRole;
import com.gradge.erp.auth.repository.EmployeeRepository;
import com.gradge.erp.auth.repository.UserRepository;
import com.gradge.erp.auth.repository.RefreshTokenRepository;
import com.gradge.erp.common.event.EventPublisher;
import com.gradge.erp.common.event.PosSaleCreatedEvent;
import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.entity.InvoiceItem;
import com.gradge.erp.pos.service.InvoiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BosEventAndOnboardingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private InvoiceService invoiceService;

    @MockitoSpyBean
    private EventPublisher eventPublisher;

    private String ownerToken;
    private UUID tenantId;
    private String superAdminToken;

    @BeforeEach
    public void setUp() throws Exception {
        // Clear children tables first to avoid DB FK constraint violations
        refreshTokenRepository.deleteAll();
        employeeRepository.deleteAll();
        
        // Clear all users except the default super admin
        userRepository.findAll().stream()
                .filter(u -> !u.getUsername().equals("superadmin"))
                .forEach(u -> userRepository.delete(u));

        // Explicitly seed the default superadmin user if not already present
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            User superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("superadmin123"))
                    .email("superadmin@platform.com")
                    .role(UserRole.SUPER_ADMIN)
                    .active(true)
                    .build();
            superAdmin.setTenantId(null);
            userRepository.save(superAdmin);
        }

        // 1. Register Tenant (Owner account is auto-created during registration)
        TenantRegisterRequestDto reg = new TenantRegisterRequestDto();
        reg.setTenantName("BOS Global Retail");
        reg.setUsername("retailowner");
        reg.setPassword("ownerpass123");
        reg.setEmail("owner@retail.com");
        reg.setIndustry("RETAIL");
        reg.setSelectedModules(List.of(com.gradge.erp.billing.model.AppModule.POS, com.gradge.erp.billing.model.AppModule.INVENTORY, com.gradge.erp.billing.model.AppModule.ACCOUNTING));

        String res = mockMvc.perform(post("/api/auth/register-tenant")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> map = objectMapper.readValue(res, Map.class);
        Map<?, ?> data = (Map<?, ?>) map.get("data");
        tenantId = UUID.fromString((String) data.get("tenantId"));

        // Login as Owner
        LoginRequestDto login = new LoginRequestDto();
        login.setUsername("retailowner");
        login.setPassword("ownerpass123");

        String loginRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> loginMap = objectMapper.readValue(loginRes, Map.class);
        ownerToken = (String) ((Map<?, ?>) loginMap.get("data")).get("token");

        // Login as Super Admin (Default seeded user on startup)
        LoginRequestDto saLogin = new LoginRequestDto();
        saLogin.setUsername("superadmin");
        saLogin.setPassword("superadmin123");

        String saLoginRes = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(saLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> saLoginMap = objectMapper.readValue(saLoginRes, Map.class);
        superAdminToken = (String) ((Map<?, ?>) saLoginMap.get("data")).get("token");
    }

    @Test
    public void testEmployeeLifecycleAndSecureOnboarding() throws Exception {
        // Step 1: Owner submits new employee creation request (Pending approval)
        Employee newEmp = Employee.builder()
                .name("Alice Cashier")
                .email("alice@retail.com")
                .role(UserRole.CASHIER)
                .department("Checkout")
                .build();

        String createRes = mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + ownerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEmp)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("PENDING_APPROVAL")))
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> createMap = objectMapper.readValue(createRes, Map.class);
        String employeeIdStr = (String) ((Map<?, ?>) createMap.get("data")).get("id");
        UUID employeeId = UUID.fromString(employeeIdStr);

        // Step 2: Super Admin retrieves pending employee list
        mockMvc.perform(get("/api/employees/pending")
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[*].email", hasItem("alice@retail.com")));

        // Step 3: Super Admin approves the employee
        String approveRes = mockMvc.perform(post("/api/employees/" + employeeId + "/approve")
                .header("Authorization", "Bearer " + superAdminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.status", is("APPROVED")))
                .andExpect(jsonPath("$.data.activationToken", notNullValue()))
                .andReturn().getResponse().getContentAsString();

        Map<?, ?> approveMap = objectMapper.readValue(approveRes, Map.class);
        String activationToken = (String) ((Map<?, ?>) approveMap.get("data")).get("activationToken");

        // Verify inactive user record was created in database
        Optional<User> userOpt = userRepository.findByEmail("alice@retail.com");
        assertTrue(userOpt.isPresent());
        assertFalse(userOpt.get().isActive());

        // Step 4: Secure onboarding / activation endpoint called by employee
        OnboardRequestDto onboardReq = new OnboardRequestDto();
        onboardReq.setToken(activationToken);
        onboardReq.setPassword("aliceSecurePass99");

        mockMvc.perform(post("/api/auth/onboard")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(onboardReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.username", is("alice")));

        // Verify user account is now active
        User activeUser = userRepository.findByEmail("alice@retail.com").get();
        assertTrue(activeUser.isActive());

        // Verify activation token is cleared from employee entity
        Employee activeEmp = employeeRepository.findById(employeeId).get();
        assertNull(activeEmp.getActivationToken());
        assertNull(activeEmp.getActivationTokenExpiry());
    }

    @Test
    public void testPosSaleInvoiceEventTrigger() {
        // Create an Invoice in H2
        InvoiceItem item1 = InvoiceItem.builder()
                .productName("Premium Spark Plug")
                .quantity(3)
                .unitPrice(new BigDecimal("15.00"))
                .build();

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-2026-TEST")
                .status(com.gradge.erp.pos.enums.InvoiceStatus.DRAFT)
                .total(new BigDecimal("45.00"))
                .items(List.of(item1))
                .paidAmount(BigDecimal.ZERO)
                .build();

        invoice.getItems().forEach(i -> i.setInvoice(invoice));

        Invoice created = invoiceService.createInvoice(invoice, tenantId);

        // Confirm the Invoice -> triggers POS sale event publishing
        invoiceService.confirmInvoice(created.getId(), tenantId);

        // Capture published PosSaleCreatedEvent using Mockito
        ArgumentCaptor<PosSaleCreatedEvent> captor = ArgumentCaptor.forClass(PosSaleCreatedEvent.class);
        verify(eventPublisher, atLeastOnce()).publishPosSaleCreated(captor.capture());

        PosSaleCreatedEvent publishedEvent = captor.getValue();
        assertEquals(tenantId, publishedEvent.getTenantId());
        assertEquals("INV-2026-TEST", publishedEvent.getInvoiceNumber());
        assertEquals(new BigDecimal("45.00"), publishedEvent.getTotal());
        assertEquals(1, publishedEvent.getItems().size());
        assertEquals("Premium Spark Plug", publishedEvent.getItems().get(0).getProductName());
        assertEquals(3, publishedEvent.getItems().get(0).getQuantity());
    }
}
