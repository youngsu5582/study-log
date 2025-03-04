# Functional Programming

순수 함수와 불변성을 중심으로 하는 프로그래밍 패러다임
함수를 일급 객체로 취급하며, 상태 변경하지 않고 선언적으로 코드 작성

- 순수 함수 : 같은 입력이 주어지면, 항상 같은 출력 반환

```kotlin
fun add(a: Int, b: Int): Int = a + b
```

무조건 같은 결과를 반환한다.

- 불변성 : 상태를 변경하지 않고, 기존 값 변경이 아닌 매번 새로운 값 반환

```kotlin
val list = listOf(1, 2, 3)
val newList = list.map { it * 2 }

```

- 일급 시민 함수 : 함수를 변수에 저장하거나, 함수의 인자로 전달 가능

```kotlin
val square: (Int) -> Int = { it * it }
fun applyFunction(x: Int, func: (Int) -> Int): Int = func(x)
println(applyFunction(4, square))
```

- 고차 함수 : 함수를 매개변수로 받거나 반환하는 함수

```kotlin
fun operation(x: Int, y: Int, op: (Int, Int) -> Int): Int {
    return op(x, y)
}
val sum = operation(5, 3) { a, b -> a + b }
```

### 장점

- 병렬 처리에 유리 -> 불변성으로 인한 공유 상태가 없어 동시성 문제 해결
- 디버깅과 테스트 용이 -> 순수 함수는 항상 같은 입력 대해 항상 같은 결과 반환하므로

### 단점

- 초기 학습 비용 있음 -> 객체지향이랑 패러다임이 다름
- 퍼포먼스 이슈 -> 불변성을 유지하기 위한 새로운 객체 생성에 비용 발생

### VS OOP

객체지향은 객체를 중심으로 캡슐화하며 상태 변경하며 동작
FP는 함수 중심으로 불변성 유지하며 선언적 동작
=> OOP 는 데이터 + 행동 묶어 모델링, FP는 데이터 변경하지 않고 변환하는 방식
