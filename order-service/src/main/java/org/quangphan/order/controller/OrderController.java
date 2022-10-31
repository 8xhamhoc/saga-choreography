package org.quangphan.order.controller;

import org.quangphan.dto.OrderRequestDto;
import org.quangphan.order.entity.PurchaseOrder;
import org.quangphan.order.service.OrderCommandService;
import org.quangphan.order.service.OrderQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("order")
public class OrderController {

    @Autowired
    private OrderCommandService commandService;

    @Autowired
    private OrderQueryService queryService;

    @PostMapping("/create")
    public PurchaseOrder createOrder(@RequestBody OrderRequestDto requestDTO) {
        requestDTO.setOrderId(requestDTO.getOrderId());
        return this.commandService.createOrder(requestDTO);
    }

    @GetMapping("/all")
    public List<PurchaseOrder> getOrders() {
        return this.queryService.getAll();
    }

}
