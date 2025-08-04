# QueryDSL With Array

PostgreSQL 은 기본적으로 ARRAY 를 제공해준다.
그리고, Hibernate 6부터는 똑똑하게 타입을 지원해준다.

```java
@JdbcTypeCode(SqlTypes.ARRAY)
@Column(
        name = "translate_result_list",
        columnDefinition = "bigint[]"
)
private List<Long> translateResultList;
```

- JdbcTypeCode : Hibernate 어노테이션 ( 표준 JPA 명세 포함 기능 X ), 어떤 JDBC 타입 코드를 사용해야 할 지 명시적으로 알려줌
- SqlTypes : SQL 데이터 타입을 나타내는 정수 상수의 집합 - VARCHAR 은 12, INTEGER 는 4, ARRAY 는 2003 등등

그러면 QueryDSL 에서도 사용할 수 있겠네? 라고 생각할 수 있다.

하지만, 절대 불가능하다... 

```java
public List<Post> findByTag(String tag) {
    return queryFactory
        .selectFrom(post)
        .where(post.tags.contains(tag))
        .fetch();
}
```

List 로 인식되어서 QueryDSL 에서 타입으로 contains, any 등등 배열 전용 함수들을 제공해준다.

사용을 하면
`Caused by: org.hibernate.query.SemanticException: Operand of 'is empty' operator must be a plural path [사용 쿼리...]`
라는 에러가 발생한다.

쉽게 말해서 타입은 List 이지만, QueryDSL(JPA) 가 배열로 인식하지 못해서 발생하는 문제다.

JPA 는 `@ElementCollection` 이나 `@CollectionTable` 을 통해서 선언된 List or Array 만 배열로 인식한다.

그러면, 우리가 원하는대로 배열로 깔끔하게 사용할 순 없는걸까...?
( DB가 배열을 제공해주겠다는데... )

## with View

View 를 선언해서 배열 요소들을 단일 컬럼들로 의도적으로 인식을 하게 할 수 있다.

```sql
CREATE TABLE entity (
  id serial PRIMARY KEY,
  translate_result_list bigint[]
);

-- 배열을 한 행당 한 요소로 풀어 주는 뷰
CREATE VIEW entity_translate_view AS
SELECT
  t.id,
  elem            AS translate_id
FROM
  text_to_image t,
  unnest(t.translate_result_list) AS elem;
```

PostgreSQL 의 `unnest` 함수를 사용해서 배열을 풀어주는 뷰를 선언한다.
그 후, QueryDSL 에서 이 뷰를 사용하면 된다.

```java
public List<EntityTranslateView> findByTranslateId(Long translateId) {
    return queryFactory
        .selectFrom(entityTranslateView)
        .where(entityTranslateView.translateId.eq(translateId))
        .fetch();
}
```

이렇게 하면, QueryDSL 에서 배열을 단일 컬럼으로 인식해서 사용할 수 있다.

하지만, 문제가 있다.

매우 느리다는 것이다..
왜냐하면, 배열을 풀어주는 로직이 들어가고, VIEW 의 특성상 매번 쿼리를 실행하기 때문이다.
( 뷰는 기본적으로 SELECT 쿼리로 동작한다. )

그리고, 인덱스를 탈 수가 없다.

> GNI 인덱스라는게 있다고 하나, 실제 적용이 되지 않았다.
> - GNI Index : Generalized Inverted Index, PostgreSQL 15 부터 제공되는 인덱스, 일종의 역인덱스
> ( 차후, 더 학습해볼 에정 )

View 적용 전

![](https://i.imgur.com/OGY3Zyp.png)


View 적용 후

![](https://i.imgur.com/i2nbsLM.png)

그렇기에 해당 방법도 사용할 수 없게 되었다..

## Query 분리

QueryDSL 을 통해 명확하게 해결할 수 있는 방법이 없다!
그러면...?

```java
List<Long> entityIds = resultList.stream().map(Entity::getId).toList();

// id, translateResult ( 단일 칼럼 ), translateResultList ( 다중 칼럼 ) 조회
List<Tuple> tuples = jpaQueryFactory
        .select(entity.id, entity.translateResult, entity.translateResultList)
        .from(entity)
        .where(entity.id.in(entityIds))
        .fetch();
```

필요한 요소들을 직접 Tuple 로 가져오고

```java
Map<Long, List<Long>> entityToTranslateIdsMap = new HashMap<>();

for (Tuple tuple : tuples) {
    Long id = tuple.get(entity.id);
    var translateResult = tuple.get(entity.translateResult);
    var translateResultList = tuple.get(entity.translateResultList);
    List<Long> translateIdList;

    // 배열이 없는 경우 ( 이전 데이터 )
    if (CollectionUtils.isEmpty(translateResultList) && translateResult != null) {
        translateIdList = Collections.singletonList(translateResult);
    } else {
        // 배열이 있는 경우 ( 현재 데이터 )
        translateIdList = (translateResultList != null) ?
                translateResultList : Collections.emptyList();
    }
    textImageToTranslateIdsMap.put(id, translateIdList);
}
return textImageToTranslateIdsMap;
```

Entity 의 ID 를 기준으로 List<Long> ( TranslateResult 의 ID 들 ) Map 을 만들어서 반환한다.

```java
// 조회할 translateText ID 목록 생성
Set<Long> allTranslateIds = entityToTranslateIdsMap.values().stream()
        .flatMap(List::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
```
ID 들을 모아서

```java
Map<Long, TranslateTextDto> translateTextMapById = jpaQueryFactory
        .selectFrom(translateText)
        .where(translateText.id.in(allTranslateIds))
        .fetch()
        .stream()
        .collect(Collectors.toMap(
                TranslateText::getId,
                tt -> new TranslateTextDto(tt.getId(), tt.getSrc(), tt.getDest())
        ));
```

직접 조회를 해서 Map 으로 만들어준다.

```java
resultList.forEach(dto -> {
            List<Long> translateIds = entityToTranslateIdsMap.getOrDefault(dto.getId(), Collections.emptyList());
            List<TranslateTextDto> translateList = translateIds.stream()
                    .map(translateTextMapById::get)
                    .toList();
            dto.setTranslateTextList(translateList);
        });
```

마지막으로, Entity 에서 가져온 ID 를 기준으로 TranslateTextDto 를 조회해서 List 로 만들어준다.

이렇게 하면, QueryDSL 에서 배열을 직접적으로 다루는 것보다 훨씬 더 효율적으로 데이터를 가져올 수 있다.

두 번의 추가적인 쿼리가 발생하긴 하나,
ID 를 기준으로 IN 절을 하므로, 인덱스를 탄다. ( 엄청 칼럼이 많아진다면 당연히 한계가 올 수도 있지만, 이 경우는 아니다. )

그리고, 애초에 번역 결과도 배열이 엄청 많은게 아니므로 애플리케이션 단 JOIN 을 하더라도 성능상 큰 문제는 없다.

쿼리를 분리한 후 결과

![](https://i.imgur.com/7Bl2jLy.png)

## 결론

항상 기억하자.

한방 쿼리, 한번에 JOIN 쿼리가 항상 좋은 것은 아니다.
QueryDSL 이 제공하는 기능이 많다고 해서, 무조건 사용하지 말고 적절하게 끊어내자. 
( 인덱스를 사용할 수 있게, 연산을 효율적이게 )