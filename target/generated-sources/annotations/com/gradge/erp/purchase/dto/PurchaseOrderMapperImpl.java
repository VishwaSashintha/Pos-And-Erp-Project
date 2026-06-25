package com.gradge.erp.purchase.dto;

import com.gradge.erp.inventory.entity.Product;
import com.gradge.erp.purchase.entity.PurchaseOrder;
import com.gradge.erp.purchase.entity.PurchaseOrderItem;
import com.gradge.erp.supplier.entity.Supplier;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-23T14:17:42+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 25.0.2 (Oracle Corporation)"
)
@Component
public class PurchaseOrderMapperImpl implements PurchaseOrderMapper {

    @Override
    public PurchaseOrderResponseDto toResponseDto(PurchaseOrder entity) {
        if ( entity == null ) {
            return null;
        }

        PurchaseOrderResponseDto.PurchaseOrderResponseDtoBuilder purchaseOrderResponseDto = PurchaseOrderResponseDto.builder();

        purchaseOrderResponseDto.supplierId( entitySupplierId( entity ) );
        purchaseOrderResponseDto.supplierName( entitySupplierName( entity ) );
        purchaseOrderResponseDto.id( entity.getId() );
        purchaseOrderResponseDto.poNumber( entity.getPoNumber() );
        if ( entity.getStatus() != null ) {
            purchaseOrderResponseDto.status( entity.getStatus().name() );
        }
        purchaseOrderResponseDto.totalAmount( entity.getTotalAmount() );
        purchaseOrderResponseDto.notes( entity.getNotes() );
        purchaseOrderResponseDto.items( toItemResponseDtoList( entity.getItems() ) );
        purchaseOrderResponseDto.createdAt( entity.getCreatedAt() );
        purchaseOrderResponseDto.updatedAt( entity.getUpdatedAt() );

        return purchaseOrderResponseDto.build();
    }

    @Override
    public List<PurchaseOrderResponseDto> toResponseDtoList(List<PurchaseOrder> entities) {
        if ( entities == null ) {
            return null;
        }

        List<PurchaseOrderResponseDto> list = new ArrayList<PurchaseOrderResponseDto>( entities.size() );
        for ( PurchaseOrder purchaseOrder : entities ) {
            list.add( toResponseDto( purchaseOrder ) );
        }

        return list;
    }

    @Override
    public PurchaseOrderResponseDto.PurchaseOrderItemResponseDto toItemResponseDto(PurchaseOrderItem item) {
        if ( item == null ) {
            return null;
        }

        PurchaseOrderResponseDto.PurchaseOrderItemResponseDto.PurchaseOrderItemResponseDtoBuilder purchaseOrderItemResponseDto = PurchaseOrderResponseDto.PurchaseOrderItemResponseDto.builder();

        purchaseOrderItemResponseDto.productId( itemProductId( item ) );
        purchaseOrderItemResponseDto.productName( itemProductName( item ) );
        purchaseOrderItemResponseDto.id( item.getId() );
        purchaseOrderItemResponseDto.quantity( item.getQuantity() );
        purchaseOrderItemResponseDto.unitCost( item.getUnitCost() );
        purchaseOrderItemResponseDto.totalCost( item.getTotalCost() );

        return purchaseOrderItemResponseDto.build();
    }

    @Override
    public List<PurchaseOrderResponseDto.PurchaseOrderItemResponseDto> toItemResponseDtoList(List<PurchaseOrderItem> items) {
        if ( items == null ) {
            return null;
        }

        List<PurchaseOrderResponseDto.PurchaseOrderItemResponseDto> list = new ArrayList<PurchaseOrderResponseDto.PurchaseOrderItemResponseDto>( items.size() );
        for ( PurchaseOrderItem purchaseOrderItem : items ) {
            list.add( toItemResponseDto( purchaseOrderItem ) );
        }

        return list;
    }

    private UUID entitySupplierId(PurchaseOrder purchaseOrder) {
        if ( purchaseOrder == null ) {
            return null;
        }
        Supplier supplier = purchaseOrder.getSupplier();
        if ( supplier == null ) {
            return null;
        }
        UUID id = supplier.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String entitySupplierName(PurchaseOrder purchaseOrder) {
        if ( purchaseOrder == null ) {
            return null;
        }
        Supplier supplier = purchaseOrder.getSupplier();
        if ( supplier == null ) {
            return null;
        }
        String name = supplier.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }

    private UUID itemProductId(PurchaseOrderItem purchaseOrderItem) {
        if ( purchaseOrderItem == null ) {
            return null;
        }
        Product product = purchaseOrderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        UUID id = product.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String itemProductName(PurchaseOrderItem purchaseOrderItem) {
        if ( purchaseOrderItem == null ) {
            return null;
        }
        Product product = purchaseOrderItem.getProduct();
        if ( product == null ) {
            return null;
        }
        String name = product.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}
