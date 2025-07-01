# Pre-Singed Url

비공개 설정된 객체를 제한된 시간 & 의도된 사용자만 접근하도록 서명 ( singature ) 을 붙여 생성한 URL

- S3 버킷을 열지 않고도, 외부 사용자에게 한시적 다운로드 및 업로드 권한을 줄 수 있다.

```java
private static final Duration PRESIGNEDURL_PUT_EXPIRED_DURATION = Duration.ofMinutes(15);

var putReq = PutObjectRequest.builder()
    .bucket(bucketName)
    .key(key)
    .contentType(mediaType.toString())
    .contentDisposition("attachment; filename=\""
        + encodedFilename
        + "\"")
    .build();

var presignedPut = s3Presigner.presignPutObject(
    PutObjectPresignRequest.builder()
        .signatureDuration(PRESIGNEDURL_PUT_EXPIRED_DURATION)
        .putObjectRequest(putReq)
        .build()
);
```

```java
private static final Duration PRESIGNEDURL_GET_EXPIRED_DURATION = Duration.ofMinutes(60);

var getReq = GetObjectRequest.builder()
    .bucket(bucketName)
    .key(key)
    .responseContentType(mediaType.toString())
    .build();

var presignedGet = s3Presigner.presignGetObject(
    GetObjectPresignRequest.builder()
        .signatureDuration(PRESIGNEDURL_GET_EXPIRED_DURATION)
        .getObjectRequest(getReq)
        .build()
);
```

PUT, GET 각각 위와 같이 Presigner 를 호출해서 받아올 수 있다.

Presinged URL 은
```
https://`<버킷명>`.s3.`<지역명>`.amazonaws.com/`<객체 경로명>`?
X-Amz-Security-Token=`<토큰>`&
X-Amz-Algorithm=AWS4-HMAC-SHA256&
X-Amz-Date=20250701T134244Z&
X-Amz-SignedHeaders=content-disposition%3Bcontent-type%3Bhost& - 서명에 포함된 HTTP 목록 (host 는 포함해야함)
X-Amz-Credential=`<액세스키`> %2F `<서명 날짜>` %2F `<AWS 리전>` %2F `<서비스명>` %2F aws4_request&
X-Amz-Expires=900&
X-Amz-Signature=`<서명값>`
```

와 같은 경로로 되어있다.

## 주의점

```java
var s3Uri = s3Client.utilities().parseUri(URI.create(presignedUrl));
String bucket = s3Uri.bucket().orElseThrow(() -> new IllegalArgumentException("bucket 정보 파싱 실패 - 원본 URL: " + presignedUrl));
String key = s3Uri.key().orElseThrow(() -> new IllegalArgumentException("key 정보 파싱 실패 - 원본 URL: " + presignedUrl));

var sanitizedUrl = s3Client.utilities().getUrl(
        GetUrlRequest.builder()
                .bucket(bucket)
                .key(key)
                .build()
).toString();
```

쿼리 파라미터가 덕지덕지 붙어있는 Signed URL 로 들어올 때  S3Uri 로 파싱하고, bucket 및 key 만 넣을 수 있다.

까먹지 말고,
```java
Region region = s3Uri.region().orElseThrow(() -> new IllegalArgumentException("Region 정보 파싱 실패 - 원본 URL: " + s3Url));
```
Region 정보를 챙겨주자.
Region 정보를 넣지 않으면 제일 처음 설정한 기본 Region 으로 자기가 멋대로 주소를 바꿔버린다.
(`ap-south-1.amazonaws.com` -> `ap-northeast-2.amazonaws.com`)

그냥 이렇게 되면 어차피 예외 터지는거 아닌가? 할 수 있다.

![img.png](https://i.imgur.com/UjYJctq.png)

![img.png](https://i.imgur.com/rLrBggZ.png)

다소 애매할 수 있는게
사진처럼 `application/xml` 과 `301 Moved Permanently` 을 반환한다.
우리의 로직이 이미지를 받는걸 기대하고, 4~5xx 가 아니면 로직을 처리한다고 만들었다면? 로직이 의도치 않게 계속 진행되어 장애를 낼 수 있다.

```java
URI uri = URI.create(s3Url);
log.debug("URI 생성 완료: {}", uri);

// 쿼리 파라미터 제거된 URI 생성
URI baseUri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, null);
log.debug("Base URI 생성 완료 (쿼리 제거됨): {}", baseUri);
```

이와 같이 제공된 URI 를 통해 쿼리 파라미터를 제거하고

```java
var urlInfo = s3Client.utilities().parseUri(baseUri);
```

다시 파싱해서 사용하는 것도 하나의 방법은 될 거 같다. (즉, 자기가 직접 제거하고 사용)