## JWK

Json Web Key
RFC 7517 로 정의된 표준 명세

- 암호화 키를 JSON 형태로 표현하는 표준 방식

서버가 자신의 공개키를 외부에 제공해야 할 때 제공하는 표준 방식 - [구글의 well-known](https://www.googleapis.com/oauth2/v3/certs)

![](https://i.imgur.com/Itt6IxE.png)


서버는 `/.well-known/jwks.json` 과 같은 URL 통해 JWK 목록을 제공한다.
(OpenID Connect 같은 표준 인증 프로토콜의 핵심적 부분)

### 구성 요소

- kty : key type, 키가 어떤 알고리즘 계열에 속하는지 알려줌 - RSA, EC, oct ...
- alg : 키를 사용하는 구체적인 암호화 알고리즘 지정 - RS256, ES256 ...
- use : 공개키의 의도된 사용 용도
- kid : 키를 식별하기 위한 고유 식별자 - Auth 서버는 여러 개의 키를 관리할 수도 있음
- n : RSA 공개키의 모듈러 값, 키 쌍의 일부, Base64 인코딩
- e : RSA 공개키의 공개 지수 값, 키 쌍의 일부, Base64 인코딩

-> n 과 e 를 통해 수학적으로 재구성해 JWT 서명을 검증할 수 있다.

### 흐름도

JWT 수신 & kid 확인

1. 클라이언트가 API 를 호출하며 JWT 인증정보 전송
2. API 서버가 JWT 헤더만 깜
3. 헤더에서 KID 값 확인 ( 어떤 키인지 식별 가능 )

well-known 에서 JWK 목록 조회

1. Auth 서버의 공개된 엔드포인트로 HTTP 요청 ( 매번 요청할 수 없으므로 일정 시간 캐싱 역시도 방법 )
2. 배열을 순회하며 kid 와 일치하는 JSON 객체 찾음

n 과 e 로 공개키 객체 재구성

```java
String n_str = "u...w";
String e_str = "AQAB";

BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(n_str));
BigInteger publicExponent = new BigInteger(1, Base64.getUrlDecoder().decode(e_str));


RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, publicExponent);

KeyFactory factory = KeyFactory.getInstance("RSA");
PublicKey publicKey = factory.generatePublic(spec);
```

1. n 과 e 를 통해 BigInteger 생성
2. 키 스펙 생성
3. 공개 키 생성

재구성된 공개키로 JWT 서명 검증

1. 검증 라이브러리 ( Spring Security 의 JwtDecoder ) 에 JWT 토큰과 공개 키를 넘겨 검증 요청
2. 라이브러리가 내부적 검증 수행
    - JWT 세 부분 분리 ( 헤더 / 페이로드 / 서명 )
    - 공개 키를 사용해 `서명` 을 검증 ( 서명을 풀고, 서명된 데이터의 해시값과 일치하는지 확인 )
3. 검증 성공 시, 토큰 유효하며 위변조되지 않음을 보증
4. API 서버는 페이로드에 담긴 claims 를 신뢰하고 비즈니스 로직 처리