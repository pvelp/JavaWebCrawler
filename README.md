# Java Web Crawler

## Stack

- Java 21
- Jsoup 1.10.2 (lib)
- RabbitMQ 5.20.0 (lib)
- Elasticsearch 7.17.19 (lib v8.8.0)
- Kibana 7.17.19

### Run env (RabbitMQ, ELK)
```shell
docker-compose -f images/docker-compose.yml up -d
```

### Stop env 
```shell
docker-compose -f images/docker-compose.yml down
```

### Clear env
```shell
curl -XPUT -H "Content-Type: application/json" http://localhost:9200/_cluster/settings -d '{ "transient": { "cluster.routing.allocation.disk.threshold_enabled": false } }'
```

```shell
curl -XPUT -H "Content-Type: application/json" http://localhost:9200/_all/_settings -d '{"index.blocks.read_only_allow_delete": null}'
```
## Description
Использовал оба вариант получения сообщения: **bacicGet** (в *PageReciever*) и **basicConsume** (в *PagePublisher*). 
>RabbitMQ реализует две различные команды AMQP RPC для выборки сообщений из очереди: Basic.Get и Basic.Consume. ...Basic.Get не является идеальным способом для выборки сообщений с сервера. Для упрощения терминологии, Basic.Get является моделью опроса (polling), в то время как Basic.Consume представляет собой модель с активным источником данных (активная доставка, push).


## Examples

### ElasticSearch
![](https://github.com/pvelp/JavaWebCrawler/blob/main/pics/el.png?raw=true)

### RabbitMQ
![](https://github.com/pvelp/JavaWebCrawler/blob/main/pics/rq1.png?raw=true)
![](https://github.com/pvelp/JavaWebCrawler/blob/main/pics/rq2.png?raw=true)


# Queries and Aggregations
### 1. OR Query
```
POST /pages/_search
{
  "query": {
    "bool": {
      "should": [
        {"match": {"place": "Москва"}},
        {"match": {"header": "Встреча"}}
       ]
    }
  }
}
```

### 2. AND Query
```
POST /pages/_search
{
  "query": {
    "bool": {
      "must": [
        {"match": {"place": "Москва"}},
        {"match": {"header": "Правительство"}}
       ]
    }
  }
}
```

### Script Query
```
POST /pages/_search
{
  "query": {
    "script_score": {
      "query": {"match_all": {}},
      "script": {
        "source": "doc['time'].value.length()"
      }
    }
  }
}
```

### MultiGet Query
```
GET /pages/_mget
{
  "docs": [
    { "_id": "TgoagY8BCG0Vgdhn4NDU" },
    { "_id": "UgoagY8BCG0Vgdhn4tDA" }
  ]
}
```

### Histogram Aggregation
```
POST /pages/_search
{
  "aggs": {
    "header_length_histogram": {
      "histogram": {
        "script": {
          "source": "doc['header'].value.length()",
          "lang": "painless"
        },
        "interval": 10
      }
    }
  }
}
```

### Terms Aggregation
```
POST /pages/_search
{
  "aggs": {
    "popular_authors": {
      "terms": {
        "field": "place"
      }
    }
  }
}
```

### Filter Aggregation
```
POST /pages/_search
{
  "aggs": {
    "filtered_body": {
      "filter": {
        "term": {"place": "Пекин"}
      },
      "aggs": {
        "avg_body_length": {
          "avg": {
            "script": {
              "source": "doc['summary'].value.length()",
              "lang": "painless"
            }
          }
        }
      }
    }
  }
}
```

### Logs Aggregation
```
GET /logstash*/_search
{
  "size": 0, 
  "aggs": {
    "streams": {
      "terms": {
        "field": "stream.keyword",
        "size": 10
      }
    }
  }
}
```