package org.quangphan.payment.service;

import org.quangphan.dto.PaymentDto;
import org.quangphan.events.order.OrderEvent;
import org.quangphan.events.payment.PaymentEvent;
import org.quangphan.events.payment.PaymentStatus;
import org.quangphan.payment.entity.UserBalance;
import org.quangphan.payment.entity.UserTransaction;
import org.quangphan.payment.repository.UserBalanceRepository;
import org.quangphan.payment.repository.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PaymentService {

    @Autowired
    private UserBalanceRepository balanceRepository;

    @Autowired
    private UserTransactionRepository transactionRepository;

    @PostConstruct
    public void initDB() {
        balanceRepository.saveAll(Stream.of(
                        new UserBalance(1, 100),
                        new UserBalance(2, 150),
                        new UserBalance(3, 5)
                ).collect(Collectors.toList())
        );
    }

    @Transactional
    public PaymentEvent newOrderEvent(OrderEvent orderEvent) {
        var purchaseOrder = orderEvent.getPurchaseOrder();
        var dto = new PaymentDto(purchaseOrder.getOrderId(), purchaseOrder.getUserId(), purchaseOrder.getPrice());

        return this.balanceRepository.findById(purchaseOrder.getUserId())
                .filter(ub -> ub.getBalance() >= purchaseOrder.getPrice())
                .map(ub -> {
                    ub.setBalance(ub.getBalance() - purchaseOrder.getPrice());
                    this.transactionRepository.save(new UserTransaction(purchaseOrder.getOrderId(), purchaseOrder.getUserId(), purchaseOrder.getPrice()));
                    return new PaymentEvent(dto, PaymentStatus.RESERVED);
                })
                .orElse(new PaymentEvent(dto, PaymentStatus.REJECTED));
    }

    @Transactional
    public void cancelOrderEvent(OrderEvent orderEvent) {
        this.transactionRepository.findById(orderEvent.getPurchaseOrder().getOrderId())
                .ifPresent(ut -> {
                    this.transactionRepository.delete(ut);
                    this.balanceRepository.findById(ut.getUserId())
                            .ifPresent(ub -> ub.setBalance(ub.getBalance() + ut.getAmount()));
                });
    }
}
