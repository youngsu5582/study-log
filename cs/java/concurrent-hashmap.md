# Concurrent HashMap

기존 HashMap 은 여러 스레드가 동시에 접근하는 환경에서 적합하지 않다.
-> 이를 해결하기 위해 나온게 ConcurrentHashMap

## HashMap 특징 및 문제점

가장 기본적인 Map 구현체
빠른 데이터 삽입과 검색 속도 제공, 단일 스레드 환경에서 유용

```java
Map<String, String> map = new HashMap<>();
map.put("1","one");
map.put("2","two");
```

동기화가 되지않아 데이터 유실되거나, NPE 가 발생할 가능성이 있다.

## ConcurrentHashMap

동시성 문제를 해결하기 위해 등장한 Map 구현체
내부적으로 LOCK 사용해 데이터를 안전하게 보호, 성능 저하 최소화 하는 구조로 설계

- 스레드 안전성 : 여러 스레드가 동시 접근해도 데이터 무결성 유지
- 세그먼트 락 : 전체 Map 에 거는게 아닌, 데이터를 나누어 부분적 락 걸어 성능 저하 줄인다.
- 읽기-쓰기 분리 : 읽기 작업은 락을 사용하지 않아 빠르다.

```java
public V put(K key, V value) {
    int hash = key.hashCode();
    int index = (table.length() - 1) & hash;

    while (true) {
        Node<K, V> first = table.get(index);

        // CAS를 이용해 빈 슬롯에 새로운 노드를 삽입 시도
        if (first == null) {
            Node<K, V> newNode = new Node<>(hash, key, value, null);
            if (table.compareAndSet(index, null, newNode)) {
                return null;
            }
            // CAS 실패하면 다시 루프
            continue;
        } else {
            // 해당 버킷에 이미 노드가 존재하면 동기화 블록을 사용하여 안전하게 연결 리스트 업데이트
            synchronized (first) {
                Node<K, V> node = first;
                while (node != null) {
                    if (node.hash == hash && node.key.equals(key)) {
                        // 키가 이미 존재하면 값 업데이트
                        V oldValue = node.value;
                        node.value = value;
                        return oldValue;
                    }
                    if (node.next == null) {
                        break;
                    }
                    node = node.next;
                }
                // 새 노드를 리스트의 끝에 추가
                node.next = new Node<>(hash, key, value, null);
                return null;
            }
        }
    }
}
```

처음에 CAS 연산을 통해 값을 가져온다.

현재 값이 expectedValue 와 같다면, newValue 로 변경한다.
CPU 명령어 수준에서 수행되므로, 다른 스레드에 의해 값이 동시 수정되더라도 안전하게 동작한다.

- 락을 사용하지 않으므로 락 경합 줄여 고성능 동시성 제어 가능
- 스레드 안전성을 보장하며 상대적으로 간단한 알고리즘 구현

`ABA`문제 : 값이 A -> B 로 바뀌고, 다시 A로 바뀌었을 때 발생할 문제가 존재한다.

자바에서는 해당 값을 Native Code 로 처리한다.


- ConcurrentHashMap : `transient volatile Node<K,V>[] table;`
- HashMap : `transient Node<K,V>[] table;`

`transient`를 선언하면 객체 직렬화 할때 해당 필드를 제외한다.
-> 바이트 스트림으로 변환할 때 포함 X
-> 직렬화 불필요한 필드, 직렬화 할 수 없는 객체 포함
( 여러 스레드가 동시 접근에 의해 변화 및 일관성 문제, 직렬화는 비용이 발생할 수 있다. )

`volatile`를 선언하면 다중 스레드 환경에서 공유 변수의 메모리 가시성을 보장해준다.
-> 한 스레드에서 해당 변수를 수정하면, 다른 스레드가 즉시 그 변경된 값 볼 수 있도록 해준다.

> 스레드는 성능 향상을 위해 메인 메모리 데이터를 자신의 로컬 캐시에 저장해 놓고 사용한다. - 로컬 캐시
> 다른 스레드가 변수 A값 변경하더라도, 이미 로컬 캐시 저장된 스레드는 메인 메모리 대신 캐시 값 사용 - 값 변경 지연

https://blog.naver.com/jjoommnn/130037479493
자그마치, 2008년도의 글

- 로컬 캐시와 레지스터 사용:
  현대 CPU는 성능 향상을 위해 각 코어마다 독립적인 캐시와 레지스터를 사용
  한 스레드가 특정 변수의 값을 변경하면, 이 변경 사항이 바로 메인 메모리에 기록되지 않고 해당 코어의 캐시나 레지스터에 남아 있을 수 있다.

- 컴파일러 최적화:
  컴파일러는 실행 속도를 높이기 위해 코드의 순서를 재배치(reordering)하거나, 캐시 사용을 극대화하는 최적화를 수행
  이로 인해 한 스레드에서 변경한 값이 다른 스레드에 즉시 보이지 않을 수 있다.

- 문제 발생:
  이러한 이유로, 다중 코어 시스템에서 한 스레드가 변경한 값이 다른 스레드에서 즉각 반영되지 않아 동기화 문제가 발생
  여러 스레드가 동일한 공유 변수의 최신 상태를 읽지 못한다.

=> 메모리 배리어가 필요한 이유이다.

- 메인 메모리와 동기화 : 모든 메모리 연산을 강제로 메인 메모리에 반영
- 연산 순서 보장 : 이전 모든 메모리 연산이 완료되어야, 다음 연산 실행하도록 강제
  ( 컴파일러나 CPU가 재배치 할 때, 메모리 장벽 넘어 재배치하지 않도록 막는다. )
  -> 코드 명시한 순서대로 메모리 연산 수행되도록 보장


