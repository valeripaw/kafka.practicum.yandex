# Запуск Kafka-кластера

1. Запустите kafka кластер командой:
```
docker-compose -f docker-compose-kafka.yml up -d
```

Подождите 1–2 минуты, пока все сервисы запустятся.

## Параметры конфигурации и их значение

| Параметр | Описание |
|--------|--------|
| `KAFKA_BROKER_ID` | Уникальный идентификатор брокера в кластере (1, 2, 3). |
| `KAFKA_ZOOKEEPER_CONNECT` | Адрес ZooKeeper для координации кластера. |
| `KAFKA_LISTENERS` | Адрес, по которому брокер принимает входящие соединения. |
| `KAFKA_ADVERTISED_LISTENERS` | Адрес, который брокер сообщает клиентам (внешний интерфейс). |
| `KAFKA_INTER_BROKER_LISTENER_NAME` | Имя слушателя для внутренней коммуникации между брокерами. |
| `KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 3` | Гарантирует, что топик `__consumer_offsets` реплицируется на все 3 брокера (высокая доступность). |
| `KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 3` | Аналогично для транзакционных логов. |
| `KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 2` | Минимальное количество реплик, которые должны подтвердить запись для транзакций. |

# Проверка работоспособности кластера

1. Проверьте статус контейнеров командой:
```
docker-compose ps
```
или
```
docker ps
```

Команда `docker ps` выведет состояние всех контейнеров, даже тех, что были подняты вне `docker-compose`.

Убедитесь, что все контейнеры (`zookeeper`, `kafka1`, `kafka2`, `kafka3`, `kafka-ui`) находятся в статусе `Up`. 
В консоле будут записи вида:
```
NAME        IMAGE                             COMMAND                  SERVICE     CREATED         STATUS         PORTS
kafka-ui    provectuslabs/kafka-ui:latest     "/bin/sh -c 'java --…"   kafka-ui    5 seconds ago   Up 4 seconds   0.0.0.0:8080->8080/tcp, :::8080->8080/tcp
kafka1      confluentinc/cp-kafka:7.6.0       "/etc/confluent/dock…"   kafka1      5 seconds ago   Up 4 seconds   0.0.0.0:9092->9092/tcp, :::9092->9092/tcp
kafka2      confluentinc/cp-kafka:7.6.0       "/etc/confluent/dock…"   kafka2      5 seconds ago   Up 4 seconds   9092/tcp, 0.0.0.0:9093->9093/tcp, :::9093->9093/tcp
kafka3      confluentinc/cp-kafka:7.6.0       "/etc/confluent/dock…"   kafka3      5 seconds ago   Up 4 seconds   9092/tcp, 0.0.0.0:9094->9094/tcp, :::9094->9094/tcp
zookeeper   confluentinc/cp-zookeeper:7.6.0   "/etc/confluent/dock…"   zookeeper   5 seconds ago   Up 4 seconds   2888/tcp, 0.0.0.0:2181->2181/tcp, :::2181->2181/tcp, 3888/tcp
```

# Проверка через Kafka UI

1. Откройте браузер и перейдите по адресу: [http://localhost:8080](http://localhost:8080)

2. Дождитесь автоматической загрузки кластера `local-cluster`.

3. В интерфейсе Вы увидите:
   - Список топиков (включая `test-topic`)
   - Количество партиций и реплик
   - Потребителей (consumers)
   - Метрики брокеров

4. Вы можете:
   - Просматривать сообщения в топиках
   - Создавать новые топики через UI
   - Отслеживать потребление сообщений

# Запуск приложения

1. Запустите приложение командой 

```
docker-compose -f docker-compose-module2.yml up -d
```

Подождите 1–2 минуты, пока пока поднимутся два контейнера.

Приложение само:
- создаст три топика, указанные в конфигах: `kafka.blocked-user.topic`, `kafka.private-message.topic` и `kafka.censored-message.topic`;
- сгенерирует список заблокированных пользователей и отправит его в `kafka.blocked-user.topic`;
- сгенерирует сообщения от всех пользователей всем пользователям, даже заблокированным, и отправит их в `kafka.private-message.topic`.

Топология, созданная в BlockingService, читает блокировки и сообщения, и фильтрует сообщения в соответствие с блокировками.

# Остановка кластера

1. Остановите кластер командой:
```
docker-compose down
```

2. Для полной очистки (включая данные) можно использовать команду:
```
docker-compose down -v
```