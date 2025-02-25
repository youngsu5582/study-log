# JPA TIP

## OneToMany Collection 에서 함부로 remove 를 하지 말것

```java
List<Item> items = owner.getItems();
items.remove(index);
```

이와같이 `stream - filter` 또는 조건에 맞는 요소들 제거하면
DB에서도 삭제 반영이 된다.

=> 매우 조심 또는 Entity 영속 관계를 끊고 나서 사용해야 한다.

## IDENTITY 전략은 BATCH INSERT ( saveAll ) 이 의미가 없다 in MYSQL

데이터베이스 IDENTITY 컬럼을 사용해 기본 키를 생성한다.
-> 죽, DB 자체에서 자동으로 고유한 값 생성하는 기능

- MySQL 은 AUTO_INCREMENT

-> DB를 통해야만 ID 값을 알 수 있다.

saveAll 은 사실, for 구문을 돌리는게 다이다.

```java
for (S entity : entities) {
    result.add(save(entity));
}
```

>For IDENTITY columns, the only way to know the identifier value is to execute the SQL INSERT. Hence, the INSERT is executed when the persist method is called and cannot be disabled until flush time.

> For this reason, Hibernate disables JDBC batch inserts for entities using the IDENTITY generator strategy.

JPA 영속 컨텍스트는 엔티티를 식별할때 엔티티 타입과 엔티티의 id 값으로 엔티티를 식별
하지만, IDENTITY 엔티티는 insert 문을 실행해야만 id 값을 확인 가능하기 때문에 batch insert 를 비활성화

```yml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 100
```

`rewriteBatchedStatements=true` 조건과 SEQUENCE 가 필요하다.

## xxxRepository.flush 는 모든 EntityManager 를 flush 한다.

```java
@Transactional
public <S extends T> S saveAndFlush(S entity) {
    S result = save(entity);
    flush();
    return result;
}

public void flush() {
    entityManager.flush();
}
```

EntityManager 를 통한 flush 이므로, 모두 반영된다.

