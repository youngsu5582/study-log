# Lombok & Hibernate

Lombok 은 사실상 자바 개발자들에게 필연적인 존재이다.

생성자, Getter, Setter, Builder 등등 이 모든걸 우리가 직접 만들순 없다...

그렇기에 DTO, 컨트롤러 등 다양한 곳에서 사용하지만
엔티티에선 주의깊게 사용해야 한다.

```java
@Entity
@EqualsAndHashCode
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String customer;

    @OneToMany(mappedBy = "order")
    private Set<OrderPosition> positions = new HashSet<>();
    ...
}
```

이와 같을 때 Best Practice 는
equals 는 객체 타입의 기본 키(pk) 만 비교, hashCode 는 42 와 같은 고정값을 반환
추가로, 둘 중 하나라도 기본 키가 null 이면 equals 는 false 를 반환하게 하는 것이다.

## EqualsAndHashCode 를 사용하지 마라

이를 직접 구현하는게 귀찮아서 Lombok 의 EqualsAndHashCode 를 사용하면 무슨 일이 일어날까? 
Lombok 이 생성하는 EqualsAndHashCode 는 두 메소드의 non-final 속성들을 포함한다.

```java
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String customer;

    @OneToMany(mappedBy = "order")
    private Set<OrderPosition> positions = new HashSet<>();

    ...

    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Order)) return false;
        final Order other = (Order) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$customer = this.getCustomer();
        final Object other$customer = other.getCustomer();
        if (this$customer == null ? other$customer != null : !this$customer.equals(other$customer)) return false;
        final Object this$positions = this.getPositions();
        final Object other$positions = other.getPositions();
        if (this$positions == null ? other$positions != null : !this$positions.equals(other$positions)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) { return other instanceof Order; }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $customer = this.getCustomer();
        result = result * PRIME + ($customer == null ? 43 : $customer.hashCode());
        final Object $positions = this.getPositions();
        result = result * PRIME + ($positions == null ? 43 : $positions.hashCode());
        return result;
    }
}
```

일단 원하는 코드가 아니니?

```java
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @EqualsAndHashCode.Include
    private Long id;
    ...
}
```

onlyExplicitlyIncluded + Include 를 사용하면 지정한 요소만 사용해 equals, hashCode 를 생성한다.

```java
 @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Order)) return false;
        final Order other = (Order) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) { return other instanceof Order; }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        return result;
    }
```

이러면 해결이 된걸까? 아니다.

두 엔티티의 기본 키가 null 이면 equals 는 false 를 반환해야 하는데 true 를 반환한다.

Set 에 적절히 동일한 두개를 넣어야 한다면? 예외가 발생한다.
왠만하면, 직접 구현하자. 위에서 말한 best-practice 나 UUID 가 있다면 uuid 를 통해 구현하는 방식으로

> 왜 UUID 가 따로 있는게 좋냐면
> 저장이 되기 전 ID 가 null 인데 DB 에 저장되고 ID 가 채워지면 다른 것인가..?
> 관점에 따라 다르겠지만 UUID 는 애플리케이션 단에서 관리 + 변하지 않으므로 매우 명확하게 동등을 보장해준다.

## ToString 을 조심해라

Lombok 의 ToString 도 equals 와 유사하게 모든 요소들을 사용한다.
이때, JPA 가 지연 로딩을 사용하는 엔티티가 있다면 DB 에서 가져오기 위해 추가 쿼리가 발생할 수 있다.

```java
@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String customer;

    @OneToMany(mappedBy = "order")
    private Set<OrderPosition> positions = new HashSet<>();

    @Override
    public String toString() {
        return "Order(id=" + this.getId() + ", customer=" + this.getCustomer() + ", positions=" + this.getPositions() + ")";
    }
}
```

이를 피하기 위해선

```java
@Entity
@ToString
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String customer;

    @OneToMany(mappedBy = "order")
    @ToString.Exclude
    private Set<OrderPosition> positions = new HashSet<>();
}
```

Exclude 를 일일히 지정해줘야 한다.
즉, 가독성도 떨어지고 + 새로운 연관 추가할 때마다 성능 및 애플리케이션을 깨뜨릴 위험이 있다.
차라리 IDE 제공 toString 을 관리하는게 더 쉬울것이다.

## Data

Data 는 일종의 God Class 이다.

```java
/**
 * Generates getters for all fields, a useful toString method, and hashCode and equals implementations that check
 * all non-transient fields. Will also generate setters for all non-final fields, as well as a constructor
 * (except that no constructor will be generated if any explicitly written constructors already exist).
 * <p>
 * Equivalent to {@code @Getter @Setter @RequiredArgsConstructor @ToString @EqualsAndHashCode}.
 * <p>
 * Complete documentation is found at <a href="https://projectlombok.org/features/Data">the project lombok features page for &#64;Data</a>.
 *
 * @see Getter
 * @see Setter
 * @see RequiredArgsConstructor
 * @see ToString
 * @see EqualsAndHashCode
 * @see lombok.Value
 */
```

주석만 봐도 상당하다.
위에서 쓰지 말라고 한 ToString, EqualsAndHashCode 도 있고 getter, setter 등등도 있다.

왠만하면 DTO 에서만 사용하자.