### 리팩토링

- Option + 위 / 아래 화살표 : 선택 범위를 늘림, 줄임

```
publisher.publishEvent(new PublishedEvent(id));
```

여기서 `.publishEvent` 부분에 클릭하고 실행하면?

`.publishEvent` -> `publishEvent(new PublishedEvent(id))` -> `publisher.publishEvent(new PublishedEvent(id))`

와 같이 선택된다.

- Option + Command + M : 메소드 추출

![](https://i.imgur.com/dBs3Plb.png)

추출해서 메소드명 선택하면 끝.

- Option + Command + P : 선택된 변수 메소드 파라미터로 이동

변수를 메소드의 파라미터로 이동시켜준다.

#### id 를 int 로 바꾸고 싶다면?

```java
private void publish(String id) {
    publisher.publishEvent(new PublishedEvent(id));  
}
```

와 같은 코드가 있는데 id 를 int 로 바꾸고 싶다면?

1. 매개변수 String id 제거
2. IDEA 추천 Safe Delete 로 제거
3. int id 추가
4. Option + Command + P 로 매개변수 이동
5. F2 로 에러 이동해서 Int 로 반영하기

- F6 : Inner Class 외부로 분리 가능 ( 깔끔히 사라짐 )

![](https://i.imgur.com/tJCsYGb.png)

- upper level : 별도 클래스를 만들어서 분리
- another class : 다른 클래스의 Inner 로 이동

- Shift + F6 : 이름 일괄 변경

- Shift + Control + F6 : 메소드 리턴 일괄 변경

> 사용해봤는데 다소 애매한 면이 있음 ( 변경 후 F2 로 찾아서 반영하든가 해야함 )

- Control + Option + O : Optimize Import

### 포커스 에디터

- Cmd + `[` or `]` : 그 이전 / 이후 포커스로 돌아감

- Option + Option : 멀티 포커스 활성화 ( 그 후, 유지하며 화살표로 멀티 포커스 선택 )

여러 줄 연달아서 변경할때 유용

- F2 : 에러가 난 곳으로 바로 이동

![](https://i.imgur.com/yzowv6o.png)

왜 에러가 발생한지도 바로 보여준다! 매우 유용함

### 검색 텍스트

- Command + F : 현재 파일 검색

- Command + R : 현재 파일에서 대치

- Comand + Shift + F : 프로젝트 전체 내용 검색
- Comand + Shift + R : 프로젝트 전체 내용 대치

> 정규식으로 검색이 가능하다.

#### 특정 부분만 패키지를 일괄 변경하고 싶다면?

com.old.api.([A-Za-z][A-Za-z0-9_])*.config

→ api.(아무거나).config 로 끝나는 요소들 검색

com.youngsu5582.api.$1*.config

→ 괄호 안에 있는 값을 유지해 변경 - $1 에 검색한 값들이 들어감

> 멀티모듈일때 꽤나 유용할지도

- Command + Shift + O : 파일,패키지 검색 ( 패키지 경로도 같이 줌 )

전문검색이 아니라, 유사한 검색도 찾아준다.

NewApiServiceTests 일시, ServiceTests 와 같이 검색해도 나온다.

- Command + Option + O : 심볼(메소드 명,변수 등등) 검색

곳곳에 숨어있는 메소드 찾기 유용

Command + Shift + A : Action ( 수많은 Action 이 있으므로 한번 둘러볼 것 ⭐️ )

Command + E : 최근 파일 목록 조회

Command + Shift + E : 최근 사용한 파일 목록 조회

![](https://i.imgur.com/Z6du6q7.png)

한번더 Command + Shift + E 를 누르면 실제로 내가 편집한 파일들만 보여준다.
역시 매우 유용! ⭐️

### 디버깅

- Control + Shift + D : 기본 디버깅 시작
  ( Control + Shift + R : 그냥 실행 )

- Command + F8 : Line Breakpoint

- Option + Command + R : 프로그램 Resume

- F8 : Step Over

한줄씩 내려가게 해줌

- F7 : Step Into

한줄에서 내부 단계로 들어가게 해줌

![](https://i.imgur.com/cmvOAc3.png)

화살표를 통해서 원하는 곳에 들어갈 수 있다.

- Shift + F8 : Step Out ( 현재 메소드 밖으로 )

너무 깊게 들어가는거 같으면 Shift + F8 을 통해서 계속해서 메소드 밖으로 나와서 탈출 가능함

![](https://i.imgur.com/dV0NIfm.png)


조건문을 걸어서 디버깅도 가능하다.
( 실제 사용하는 변수가 이미 있는 곳에서만 사용 가능 -> 메소드 선언부분에서는 불가능 )

Option + F8 : Evaluate Expression

![](https://i.imgur.com/Rvk4xVW.png)

Evaluate 를 통해 내부 변수 및 요소들 검색 & 조작이 가능하다.

`customRepository.findAll()` 와 같이 유용하게 사용 가능하다.

### Git

Command + K : Commit

Command + Shift + K : Push

Ctrl + V + 4 : Git History

Ctrl + V + 7 : Branch