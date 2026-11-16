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
        if ("alert-triggered".equals(record.topic())) {
            // Alert events are keyed by user ID. Without a key, drop the event instead of
            // sending it to everyone.
            if (record.key() != null) {
                feed.broadcast(record.topic(), record.key(), record.value());
            }
            return;
        }
        // Price ticks are public to every authenticated client.
        feed.broadcast(record.topic(), null, record.value());
    }
}
