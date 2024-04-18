cd ../docker-elk7
docker-compose -f docker-compose.elk7.yml up -d
cd ../RabbitMQ
docker-compose -f docker-compose.rmq.yml up -d