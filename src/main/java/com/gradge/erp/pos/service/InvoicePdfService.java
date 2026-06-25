package com.gradge.erp.pos.service;

import com.gradge.erp.pos.entity.Invoice;
import com.gradge.erp.pos.entity.InvoiceItem;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

    public byte[] generateInvoicePdf(Invoice invoice) {
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(31, 41, 55)); 
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(107, 114, 128));
            Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(55, 65, 81));
            Font boldFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(17, 24, 39));
            Font regularFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(55, 65, 81));
            Font thFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);

            
            Paragraph title = new Paragraph("INVOICE", titleFont);
            title.setAlignment(Element.ALIGN_RIGHT);
            document.add(title);

            Paragraph orgName = new Paragraph("Gradge ERP System", boldFont);
            orgName.setAlignment(Element.ALIGN_LEFT);
            document.add(orgName);

            Paragraph subtitle = new Paragraph("Tenant-Isolated Business Solutions", subtitleFont);
            subtitle.setAlignment(Element.ALIGN_LEFT);
            document.add(subtitle);

            document.add(Chunk.NEWLINE);

            
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("Billed To:", headerFont));
            String customerName = (invoice.getCustomer() != null) ? invoice.getCustomer().getName() : "Walk-In Customer";
            leftCell.addElement(new Paragraph(customerName, regularFont));
            if (invoice.getCustomer() != null && invoice.getCustomer().getPhone() != null) {
                leftCell.addElement(new Paragraph("Phone: " + invoice.getCustomer().getPhone(), regularFont));
            }
            metaTable.addCell(leftCell);

            
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(new Paragraph("Invoice Details:", headerFont));
            rightCell.addElement(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber(), regularFont));
            rightCell.addElement(new Paragraph("Status: " + invoice.getStatus().name(), boldFont));
            if (invoice.getCreatedAt() != null) {
                rightCell.addElement(new Paragraph("Date: " + invoice.getCreatedAt().toLocalDate().toString(), regularFont));
            }
            metaTable.addCell(rightCell);

            document.add(metaTable);
            document.add(Chunk.NEWLINE);

            
            PdfPTable table = new PdfPTable(new float[]{3f, 1f, 1f, 1f});
            table.setWidthPercentage(100);

            
            Color thColor = new Color(79, 70, 229); 

            
            String[] headers = {"Product Item", "Unit Price ($)", "Qty", "Total ($)"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, thFont));
                cell.setBackgroundColor(thColor);
                cell.setPadding(8f);
                cell.setHorizontalAlignment(h.equals("Product Item") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
                table.addCell(cell);
            }

            
            for (InvoiceItem item : invoice.getItems()) {
                
                PdfPCell cellName = new PdfPCell(new Phrase(item.getProductName() != null ? item.getProductName() : "Unknown Product", regularFont));
                cellName.setPadding(6f);
                table.addCell(cellName);

                
                PdfPCell cellPrice = new PdfPCell(new Phrase(String.format("%.2f", item.getUnitPrice().doubleValue()), regularFont));
                cellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellPrice.setPadding(6f);
                table.addCell(cellPrice);

                
                PdfPCell cellQty = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), regularFont));
                cellQty.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellQty.setPadding(6f);
                table.addCell(cellQty);

                
                PdfPCell cellTotal = new PdfPCell(new Phrase(String.format("%.2f", item.getLineTotal().doubleValue()), regularFont));
                cellTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                cellTotal.setPadding(6f);
                table.addCell(cellTotal);
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(40);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            addTotalRow(totalsTable, "Subtotal:", String.format("$%.2f", invoice.getSubTotal().doubleValue()), regularFont);
            addTotalRow(totalsTable, "Discount:", String.format("-$%.2f", invoice.getDiscount().doubleValue()), regularFont);
            addTotalRow(totalsTable, "Tax (10%):", String.format("$%.2f", invoice.getTax().doubleValue()), regularFont);
            addTotalRow(totalsTable, "Grand Total:", String.format("$%.2f", invoice.getTotal().doubleValue()), boldFont);
            addTotalRow(totalsTable, "Paid Amount:", String.format("$%.2f", invoice.getPaidAmount().doubleValue()), boldFont);

            document.add(totalsTable);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out.toByteArray();
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, font));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(4f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, font));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(4f);
        table.addCell(valueCell);
    }
}
