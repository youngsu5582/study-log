
기존 Spring Data JPA 의 문제점을 해결하기 위해 나온것이다.

1. 문자열 쿼리 관리가 힘들다
   Type-Check 불가능, 휴먼 에러 ( 공백 누락, 리턴 타입 등등 ) -> 런타임에 발견된다!
2. 조건식 재활용 불가
   기존에서 `and` 문 하나만 추가되더러라도 새로운 쿼리를 작성해줘야 한다.
3. 더러운 동적 쿼리
   직접 `:endDate` 와 같이 값을 주입해야 하거나 `is null` 같은 요소들을 인지해야 한다.

이 3가지 전부를 가능하게 해준다.

쿼리를 타입 안전하게 작성해주는 프레임워크

- 메소드 체이닝 -> 가독성 ( `+` 단순, 간결 )
- 메소드 분리 통해 재사용성 향상
- 메소드 네이밍으로 쿼리 조건, 정렬 방식등을 유추 가능하다.

컴파일 타임에 `@Entity` 를 추적해 엔티티 정보를 가진 `QClass` 를 만든다.

### 왜 타입 안전한가?

```java
public final NumberPath<Long> id = createNumber("id", Long.class);    
public final StringPath title = createString("title");
```

Path 로 만들어진다.
( 각 NumberExpression, StringExpression 을 extends 하고, Expression 을 implement 한다. )

id.concat("문자연산") 와 같은 타입 불일치 문제를 컴파일 타임에 잡아낼 수 있다.
( `qMockupTemplate.id.eq(mockupTemplateId)` 조회문에서도 타입이 다른걸 잡아낸다. )

### 어떻게 조건식을 재사용 할 수 있는가?

기존 JPQL 은 조건식 표현 영역과 JPQL 생성 및 실행 영역이 하나의 메소드에 통합되어 있다.

QueryDSL 에서는

- 엔티티 조건식 표현 영역은 Expression
- JPQL 생성 및 실행 영역은 JpaQueryFactory 가 담당한다.

where 절에 여러 BooleanExpression 을 넣어서 사용하면 된다.

> 이때, 실수로 똑같은 조건 넣어도 잘 동작한다.

null 이라면 자동으로 무시한다.
1, 2, 3 이 있는데 1이 null 이라면? -> 2 and 3으로 처리

---

## 잘 사용하기

1. QueryDSL 통해 쿼리를 만들 메소드 + 인터페이스를 선언하고
2. 인터페이스를 구현 - `xxxxImpl`
3. JPA Repository 에 QueryDSL Interface 를 `extends`


- select + from 에 들어가는 QClass 가 같다면 selectFrom 을 사용하자

#### 결과 사용하기

- fetchOne : return Store
- fetch : return List`<Store>`
- fetchFirst : return limit(1).fetchOne()

연관관계가 없는 테이블간 조인은 `카테시안 곱`이 발생할 수 있으므로 주의해야 한다.
```
.join(order, review)
.where(order.review.eq(review.content))
```

> 해당 부분은 내가 직접해서 확인해봐야 할듯 (?)

`groupBy, orderBy` 로 집계함수를 사용 가능하다.

### 서브 쿼리

JPAQuery, JPAExpressions 를 제공해준다.

JPAExpressions 가 유틸성 클래스이므로 이를 사용하는게 더 좋음
( 사용 범위가 서브 쿼리에 맞쳐져 있음 + 공식 문서에서 이로 작성 )

가게 상관없이 계산된 전체 리뷰 평점보다
특정 가게의 모든 주문의 리뷰 평점이 높은 가게만 골라서 평점 순으로 조회 해주세요!

```java
final QReview notJoinedReview = new QReview("review2");

// goe : greaterOrEqual
.having(review.rate.avg().goe(
	JPAExpressions
	.select(notJoinedReview.rate.avg())
	.from(notJoinedReview)
	))
)
```

생각보다 간결하게 표현

하지만 더 깔끔하게도 역시 가능하다.
-> 메소드로 분리!

```java
.having(review.rate.avg().goe(
	JPAExpressions
	.select(reviewforSelectAllavg.rate.avg())
	.from(reviewforSelectAllavg)
	))
)

//
.having(revie.rate.avg().goe(calculateTotalRateAvg()))
```

이때 주의할 점은 새로운 QClass 를 사용한걸 봐야한다.

별칭을 새롭게 지정하지 않으면, 동일한 review 에 대해 같거나 큼을 비교한다.

### 동적 정렬

OrderSpecifier 를 만들어서 내가 사용 가능하다.

![[Pasted image 20250410184950.png]]

복잡한 동적 정렬도 비교적 간단한 자바 코드로 해결 가능



## 주의점

- 1차 캐시 장점을 누릴 수 없다.

QueryDSL 의 결과는 JPQL이다.
-> DB에 쿼리를 무조건 보낸다.

DB에서 조회한 값에 대해 1차 캐시가 있으면, DB 결과를 버리고 1차 캐시를 가져온다.

- QueryDSL 의 마지막 업데이트는 5.1이다.
  ( 2021년 7월에 5.0 이고, 2024년 1월에 5.1로 범프 위주 업데이트가 됐다. )
  -> OpenFeign 티의


---

권남님이 정리한 블로그 내용
https://kwonnam.pe.kr/wiki/java/querydsl#%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8_%EC%9D%B4%EA%B4%80

QueryDSL Tutorial

https://examples.javacodegeeks.com/java-development/enterprise-java/spring/spring-querydsl-tutorial/
