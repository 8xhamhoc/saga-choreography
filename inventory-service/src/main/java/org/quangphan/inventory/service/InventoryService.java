package org.quangphan.inventory.service;

import org.quangphan.dto.InventoryDto;
import org.quangphan.events.inventory.InventoryEvent;
import org.quangphan.events.inventory.InventoryStatus;
import org.quangphan.events.order.OrderEvent;
import org.quangphan.inventory.entity.OrderInventory;
import org.quangphan.inventory.entity.OrderInventoryConsumption;
import org.quangphan.inventory.repository.OrderInventoryConsumptionRepository;
import org.quangphan.inventory.repository.OrderInventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InventoryService {

    @Autowired
    private OrderInventoryRepository inventoryRepository;

    @Autowired
    private OrderInventoryConsumptionRepository consumptionRepository;

    @PostConstruct
    public void initDB() {
        inventoryRepository.saveAll(Stream.of(
                new OrderInventory(1, 5),
                new OrderInventory(2, 10),
                new OrderInventory(3, 15)
                ).collect(Collectors.toList())
        );
    }

    @Transactional
    public InventoryEvent newOrderInventory(OrderEvent orderEvent) {
        InventoryDto dto = InventoryDto.of(orderEvent.getPurchaseOrder().getOrderId(), orderEvent.getPurchaseOrder().getProductId());
        return inventoryRepository.findById(orderEvent.getPurchaseOrder().getProductId())
                .filter(i -> i.getAvailableInventory() > 0)
                .map(i -> {
                    i.setAvailableInventory(i.getAvailableInventory() - 1);
                    consumptionRepository.save(OrderInventoryConsumption.of(orderEvent.getPurchaseOrder().getOrderId(), orderEvent.getPurchaseOrder().getProductId(), 1));
                    return new InventoryEvent(dto, InventoryStatus.RESERVED);
                })
                .orElse(new InventoryEvent(dto, InventoryStatus.REJECTED));
    }

    @Transactional
    public void cancelOrderInventory(OrderEvent orderEvent) {
        consumptionRepository.findById(orderEvent.getPurchaseOrder().getOrderId())
                .ifPresent(ci -> {
                    inventoryRepository.findById(ci.getProductId())
                            .ifPresent(i ->
                                    i.setAvailableInventory(i.getAvailableInventory() + ci.getQuantityConsumed())
                            );
                    consumptionRepository.delete(ci);
                });
    }

}
