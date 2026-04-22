import json
import os
import sys
import time

from kafka import KafkaConsumer


BOOTSTRAP_SERVERS = os.getenv("BOOTSTRAP_SERVERS", "kafka:29092")
TOPIC = os.getenv("TOPIC", "orderdb.order_service_db.outbox_event")
GROUP_ID = os.getenv("GROUP_ID", "order-outbox-consumer")


def create_consumer():
    return KafkaConsumer(
        TOPIC,
        bootstrap_servers=BOOTSTRAP_SERVERS,
        group_id=GROUP_ID,
        auto_offset_reset="earliest",
        enable_auto_commit=True,
        value_deserializer=lambda value: json.loads(value.decode("utf-8")),
        consumer_timeout_ms=0,
    )


def main():
    print(
        f"[event-consumer] bootstrap={BOOTSTRAP_SERVERS}, topic={TOPIC}, group={GROUP_ID}",
        flush=True,
    )

    while True:
        try:
            consumer = create_consumer()
            print("[event-consumer] connected", flush=True)
            while True:
                records = consumer.poll(timeout_ms=3000)
                if not records:
                    continue

                for _, messages in records.items():
                    for message in messages:
                        print("[event-consumer] message received", flush=True)
                        print(
                            json.dumps(
                                {
                                    "topic": message.topic,
                                    "partition": message.partition,
                                    "offset": message.offset,
                                    "value": message.value,
                                },
                                ensure_ascii=False,
                            ),
                            flush=True,
                        )
        except Exception as exc:
            print(f"[event-consumer] retrying after error: {exc}", file=sys.stderr, flush=True)
            time.sleep(3)


if __name__ == "__main__":
    main()
