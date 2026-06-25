package com.gradge.erp.purchase;

import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.inventory.service.StockService;
import com.gradge.erp.purchase.entity.GoodsReceivedNote;
import com.gradge.erp.purchase.entity.PurchaseOrder;
import com.gradge.erp.purchase.entity.PurchaseOrderItem;
import com.gradge.erp.purchase.enums.PurchaseOrderStatus;
import com.gradge.erp.finance.entity.Account;
import com.gradge.erp.finance.enums.AccountType;
import com.gradge.erp.finance.repository.AccountRepository;
import com.gradge.erp.purchase.repository.PurchaseOrderRepository;
import com.gradge.erp.purchase.service.PurchaseService;
import com.gradge.erp.supplier.entity.Supplier;
import com.gradge.erp.supplier.repository.SupplierRepository;
import com.gradge.erp.tenant.context.TenantContext;
import com.gradge.erp.tenant.entity.Tenant;
import com.gradge.erp.tenant.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PurchaseOrderReceiveIntegrationTest {

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StockService stockService;

    private Tenant tenant;
    private Product product;
    private PurchaseOrder testOrder;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setName("PO Test Garage");
        tenant = tenantRepository.save(tenant);
        
        TenantContext.setTenantId(tenant.getId());

        Account invAccount = new Account();
        invAccount.setTenantId(tenant.getId());
        invAccount.setCode("1400");
        invAccount.setName("Inventory");
        invAccount.setType(AccountType.ASSET);

        Account apAccount = new Account();
        apAccount.setTenantId(tenant.getId());
        apAccount.setCode("2000");
        apAccount.setName("Accounts Payable");
        apAccount.setType(AccountType.LIABILITY);

        accountRepository.saveAll(List.of(invAccount, apAccount));

        Supplier supplier = new Supplier();
        supplier.setTenantId(tenant.getId());
        supplier.setName("Test Supplier");
        supplier = supplierRepository.save(supplier);

        product = new Product();
        product.setTenantId(tenant.getId());
        product.setName("Brake Pads");
        product.setSku("BP-100");
        product.setQuantity(10);
        product.setCostPrice(15.00);
        product.setSellingPrice(30.00);
        product = productRepository.save(product);

        testOrder = new PurchaseOrder();
        testOrder.setTenantId(tenant.getId());
        testOrder.setSupplier(supplier);
        testOrder.setPoNumber("PO-TEST-1");
        testOrder.setStatus(PurchaseOrderStatus.SUBMITTED);

        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setPurchaseOrder(testOrder);
        item.setProduct(product);
        item.setQuantity(50.0);
        item.setUnitCost(new BigDecimal("15.00"));

        testOrder.setItems(new java.util.ArrayList<>(List.of(item)));
        testOrder = purchaseOrderRepository.save(testOrder);
    }

    @Test
    void receiveGoods_updatesStock() {
        Double qtyToReceive = 50.0;
        
        GoodsReceivedNote grn = purchaseService.receiveGoods(
                testOrder.getId(), 
                product.getId(), 
                qtyToReceive, 
                "Received intact", 
                tenant.getId()
        );

        assertThat(grn).isNotNull();
        assertThat(grn.getQuantityReceived()).isEqualTo(qtyToReceive);
        
        // Stock should be initial (0) + received (50) = 50
        Double newStock = stockService.getStock(product, tenant);
        assertThat(newStock).isEqualTo(50.0);
        
        PurchaseOrder updated = purchaseOrderRepository.findById(testOrder.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(PurchaseOrderStatus.RECEIVED);
    }
}
