package com.gradge.erp.billing.model;

public enum AppFeature {
    POS_BILLING(AppModule.POS),
    POS_REFUND(AppModule.POS),
    POS_DISCOUNT(AppModule.POS),
    
    INV_PRODUCTS(AppModule.INVENTORY),
    INV_WAREHOUSE(AppModule.INVENTORY),
    
    ACC_LEDGER(AppModule.ACCOUNTING),
    ACC_REPORTS(AppModule.ACCOUNTING),
    
    CRM_LEADS(AppModule.CRM),
    
    HRM_EMPLOYEES(AppModule.HRM);

    private final AppModule module;

    AppFeature(AppModule module) {
        this.module = module;
    }

    public AppModule getModule() {
        return module;
    }
}
