package com.gradge.erp.billing.model;

public enum AppPermission {
    POS_BILLING_CREATE(AppFeature.POS_BILLING),
    POS_BILLING_VIEW(AppFeature.POS_BILLING),
    POS_REFUND_APPLY(AppFeature.POS_REFUND),
    POS_DISCOUNT_APPLY(AppFeature.POS_DISCOUNT),
    
    INV_PRODUCTS_MANAGE(AppFeature.INV_PRODUCTS),
    INV_PRODUCTS_VIEW(AppFeature.INV_PRODUCTS),
    INV_WAREHOUSE_MANAGE(AppFeature.INV_WAREHOUSE),
    INV_WAREHOUSE_VIEW(AppFeature.INV_WAREHOUSE),
    
    ACC_LEDGER_RECORD(AppFeature.ACC_LEDGER),
    ACC_LEDGER_VIEW(AppFeature.ACC_LEDGER),
    ACC_REPORTS_VIEW(AppFeature.ACC_REPORTS),
    
    CRM_LEADS_MANAGE(AppFeature.CRM_LEADS),
    CRM_LEADS_VIEW(AppFeature.CRM_LEADS),
    
    HRM_EMPLOYEES_MANAGE(AppFeature.HRM_EMPLOYEES),
    HRM_EMPLOYEES_VIEW(AppFeature.HRM_EMPLOYEES);

    private final AppFeature feature;

    AppPermission(AppFeature feature) {
        this.feature = feature;
    }

    public AppFeature getFeature() {
        return feature;
    }
}
