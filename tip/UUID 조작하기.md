# UUID 버전 추출 및 조작하기 (Bitwise Operation)

자극적인 제목이지만 매우 간단한 내용이다. 🥲

## UUID란?

UUID(Universally Unique Identifier)는 전 세계적으로 고유한 식별자를 만들기 위한 128비트(16바이트) 길이의 표준 규약이다. 
중앙 관리 시스템 없이도 각 시스템이 독립적으로 식별자를 생성할 수 있고, 생성된 식별자가 서로 중복될 확률이 무시할 수 있을 정도로 낮다는 특징이 있다.

> 매 초 10억개의 uuid를 100년에 걸쳐서 생성할 때 단 하나의 uuid가 중복될 확률은 50%이다.

믿거나 말거나

이러한 특성 덕분에 분산 시스템 환경에서 데이터베이스의 기본 키(Primary Key), 트랜잭션 ID, 파일 이름 등 고유성이 반드시 보장되어야 하는 다양한 곳에서 널리 사용한다.

## UUID의 구조

UUID는 32개의 16진수 문자와 4개의 하이픈(-)으로 구성된 `8-4-4-4-12` 형태로 표현된다.

`xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx`

- **M (버전)**: UUID가 어떤 방식으로 생성되었는지를 나타내는 4비트 숫자. (예: `1`, `4`, `7` 등)
- **N (변형)**: UUID의 레이아웃 형식을 나타내는 1~3비트 정보입니다. 대부분의 현대 UUID는 `8`, `9`, `a`, `b` 중 하나로 시작

### UUID의 주요 버전

- **Version 1 (v1)**: **타임스탬프 + MAC 주소 기반**. 생성된 시간과 해당 컴퓨터의 고유한 MAC 주소를 결합하여 만듬. 시간 순으로 정렬이 가능하지만, MAC 주소가 노출될 수 있음
- **Version 3 (v3)**: **네임스페이스 기반 (MD5 해시)**. 특정 이름(예: URL, DNS 등)을 MD5로 해시하여 생성합니다. 동일한 이름은 항상 동일한 UUID 생성
- **Version 4 (v4)**: **순수 난수 기반**. 버전과 변형 비트를 제외한 모든 비트를 무작위로 생성합니다. 가장 널리 사용되는 방식이지만, 완전한 무작위성이므로 시간 순 정렬 불가 & 데이터베이스 인덱싱에 불리
- **Version 5 (v5)**: **네임스페이스 기반 (SHA-1 해시)**. v3와 동일하지만 더 안전한 SHA-1 해시 알고리즘을 사용
- **Version 7 (v7)**: **타임스탬프 + 난수 기반**. v1의 장점(시간순 정렬)과 v4의 장점(개인정보보호)을 결합한 최신 규격. 앞부분은 유닉스 타임스탬프로 채워지고 뒷부분은 난수로 채워져, 시간순 정렬이 가능하면서도 MAC 주소 같은 민감한 정보 포함 X

즉, 버전은 엄격하게 제공이 된다.

```java
UUID uuidV4 = UUID.randomUUID(); // 표준 라이브러리로 v4 생성
UUID uuidV7 = UUID.fromString("0198371b-213a-7b0e-a7fd-efded06c4c80"); // v7 문자열로 생성

// 내장 version() 메소드 사용
int version4 = uuidV4.version(); // 4
int version7 = uuidV7.version(); // 7
```

`version()` 메소드는 내부적으로 비트 연산을 통해 버전을 알아낸다. UUID는 2개의 64비트 `long` 값으로 구성되며 버전 정보는 상위 64비트(`mostSignificantBits`)에 저장된다.

- **추출 로직**: `(mostSigBits >> 12) & 0x0f`
- 
1. `mostSigBits >> 12`: 버전 정보가 담긴 4비트를 가장 오른쪽으로 이동
2. `& 0x0f`: `0x0f`는 이진수로 `1111`입니다. AND 연산을 통해 오른쪽 4비트(버전 정보)만 남기고 나머지는 모두 0으로 만듬

```java
long mostSigBits = uuid.getMostSignificantBits();
int version = (int) ((mostSigBits >> 12) & 0x0f); // 핵심 로직
```

상위 비트 -> 상위 비트 12칸 이동 ( 16칸 중 4칸 ) -> 1111 AND 연산 통해 일치한 값 추출

## UUID 버전 조작하기

버전 추출의 역순으로 비트 연산을 적용하면 당연히 버전도 조작 가능하다.

- **조작 로직**:
1. **기존 버전 정보 지우기**: 특정 위치의 버전 비트들만 `0`으로 만듬
2. **새로운 버전 정보 씌우기**: 해당 위치에 원하는 버전의 비트 값을 `1`로 설정

### 2-1. UUID 버전 변경 유틸리티 (`UUIDUtil.java`)

다음은 UUID 버전을 안전하게 변경하는 유틸리티 클래스이다.

```java
    /**
 * UUID의 버전을 지정된 버전으로 변경합니다.
 * 이 메소드는 비트 연산 학습 및 특수 목적을 위한 것이며,
 * 생성된 UUID는 표준 규격을 따르지 않을 수 있으므로 주의해야 합니다.
 *
 * @param uuid    원본 UUID
 * @param version 변경할 버전 (1-7 사이의 값)
 * @return 버전이 변경된 새로운 UUID 객체
 */
public static UUID changeVersion(UUID uuid, int version) {
    if (version < 1 || version > 7) {
        throw new IllegalArgumentException("Version must be between 1 and 7.");
    }

    if (uuid.version() == version) {
        return uuid;
    }

    long mostSigBits = uuid.getMostSignificantBits();

    // 1. 기존 버전 비트를 0으로 초기화합니다.
    //    마스크: 0x000000000000F000L (버전 비트 위치만 1)
    //    ~마스크: 버전 비트 위치만 0이고 나머지는 1인 마스크
    long clearedVersionBits = mostSigBits & ~0x000000000000F000L;

    // 2. 새로운 버전 값을 해당 위치에 설정합니다.
    //    (long)version << 12 : 버전 숫자를 왼쪽으로 12비트 쉬프트하여 위치를 맞춥니다.
    long newVersionBits = (long) version << 12;

    // 3. 초기화된 비트와 새로운 버전 비트를 OR 연산하여 합칩니다.
    long newMostSigBits = clearedVersionBits | newVersionBits;

    return new UUID(newMostSigBits, uuid.getLeastSignificantBits());
}
```

그러면, 이런걸 언제 사용할 수 있을까?

폴링 로직이 있다고 가정해보자. 근데 이 폴링 로직은 매우 방어적으로 작성되어있다.

```java
List<TaskResultDto> resultList = new ArrayList<>();

for (int i = 0; i < uuidList.size(); i++) {
    for (ResultDto resultDto : result.getList()) {
        if (Objects.equals(resultDto.getUuid(), uuidList.get(i))) {
            TaskResultDto taskResultDto = pollingDto.getTaskList().get(0).getResult();
            if (resultDto != null) {
                resultList.add(resultDto);
            }

            break;
        }
    }
}
```

resultDto - UUID 가 일치할 때만 값을 추가하는 로직이다.
즉, UUID 가 일치하지 않으면, 결과를 추가하지 않는다. ( 결과에 UUID 가 1개 )

여기에 두 가지 요구사항이 있다.

- 기존, 생성 데이터 1개 : UUID 1개 구조를 변경하라 ( -> 생성 데이터 N개 : UUID 1개 구조 )
- 하지만, 기존 폴링 로직은 건들지 못하고 ( 위 코드 수정 불가능 ) 폴링을 응답해주는 로직만 변경하라 ( 하위 호환성 )

이때, UUID 버전을 조작해서 해결할 수 있다.

왜 UUID 를 조작해야 하나?

우선, 가짜 UUID 를 만들어야 하는데 ( 위 if 문 로직을 통과시키기 위해 )
가짜 UUID 를 만들다가 혹시나 UUID 가 중복된다면? 문제가 된다.
( 다른 사용자의 결과가 조회된다거나, 이상한 데이터가 포함된다거나... )

그리고, 한개의 실제 UUID 역시도 중복이 되면 안되는데
이를 굳이 DB 에 `fineByUUID` 와 같이 조회를 해서 해결 하는것도 비효율적이다.

우리가 V4 로 UUID 를 생성해서 사용하고 있다고 가정하면, V1,V6,V7 등은 절대 중복되지 않는다. ( V4 는 V1,V6,V7 등과 중복될 수 없음 )

> 즉, 가짜 UUID 를 만들어서 넘겨주고 진짜 UUID 1개를 통해 조회하는 것

로직의 변경 흐름은

1. 폴링할 UUID 생성해주는 로직 : UUID 한개를 통해 생성 데이터 생성 후, 가짜 UUID 를 통해 생성 데이터 1개 당 UUID 1개인 것 처럼 구조 변경

```java
List<ResultDto> list = new ArrayList<>();

var savedUuid = dto.getUuid();

for (var task : dto.getTaskList()) {
    var copy = dto.clone();
    copy.setUuid(UUID.randomUUID());
    copy.setTaskList(List.of(task));
    list.add(copy);
}

list.getFirst().setUuid(savedUuid);
return list;
```
(간단한 슈도 코드)

2. 폴링 로직 : UUID 의 버전을 확인한다.

```java
var prevUUID = uuidList.stream().filter(uuid -> uuid.version() != 4).findFirst();
if (prevUUID.isPresent()) {
    var uuid = prevUUID.get();
    var result = service.polling(List.of(uuid));
}
```
4 버전이 아니라면, 우리가 생성한 가짜 UUID 들이 있는 예전 로직이라는 의미
-> 하나의 UUID 로만 조회한다.

3. 폴링 반환 로직 : 다시 데이터 구조를 변경한다.

```java
private List<ResultDto> setFakeResultDto(ResultDto dto, List<UUID> uuidList) {
    List<ResultDto> list = new ArrayList<>();
    List<TaskDto> taskList = dto.getTaskList();
    for (int i = 0; i < uuidList.size(); i++) {
        var copy = dto.clone();
        copy.setUuid(uuidList.get(i));
        copy.setTaskList(List.of(taskList.get(i)));

        list.add(copy);
    }
    return list;
}
```
1개의 ResultDto 를 N개의 ResultDto 로 변경하는 로직이다. ( 위에서 생성한 가짜 UUID 를 유지 )

이런식으로 폴링에서 `코드 구조를 수정 못하면서` + `데이터 오염` 을 해결 할 때 사용할 수 있다.
매우 희귀한 케이스일듯 아마도.