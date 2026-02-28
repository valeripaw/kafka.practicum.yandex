# Запуск Kafka-кластера

Запустите kafka кластер командой:
```
docker-compose -f docker-compose-kafka5.yml up -d
```

Файл `docker-compose-kafka5.yml` лежит в корне проекта.

Подождите 1–2 минуты, пока все сервисы запустятся.

# Топики

Создаем `topic-1`:
```
docker exec kafka1 kafka-topics \
  --create \
  --topic topic-1 \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 3 \
  --replication-factor 3
```
Создаем `topic-2`:
```
docker exec kafka1 kafka-topics \
  --create \
  --topic topic-2 \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --partitions 3 \
  --replication-factor 3
```

## Настраиваем права
В файлах `kafka_server_jaas.conf` уже лежат пользователи `producer` и `consumer`:
```
user_producer="prod-secret"
user_consumer="cons-secret"
```
topic-1 должен быть доступен как для продьюсеров, так и для консьюмеров.
Т.е. для продьюсера выдаем права на запись:
```
docker exec kafka1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic topic-1
```
А для консьюмера выдаем права за чтение:
```
docker exec kafka1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:consumer \
  --operation READ \
  --topic topic-1
```
Права на группу для консьюмера:
```
docker exec kafka1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:consumer \
  --operation Read \
  --group topic-1.1
```

topic-2 должен быть доступен только для продьюсеров. Консьюмеры не имеют доступа к чтению данных.
Т.е. для продьюсера выдаем права на запись:
```
docker exec kafka1 kafka-acls \
  --bootstrap-server kafka1:19092 \
  --command-config /etc/kafka/secrets/admin-client-configs.conf \
  --add \
  --allow-principal User:producer \
  --operation WRITE \
  --topic topic-2
```

# Запуск приложения

Запустите приложение командой
```
docker-compose -f docker-compose-module5.yml up -d
```

Файл `docker-compose-module5.yml` лежит в корне проекта.

Подождите 1–2 минуты, пока пока поднимутся два контейнера.

## Проверка

В приложении настроены два консьюмера и два продьюсера.
Консьюмер для `topic-1` должен стартануть успешно.
Консьюмер для `topic-2` упадет с ошибками (сделано специально, чтобы проверить права доступа):
```
2026-03-02T19:54:06.190+04:00  WARN 35501 --- [-listener-0-C-1] org.apache.kafka.clients.NetworkClient   : [Consumer clientId=consumer-topic-2.1-2, groupId=topic-2.1] The metadata response from the cluster reported a recoverable issue with correlation id 3 : {topic-2=TOPIC_AUTHORIZATION_FAILED}
2026-03-02T19:54:06.190+04:00 ERROR 35501 --- [-listener-0-C-1] org.apache.kafka.clients.Metadata        : [Consumer clientId=consumer-topic-2.1-2, groupId=topic-2.1] Topic authorization failed for topics [topic-2]
2026-03-02T19:54:06.190+04:00 ERROR 35501 --- [-listener-0-C-1] o.s.k.l.KafkaMessageListenerContainer    : Authentication/Authorization Exception and no authExceptionRetryInterval set
   org.apache.kafka.common.errors.TopicAuthorizationException: Not authorized to access topics: [topic-2]
```

Для продьюсеров сделан rest контроллер. Сообщения можно отправить, например, так:
- для `topic-1`:
```
curl -X POST localhost:9193/messages/topic1 \
  -d "hello kafka 1"  
```

- для `topic-2`:
```
curl -X POST localhost:9193/messages/topic2 \
  -d "hello kafka 2"  
```

# Остановка кластера

1. Остановите кластер командой:
```
docker-compose down
```

2. Для полной очистки (включая данные) можно использовать команду:
```
docker-compose down -v
```
