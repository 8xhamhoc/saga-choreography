package org.quangphan.order.config;

import org.quangphan.events.inventory.InventoryStatus;
import org.quangphan.events.order.OrderStatus;
import org.quangphan.events.payment.PaymentStatus;
import org.quangphan.order.entity.PurchaseOrder;
import org.quangphan.order.repository.PurchaseOrderRepository;
import org.quangphan.order.service.OrderStatusPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;
import java.util.function.Consumer;

@Slf4j
@Service
public class OrderStatusUpdateEventHandler {

    @Autowired
    private PurchaseOrderRepository repository;

    @Autowired
    private OrderStatusPublisher publisher;

    @Transactional
    public void updateOrder(final Long id, Consumer<PurchaseOrder> consumer) {
        this.repository
                .findById(id)
                .ifPresent(consumer.andThen(this::updateOrder));

    }

    private void updateOrder(PurchaseOrder purchaseOrder) {
        if (Objects.isNull(purchaseOrder.getInventoryStatus()) || Objects.isNull(purchaseOrder.getPaymentStatus()))
            return;
        var isComplete = PaymentStatus.RESERVED.equals(purchaseOrder.getPaymentStatus()) && InventoryStatus.RESERVED.equals(purchaseOrder.getInventoryStatus());
        var orderStatus = isComplete ? OrderStatus.ORDER_COMPLETED : OrderStatus.ORDER_CANCELLED;
        purchaseOrder.setOrderStatus(orderStatus);
        if (!isComplete) {
            this.publisher.raiseOrderEvent(purchaseOrder, orderStatus);
        }
    }

}
