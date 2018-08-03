# Kafka Simple WebHooks
Send kafka messages to a URL.

###### Features:
- Kafka 1.1.0 client.
- Send messages at least once.
- Request method and headers are configurable.
- Retries requests with exponential backoff.

## Description
Using kafka streams client, consumes messages from kafka on the configured topic.

For each message, an HTTP Post request will be executed to the configured URL.
The body of the request will be the binary content of the Kafka message (you can change the `Content-Type` of the request, see more below).

If the request fails, it will be retried. If exhausts retry executions, the container will stop with an error.

## Usage

```bash
docker run -d \
  -e "webhook.url=https://my-service:8080/" \
  -e "kafka.topic=test" \
  -e "kafka.application.id=webhooks" \
  -e "kafka.bootstrap.servers=kafka:9092" \
  graphpathai/kafka-simple-webhooks
```

See `docker-compose.yml` for an example.

## Config
Set the following keys on environment variables to override defaults. 

#### WebHook
- `webhook.url`: HTTP/S endpoint where the messages will be sent to (**Required**).
- `webhook.method`: Method to use to send the messages to the url (Default: `POST`).
- `webhook.headers.*`: Custom headers to set on the requests (Default: `webhook.headers.Content-Type = "application/octet-stream"`).
- `webhook.retry.maxExecutions`: Number of retries if the HTTP request fail (status code != 2XX) (Default: 10).
- `webhook.retry.baseWait`: Time to wait between retries using random exponential backoff (Default: 100 millis).
- `webhook.retry.maxWait`: Maximum time to wait on each retry (the retry will never wait more than `maxWait`) (Default: 5 seconds).
- `webhook.retry.timeout`: Maximum time to wait for a successful response since the first request (Default: 10 minutes).

#### HTTP Client
We use Play WS client (https://github.com/playframework/play-ws).
Some defaults are overwritten on `application.conf`.

- `play.ws.ahc.*`: Configuration specific to the Ahc implementation of the WS client (Defaults: https://github.com/playframework/play-ws/blob/1.1.x/play-ahc-ws-standalone/src/main/resources/reference.conf).
- `play.ws.*`: Configuration for Play WS (Defaults: https://github.com/playframework/play-ws/blob/1.1.x/play-ws-standalone/src/main/resources/reference.conf).

#### Kafka
- `kafka.topic`: Webhook content topic. For each message on this topic a request will be triggered to the url (**Required**).
- `kafka.application.id`: Streams application id (**Required**).
- `kafka.bootstrap.servers`: Kafka servers (**Required**).
- `kafka.*`: Streams config properties as described on the docs (https://kafka.apache.org/11/documentation.html#streamsconfigs).
- `kafka.consumer.*`: Consumer config as describe on the docs (https://kafka.apache.org/11/documentation.html#newconsumerconfigs).
