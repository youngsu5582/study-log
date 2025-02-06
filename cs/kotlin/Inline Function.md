```kotlin
fun doSomethingElse(lambda: () -> Unit) {
    println("Doing something else")
    lambda()
}
```

위 코틀린 함수가

```java
public static final void doSomethingElse(Function0 lambda) {
    System.out.println("Doing something else");
    lambda.invoke();
}
```

이와같은 자바 함수로 변환된다.

`invoke` 라는 코틀린 표준 라이브러리는 메소드 시그니처 따라(변수 개수 22) 따라 인터페이스 생성되어있고,
람다식을 구현해 해당 객체를 파라미터로 사용한다.

```kotlin
fun doSomething() {
    println("Before lambda")
    
    doSomethingElse {
        println("Inside lambda")
    }
    
    println("After lambda")
}
```

해당 코드는?

```java
public static final void doSomething() {
    System.out.println("Before lambda");
    
    doSomethingElse(new Function() {
            public final void invoke() {
            System.out.println("Inside lambda");
        }
    });
    
    System.out.println("After lambda");
}
```

이와 같이 익명 클래스로 만들어진다.
부가적인 메모리 할당으로 메모리 효율이 안 좋아지고, 함수 호출로 런타임 오버헤드가 발생한다.

```kotlin
inline fun doSomethingElse(lambda: () -> Unit) {
   println("Doing something else")
   lambda()
}
```

인라인으로 선언하면?

```kotlin
public static final void doSomething() {
    System.out.println("Before lambda");
    System.out.println("Doing something else");
    System.out.println("Inside lambda");
    System.out.println("After lambda");
}
```

새로운 객체 생성이 아닌 내장 코드로 변환된다.

---

- 기본적으로 JVM 의 JIT 컴파일러에 의해 inline 함수가 더 좋다고 생각하면 JVM 이 자동으로 만들어준다.
- 호출 지점마다 함수 본문이 삽입되므로, `함수가 길거나` `여러번 호출되면` 바이트코드 크기가 커진다.
( 메소드 크기가 커져 JIT 최적화에도 부정적인 영향 미침 )
- 짧고 자주 호출되는 함수, 람다를 인자로 받는 함수, reified 타입이 필요한 함수에 사용하는 것이 좋다.
- `nonline` 키워드로 인라인 대상에서 제외할 수 있다.
