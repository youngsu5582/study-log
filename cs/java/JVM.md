# JVM

> 학습 링크 : https://www.youtube.com/watch?v=BOctj2RxaNc

유저 모드에 있는 가상의 VM ( Virtual Machine  )

- 어떤 OS 에서든 자바코드가 동일하게 실행을 하게 해준다.

## 자바 컴파일 과정 

1. Java Source Code (.java)
2. Compiler (javac) 통해 Bytecode(.class) 로 변환
3. ClassLoader 가 JVM 메모리에 로드
4. Execution Engine 가 .class 바이트코드를 실제 기계어로 변환해 사용
5. 프로그램 실행

## JVM 메모리 구조

`Method Area`, `Heap`, `JVM Stack`, `PC Register`, `Native Method Stack`

### Method Area

- 클래스 정보 : class 파일의 클래스명, 부모 클래스, 필드 정보,메소드 정보
- static 변수
- JIT 코드
- 상수 풀 : 상수(final static), 문자열 리터럴 저장

### Heap

GC 의 대상, `new` 키워드를 통해 생성된게 주요 대상

- 객체 인스턴스
- 배열
- JVM 에서 동적으로 생성한 객체 (리플렉션,프록시 객체)

### JVM Stack

각 스레드마다 생성

- 메소드 호출 정보
- 지역 변수 (Primitive 타입,참조변수)
- 메소드 실행 중간 결과값

```java
public static void main(String[] args) {
    int result = add(3, 5);  // Stack Frame 생성
}

static int add(int a, int b) {
    int sum = a + b;  // 지역 변수 저장
    return sum;
}
```

1. main 메소드 호출 - Stack 에 `main()` Frame 생성
2. `add(3,5)` 메소드 호출 - Stack 에 `add()` Frame 생성
3. `sum = a + b` 연산 수행 - Stack 에 `sum` 저장
4. `add()` 종료 - Stack 에 `add()` Frame 제거
5. `main()` 종료 - Stack 에 `main()` Frame 제거

#### Stack Frame

JVM Stack 내부에서 각 메소드의 실행 정보를 저장하는 단위

- 메소드 호출마다 새로운 스택 프레임이 생성
- 메소드가 종료되면 스택 프레임 제거

- Local Variables : primitive 값은 프레임에 직접 저장, Reference 저장
- Operand Stack : 메소드 내 계산 위한 공간 ( `a + b` 와 같은 )
- Constant Pool : 상수 풀의 Reference 저장 ( `a=4000000;` 일 시, `4000000` 의 주소를 저장 )

### PC Register

현재 실행 중인 JVM 명령어 추적
( CPU 의 Program Counter 와 동일 )
명령어 실행마다 자동으로 업데이트된다.

- 각 스레드마다 PC Register 를 가진다 ( 여러 스레드가 동작해도, 추적 가능 )
- 점프 명령어(`if`,`for`,`while`) 실행 시, 명령어 흐름을 제어
- 메소드 호출 시, 새로운 메소드의 첫 번째 명령어로 이동 + 종료 후 원래 위치 복귀

### Native Method Stack

JVM 내부에서 네이티브 코드 ( C,C++ ) 실행할 때 사용되는 스택
JVM 위에서 동작하므로, 네이티브 코드 통해 운영체제와 하드웨어랑 소통한다

- 각 스레드마다 Native Method Stack 을 가진다
- `System.gc()`,`Socket()` 등 사용시 호출 (`OS API`)
