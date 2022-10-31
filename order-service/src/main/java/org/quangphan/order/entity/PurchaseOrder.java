package org.quangphan.order.entity;

import org.quangphan.events.inventory.InventoryStatus;
import org.quangphan.events.order.OrderStatus;
import org.quangphan.events.payment.PaymentStatus;
import lombok.Data;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;

@Data
@Entity
@ToString
public class PurchaseOrder {

    @Id
    private Long id;
    private Integer userId;
    private Integer productId;
    private Integer price;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private InventoryStatus inventoryStatus;

    @Version
    private int version;

}