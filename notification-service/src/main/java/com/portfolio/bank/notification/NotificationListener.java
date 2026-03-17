package com.portfolio.bank.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j // Lombok annotation to give us a logger
public class NotificationListener {

    @KafkaListener(topics = "transaction-events", groupId = "notification-group")
    public void handleTransactionEvent(MoneyTransferredEvent event) {

        log.info("======================================================");
        log.info("📧 NEW EMAIL RECEIPT DISPATCHED");
        log.info("======================================================");
        log.info("From Account : {}", event.sourceAccountId());
        log.info("To Account   : {}", event.destinationAccountId());
        log.info("Amount       : ${}", event.amount());
        log.info("Time         : {}", event.timestamp());
        log.info("======================================================");

        // In a real application, you would put SendGrid or AWS SES Java code here
        // to actually send a real email to the user!
    }
}