## Basic 인증

가장 기본적인 HTTP 인증 방식
인증 정보로 사용자 ID, 비밀번호를 사용한다.

Base64 로 인코딩한 "사용자ID:비밀번호" 문자열을 Basic 과 함께 인증 헤더에 입력

[  The 'Basic' HTTP Authentication Scheme](https://datatracker.ietf.org/doc/html/rfc7617)

에 자세히 설명되어 있다.

Base64 는 쉽게 암/복호화가 가능하다.
즉, 단순 base64 인코딩된 사용자 ID, 비밀번호를 HTTP 로 전달하면 요청의 보안이 보장되지 않는다.
그래서 반드시 HTTPS + TLS 와 함께 사용해야 한다.

### 장점

가장 큰 장점. 간단함!
사용자 ID, 비밀번호 외 로그인 페이지 & 별도 인증 정보를 요구하지 않는다.
-> 쉬운 접근이 중요시되는 웹 서비스인 만큼 Basic 인증은 단순하고 구축하기 쉽다.

### 단점

- 서버에 사용자 목록을 저장해야 한다.

> 저장하지 않으면, 어떻게 막거나 어떻게 알것인가...

적절히 DB 에 관리하거나, 관리를 해야 한다.

- 사용자 권한을 정교히 제어할 수 없다.

사용자한테는 꼭 필요한 리소스에만 권한을 줘야 하는데
Basic 인증 방식으로는 세세하게 사용자 권한 설정하려면 추가 구현이 필요하다.

---

![](https://i.imgur.com/5J5HJke.png)


### in Java

```java
private static String decode(String line) {  
    var decodedBytes = Base64.getDecoder().decode(line);  
    return new String(decodedBytes);  
}  
  
private static String encode(String line) {  
    var encodedBytes = Base64.getEncoder().encode(line.getBytes());  
    return new String(encodedBytes);  
}
```

와 같이 처리 가능하다.

```java
http
  .httpBasic(Customizer.withDefaults()) // BasicAuthenticationFilter 활성화
  .authorizeHttpRequests(auth -> auth
	  .requestMatchers("/admin/**").hasRole("ADMIN")
	  .anyRequest().authenticated())
  .userDetailsService(customUserDetailsService)
  .passwordEncoder(passwordEncoder);
```

spring security 에서도 사용 가능하다고 한다.

### ETC

- 문자 인코딩은 기본적으로 ISO-8859-1 라고 한다.
  하지만, 국제 문자 계정 지원을 해야하면 `WWW-Authenticate: Basic realm="admin", charset="UTF-8"` 와 같이 알려주고
  서버도 `UTF-8` 로 디코딩 하는게 일반적.

- Basic 은 결국 양방향이 가능하므로 ( 복호화, 암호화 ), Gateway 통과하면 JWT or 내부 API 키 등으로 발급해 MSA 는 토큰만 검증하게 하는 식이 일반적이라고 한다.