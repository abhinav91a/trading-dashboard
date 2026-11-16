package com.tradingdashboard.gateway.feed;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FeedListener {

    private final FeedHandler feed;

    public FeedListener(FeedHandler feed) {
        this.feed = feed;
    }

    @KafkaListener(topics = {"price-ticks", "alert-triggered"})
    public void onMessage(ConsumerRecord<String, String> record) {
        feed.broadcast(record.topic(), record.value());
    }
}
