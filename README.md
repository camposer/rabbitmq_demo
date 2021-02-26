# RabbitMQ Simple Demo App

## Build
```
mvn package
```

* Tests use in memory ActiveMQ

# Run
```
java -jar target/xxx
```

* You need RabbitMQ running on your local. Instructions for [installing RabbitMQ on Windows](https://www.rabbitmq.com/install-windows.html)
* The project includes a UI, see: http://localhost:8080

# Send info via cURL
```
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" --data-urlencode "message=$(cat target/model.txt)" http://localhost:8080/process
```