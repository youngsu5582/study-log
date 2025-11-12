---
tags:
  - topic/elasticsearch
  - type/case-study
moc: Elasticsearch
sub-topic: Bool Query, Range Query
description: 조건을 조합하려면 Bool Query, 숫자 및 날짜 범위 검색하려면 Range
status: evergreen
modified: 2025-11-12T23:08:42+09:00
original_video_url: https://youtu.be/5QCdtdt9Y0g?si=QwJ18VFDVNDhkEPI
---

여러 쿼리를 조합하기 위해선 bool 쿼리를 사용한다.
-> 상위에 bool 쿼리를 사용후, 그 안에 다른 쿼리들을 넣는식으로 사용

4개의 인자를 가지고 있다.

- must : 쿼리가 참인 도큐먼트 검색
- must_not : 쿼리가 거짓인 도큐먼트 검색 - 스코어 계산은 X
- should : 검색 결과 중 이 쿼리에 해당하는 도큐먼트 점수 높임
- filter : 쿼리가 참인 도큐먼트를 검색하나, 스코어 계산은 X - must 보다 속도 빠르고, 캐싱 가능

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "quick"
          }
        },
        {
          "match_phrase": {
            "message": "lazy dog"
          }
        }
      ]
    }
  }
}
```

-> `quick` 과 `lazy dog` 가 포함된 모든 문서 검색하는 쿼리
(must_not 이라면, 하나도 포함되지 않는 문서 검색)

![700](https://i.imgur.com/xrVl8wQ.png)

애매하게도

표준 SQL : AND, OR 은 2항 연산자
Search : must, must_not, should 는 각각 쿼리에 대해 참 or 거짓으로 적용하는 단항 연산자

### should

should 를 통해 가중치를 높일 수 있다.

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        }
      ],
      "should": [
        {
          "match": {
            "message": "lazy"
          }
        }
      ]
    }
  }
}
```

기존 fox 로 있던 결과와 lazy 가 포함된 결과는, 요소들의 score 가 달라져 순서가 달라지게 된다.

### filter

검색 조건의 참 / 거짓 여부만 판별한다. 추가로, score 에 가중치를 추가해준다.

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        },
        {
          "match": {
            "message": "quick"
          }
        }
      ]
    }
  }
}
```

quick 가 포함된 요소들이 점수가 올라간다.
-> 즉, 둘다 점수에 반영된다는 의미

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        }
      ],
      "filter": [
        {
          "match": {
            "message": "quick"
          }
        }
      ]
    }
  }
}
```

처음에 fox 만 검색한 것과 score 가 동일하게 나온다.
-> filter 는 검색에 조건은 추가하지만, 스코어에 영향을 주지 않을때 사용한다.

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "must": [
        {
          "match": {
            "message": "fox"
          }
        }
      ],
      "filter": [
        {
          "bool": {
            "must_not": [
              {
                "match": {
                  "message": "dog"
                }
              }
            ]
          }
        }
      ]
    }
  }
}
```

must 로 fox 를 검색한 후, must_not + filter 로 dog 제거

- dog 를 제외하는 must_not 쿼리는 filter 안에 있으므로 스코어에 영향을 주지 않는다

### keyword

문자열 데이터는 keyword 형식을 통해 정확한 검색이 가능하다.

```json
GET my_index/_search
{
  "query": {
    "bool": {
      "filter": [
        {
          "match": {
            "message.keyword": "Brown fox brown dog"
          }
        }
      ]
    }
  }
}
```

문자열, 공백, 대소문자 까지 정확히 일치하는 데이터만 결과로 리턴

- 정확값 일치 여부만 따지므로 스코어는 0.0
- 스코어는 계산하지 않으므로 keyword 값 검색할 때는 filter 구문안에 넣음

> filter 안에 검색 조건들은 스코어를 계산하지 않지만, 캐싱 되므로 쿼리가 더 가볍고 빠르게 실행
> 스코어 계산이 필요하지 않은 쿼리는 모두 filter 안에 넣어서 실행 - keyword, range 등

### range

숫자, 날짜 형식도 range 쿼리를 이용해서 검색이 가능하다.

range 는 `range : { <필드명>: { <파라미터>:<값> }}` 으로 입력하면 된다.
4가지 파라미터가 존재한다.

- gte : Greater-than or equal to, 이상
- gt : Greater-than, 초과
- lte : Less-than or equal to, 이하
- lt : Less-than, 미만

```json
GET phones/_search
{
  "query": {
    "range": {
      "price": {
        "gte": 700,
        "lt": 900
      }
    }
  }
}
```

price 값이 700 이상 900 미만 데이터 검색

```json
GET phones/_search
{
  "query": {
    "range": {
      "date": {
        "gt": "2016-01-01"
      }
    }
  }
}
```

date 값이 2016-01-01 이후 데이터 검색

- 2016-01-01 or 2016-01-01T10:15:30 같은 `ISO8601` 형식


