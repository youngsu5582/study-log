# CloudWatch Log URL 조립할 때 팁

서버 로직 내에서 클라우드 와치 URL 을 조립해야 하는 경우가 있을 수도 있다.

( 예를 들어, 개인정보 보호를 위해 내부 사용자들이 고객들의 데이터를 접근하는 것에 대해 모든 로그를 남긴다던가 )

DB 에 작업 이력과 함께 클라우드 와치 URL 을 저장한다고 가정해보자.
그러면, 우리가 직접 URL 을 생성해줘야 한다. ( SDK 는 이런 기능 제공 X )

```java

String.format(
"https://%s.console.aws.amazon.com/cloudwatch/home?region=%s#logsV2:log-groups/log-group/%s/log-events/%s$3FfilterPattern$3D$26start$3D%d$26end$3D%d",
    region, region, encodedLogGroup, encodedLogStream, startTime, endTime
);
```

맨 처음 region - `console.aws.amazon.com/cloudwatch/home?region=` - 두 번째 region - `#logsV2:log-groups/log-group/` - 인코딩된 로그 그룹 - `/log-events/` - 인코딩된 로그 스트림 - 쿼리 파람

으로 복잡하게 조립되어 있다.

이때 뒤에 쿼리 파라미터는 

```java
URLEncoder.encode(queryParams, StandardCharsets.UTF_8);
```

같은 자바와 같은 표준 제공 인코더를 사용하면 안된다!!
Cloudwatch 는 URL 을 JSURL 이라는 다소 특이한 방식으로 인코딩 한다.

Cloudwatch 는 URL 프래그먼트(`#`) 을 보면 알다시피, SPA 로 이루어져 있다.
페이지 전체를 다시 로드하는게 아닌, URL 의 프래그먼트만 수정해서 뷰를 다시 렌더링한다.

- 어떤 로그 그룹을 보는지
- 어떤 로그 스트림을 보는지
- 어떤 시간 범위 로그를 보는지
- 어떤 필터 패턴을 적용 하는지

사용자가 링크를 클릭하면 프래그먼트를 파싱해 위와 같은 기능들을 가능하게 해준다.

### JSURL

프래그먼트를 적절히 파싱해서 위 요구사항을 수행해줘야 하므로
Cloudwatch 는 애초에 값들을 JSON 으로 받으려고 한다.

JSURL 은 URL 을 통해 JSON 을 안전하고 효율적으로 전달받는 데이터 형식이다.

- 간결성 : 결과물이 JSON + URL 인코딩보다 간결하다
- 가독성 : 결과물이 JSON + URL 인코딩보다 읽기 쉽다. ( 그냥 둘다 어려운거 같다... )
- 실수 방지 : 결과물이 URL 인코딩, 디코딩에 영향 받지 않는 문자들만 포함한다.

```
Curly braces ({ and }) replaced by parentheses (( and ))
Square brackets ([ and ]) replaced by (~ and )
Property names unquoted (but escaped -- see below).
String values prefixed by a single quote (') and escaped
All other JSON punctuation (colon : and comma ,) replaced by tildes (~)
An extra tilde (~) at the very beginning.
```
이런 문법을 따른다고 한다.

> `%XX` 와 같은 표준 인코딩을 따르지 않는 이유
> 이 문자열을 URL 디코딩해야 하나? 라는 혼란 자체를 주지 않기 위해 `$` 뒤에 16진수 아스키 값을 붙여서 알려준다.

그래서, 뒤에 필터 파람은?

```
- 원문

?filterPattern=&start={startTime}&end={endTime}

->

- jsurl 로 변경된 값 
"$3FfilterPattern$3D$26start$3D%d$26end$3D%d",
```

- ? 문자는 $3F로 인코딩 (일반 URL: %3F)
- = 문자는 $3D로 인코딩 (일반 URL: %3D)
- & 문자는 $26으로 인코딩 (일반 URL: %26)

상당히 어렵다...

아마존에서 기본 제공해주는 링크들을 리버스로 조립해서 링크를 만드는건데 왜 이렇게 어려울까? 🫠

## LogEventViewer

헤매는 도중 상당히 유용한 글을 발견했다.
[Is there a way to generate the AWS Console URLs for CloudWatch Log Group filters?](https://stackoverflow.com/questions/60796991/is-there-a-way-to-generate-the-aws-console-urls-for-cloudwatch-log-group-filters)

해당 글로

프래그먼트를 `#logsV2` 가 아니라, `#logEventViewer` 를 사용하면 cloudWatch 측에서 적절히 인코딩 및 파악을 해서 렌더링 해준다고 한다.

이런 코드가 있다면

```java
// 로그 그룹과 스트림명 URL 인코딩 (표준 방식)
String encodedLogGroup = URLEncoder.encode(logGroup, StandardCharsets.UTF_8);
String encodedLogStream = URLEncoder.encode(logStream, StandardCharsets.UTF_8);

// CloudWatch 콘솔용 JSUrl 인코딩된 쿼리 파라미터
// ?filterPattern=&start={startTime}&end={endTime}를 JSUrl로 인코딩
String jsUrlQueryParams = String.format(
        "$3FfilterPattern$3D$26start$3D%d$26end$3D%d",
        startTime, endTime
);

// CloudWatch 로그 이벤트 뷰 URL 생성
return String.format(
        "https://%s.console.aws.amazon.com/cloudwatch/home?region=%s#logsV2:log-groups/log-group/%s/log-events/%s%s",
        region, region, encodedLogGroup, encodedLogStream, jsUrlQueryParams
);
```

이렇게 간결한 코드로 변경된다.

```java
// 프래그먼트 조합
String fragment = String.format(
        "#logEventViewer:group=%s;stream=%s;filter=%s;start=%d;end=%d",
        logGroup,
        logStream,
        pattern,
        startTime,
        endTime
);

// 최종 URL을 조합
return String.format(
        "https://%s.console.aws.amazon.com/cloudwatch/home?region=%s%s",
        region, region, fragment
);
```

하지만, 공식 문서 어디에도 정보를 찾지 못했다...
(동작이 되는건 확인했음)

적절히 사용하자.