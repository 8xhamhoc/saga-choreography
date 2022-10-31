package org.quangphan.inventory.config;

import org.quangphan.events.inventory.InventoryEvent;
import org.quangphan.events.order.OrderEvent;
import org.quangphan.events.order.OrderStatus;
import org.quangphan.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Configuration
public class InventoryConfig {

    @Autowired
    private InventoryService service;

    @Bean
    public Function<Flux<OrderEvent>, Flux<InventoryEvent>> inventoryProcessor() {
        return flux -> flux.flatMap(this::processInventory);
    }

    private Mono<InventoryEvent> processInventory(OrderEvent event) {
        if (event.getOrderStatus().equals(OrderStatus.ORDER_CREATED)) {
            return Mono.fromSupplier(() -> this.service.newOrderInventory(event));
        }
        return Mono.fromRunnable(() -> this.service.cancelOrderInventory(event));
    }

}

