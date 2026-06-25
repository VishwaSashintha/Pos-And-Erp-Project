package com.gradge.erp.reporting.service;

import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.repository.InvoiceRepository;
import com.gradge.erp.inventory.repository.ProductRepository;
import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.reporting.dto.InventoryReportDTO;
import com.gradge.erp.reporting.dto.SalesReportDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;

    public SalesReportDTO getSalesReport(UUID tenantId) {
        List<Invoice> invoices = invoiceRepository.findByTenant_IdAndDeletedFalse(tenantId);

        double totalSales = 0;
        double totalTax = 0;
        double totalDiscount = 0;

        for (Invoice inv : invoices) {
            totalSales    += inv.getTotal()    != null ? inv.getTotal().doubleValue()    : 0.0;
            totalTax      += inv.getTax()      != null ? inv.getTax().doubleValue()      : 0.0;
            totalDiscount += inv.getDiscount() != null ? inv.getDiscount().doubleValue() : 0.0;
        }

        return SalesReportDTO.builder()
                .totalSales(totalSales)
                .totalTax(totalTax)
                .totalDiscount(totalDiscount)
                .netRevenue(totalSales - totalDiscount)
                .invoiceCount(invoices.size())
                .build();
    }

    public InventoryReportDTO getInventoryReport(UUID tenantId) {
        List<Product> products = productRepository.findByTenant_IdAndDeletedFalse(tenantId);

        int lowStock = 0;
        double stockValue = 0;

        for (Product p : products) {
            if (p.getQuantity() != null && p.getReorderLevel() != null
                    && p.getQuantity() <= p.getReorderLevel()) {
                lowStock++;
            }
            if (p.getQuantity() != null && p.getCostPrice() != null) {
                stockValue += (p.getQuantity() * p.getCostPrice());
            }
        }

        return InventoryReportDTO.builder()
                .totalProducts(products.size())
                .totalStockValue(stockValue)
                .lowStockItems(lowStock)
                .build();
    }

    /**
     * Generates an Excel (.xlsx) workbook with two sheets:
     *  - Sheet 1: Sales Report  (one row per invoice)
     *  - Sheet 2: Inventory Report (one row per product)
     */
    public byte[] generateExcelReport(UUID tenantId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Styles ──────────────────────────────────────────────
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat fmt = workbook.createDataFormat();
            currencyStyle.setDataFormat(fmt.getFormat("#,##0.00"));

            // ── Sheet 1: Sales ──────────────────────────────────────
            Sheet salesSheet = workbook.createSheet("Sales Report");
            String[] salesHeaders = {
                "Invoice Number", "Customer", "Status",
                "Sub-Total ($)", "Discount ($)", "Tax ($)", "Total ($)", "Paid ($)"
            };
            buildHeaderRow(salesSheet, salesHeaders, headerStyle);

            List<Invoice> invoices = invoiceRepository.findByTenant_IdAndDeletedFalse(tenantId);
            int rowIdx = 1;
            for (Invoice inv : invoices) {
                Row row = salesSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "");
                row.createCell(1).setCellValue(inv.getCustomer() != null ? inv.getCustomer().getName() : "Walk-in");
                row.createCell(2).setCellValue(inv.getStatus() != null ? inv.getStatus().name() : "");
                createCurrencyCell(row, 3, inv.getSubTotal()   != null ? inv.getSubTotal().doubleValue()   : 0, currencyStyle);
                createCurrencyCell(row, 4, inv.getDiscount()   != null ? inv.getDiscount().doubleValue()   : 0, currencyStyle);
                createCurrencyCell(row, 5, inv.getTax()        != null ? inv.getTax().doubleValue()        : 0, currencyStyle);
                createCurrencyCell(row, 6, inv.getTotal()      != null ? inv.getTotal().doubleValue()      : 0, currencyStyle);
                createCurrencyCell(row, 7, inv.getPaidAmount() != null ? inv.getPaidAmount().doubleValue() : 0, currencyStyle);
            }
            for (int i = 0; i < salesHeaders.length; i++) salesSheet.autoSizeColumn(i);

            // ── Sheet 2: Inventory ──────────────────────────────────
            Sheet inventorySheet = workbook.createSheet("Inventory Report");
            String[] inventoryHeaders = {
                "SKU", "Name", "Category", "Qty On Hand", "Reorder Level",
                "Cost Price ($)", "Selling Price ($)", "Stock Value ($)", "Status"
            };
            buildHeaderRow(inventorySheet, inventoryHeaders, headerStyle);

            List<Product> products = productRepository.findByTenant_IdAndDeletedFalse(tenantId);
            rowIdx = 1;
            for (Product p : products) {
                Row row = inventorySheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(p.getSku()  != null ? p.getSku()  : "");
                row.createCell(1).setCellValue(p.getName() != null ? p.getName() : "");
                row.createCell(2).setCellValue(p.getCategory() != null ? p.getCategory().getName() : "");
                row.createCell(3).setCellValue(p.getQuantity()    != null ? p.getQuantity()    : 0);
                row.createCell(4).setCellValue(p.getReorderLevel()!= null ? p.getReorderLevel(): 0);
                createCurrencyCell(row, 5, p.getCostPrice()    != null ? p.getCostPrice()    : 0, currencyStyle);
                createCurrencyCell(row, 6, p.getSellingPrice() != null ? p.getSellingPrice() : 0, currencyStyle);
                double stockVal = (p.getQuantity() != null && p.getCostPrice() != null)
                        ? p.getQuantity() * p.getCostPrice() : 0;
                createCurrencyCell(row, 7, stockVal, currencyStyle);
                boolean isLow = p.getQuantity() != null && p.getReorderLevel() != null
                        && p.getQuantity() <= p.getReorderLevel();
                row.createCell(8).setCellValue(isLow ? "LOW STOCK" : "OK");
            }
            for (int i = 0; i < inventoryHeaders.length; i++) inventorySheet.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void buildHeaderRow(Sheet sheet, String[] headers, CellStyle style) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void createCurrencyCell(Row row, int col, double value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
