[편리한 객체 간 매핑을 위한 MapStruct 적용기 (feat. SENS) - 2022.12.15](https://medium.com/naver-cloud-platform/%EA%B8%B0%EC%88%A0-%EC%BB%A8%ED%85%90%EC%B8%A0-%EB%AC%B8%EC%9E%90-%EC%95%8C%EB%A6%BC-%EB%B0%9C%EC%86%A1-%EC%84%9C%EB%B9%84%EC%8A%A4-sens%EC%9D%98-mapstruct-%EC%A0%81%EC%9A%A9%EA%B8%B0-8fd2bc2bc33b)


Java Bean 유형 간 매핑 구현 단순화하는 코드 생성기

- 다른 매핑 라이브러리 보다 빠르다
- 컴파일 시점 코드 생성 - 런타임 안전성 보장
- Annotation processor 이용 ( Lombok을 이용함. - 먼저 의존성 선언될시 실행 X )
- 반복되는 객체 매핑에서 발생하는 오류 줄이며, 구현 코드 자동으로 만들어주므로 사용 쉬움

interface 를 선언하면, 매핑 필요한 객체 대해 자동으로 구현체 만들어줌
-> `build/classes/java/main` 에 만들어지게 된다.

```java
public class MessageBodyDto {
        private String title;
        private String content;
        private String sender;
        private List<String> receiver;
        private LocalDateTime requestTime;
        private String type;
}
```

```java
public class RequestDto {
        private String title;
        private String content;
        private String sender;
        private List<String> receiver;
        private LocalDateTime requestTime;
        private String type;
}
```

```java
@Mapper  
public interface MessageMapper {  
	MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);  
	  
	// RequestDto -> MessageBodyDto 매핑  
	MessageBodyDto toMessageBodyDto(RequestDto requestDto);  
}
```

자동으로 interface에 대한 구현체를 생성 해준다.

> 자동 생성 구현체

```java
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageBodyDto toMessageBodyDto(RequestDto requestDto) {
        if ( requestDto == null ) {
            return null;
        }

        MessageBodyDto.MessageBodyDtoBuilder messageBodyDto = MessageBodyDto.builder();

        messageBodyDto.title( requestDto.getTitle() );
        messageBodyDto.content( requestDto.getContent() );
        messageBodyDto.sender( requestDto.getSender() );
        List<String> list = requestDto.getReceiver();
        if ( list != null ) {
            messageBodyDto.receiver( new ArrayList<String>( list ) );
        }
        messageBodyDto.requestTime( requestDto.getRequestTime() );
			  messageBodyDto.requestType( requestDto.getRequestType() );

        return messageBodyDto.build();
    }
}
```

```java
public interface MessageMapper {
	MessageMapper INSTANCE = Mappers.getMapper(MessageMapper.class);

         //PageDto, RequestDto -> MessageServiceDto 매핑
	@Mapping(source="pageDto.pageIndex", target="pageIdx")
        @Mapping(source="pageDto.pageCount", target="pageCnt")
	MessageServiceDto toMessageServiceDto(PageDto pageDto, RequestDto requestDto);
}
```

이와같이 사용해 여러 객체를 통해서도 매핑 가능

```java
 @Mapping(source = "messageId", target = "messageId", defaultExpression = "java(UUID.randomUUID().toString())")
        @Mapping(source = "requestDto.type", target = "type", defaultValue = "SMS")
```

messageId 가 null 일 시, UUID default 로 채워줌
requestDto.type null 일 시, 기본 값으로 채워줌

```java
@Mapping(source = "requestDto.sender", target="sender", ignore=true)
```

특정 필드 빼고 매핑 가능

```java
    @Mapping(source = "requestDto.type", target = "type", qualifiedByName = "typeToEnum")
    MessageListServiceDto toMessageListServiceDto(String messageId, Integer count, RequestDto requestDto);

    @Named("typeToEnum")
    static Type typeToEnum(String type) {
     switch (type.toUpperCase()) {
      case "LMS":
       return Type.LMS;
      case "MMS":
       return Type.MMS;
      default:
       return Type.SMS;
     }
    }
```

ENUM 과 같을시 해당 요소를 통해 동적으로 값 변환 가능

