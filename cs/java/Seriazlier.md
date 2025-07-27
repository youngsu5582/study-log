다시 한 번 직렬화 리마인드.

- 직렬화 : 메모리 상 객체를 네트워크로 전송하거나 파일에 저장할 수 있게 일련의 데이터 형식으로 변환하는 것

객체 -> JSON ( JSON -> 객체는 역직렬화 )

## Custom Serializer

Jackson 은 기본적으로 Java 타입 ( String, List, Map ... ) 에 대해선 기본 Serializer 를 제공해준다.
(`JsonSerializer` 를 상속하는 클래스만 300개가 넘으니 )

하지만, 우리 기호에 맞게 직렬화를 해야할 때가 있다. ( 비즈니스 로직, 클라이언트 요구사항 등등등 )

이럴때 StdSerializer 를 상속받는 클래스를 만들면 된다.

### StdSerializer

모든 표준 Serializer 의 상위 클래스

이 내부에 `_handleType` 이라는 필드가 있다.

이 정보를 바탕으로 특정 타입 객체를 만나면 어떤 Serializer 를 사용해야 할지 결정한다.

```java
public Serializer() {  
    super((Class<List<FileInfo>>) (Class<?>) List.class);  
}
```

List 를 넘기고 싶으면 이렇게 넘겨주면 된다.
그 후, 우리는 2가지를 메소드를 오버라이딩 해서 구현하자.

```java
@Override  
public void serialize(List<FileInfo> list,  
                      JsonGenerator gen,  
                      SerializerProvider serializers) throws IOException {  
    // 현재 활성화된 뷰가 지정된 뷰와 일치하는지 확인  
    if (serializers.getActiveView() == view) {  
        List<FileInfo> filtered = list.stream()  
                .filter(fi -> !excludeTypes.contains(fi.getType()))  
                .toList();  
        gen.writeObject(filtered);  
    } else {  
        // 뷰가 다르면 원본 그대로 직렬화  
        gen.writeObject(list);  
    }  
}
```

직렬화를 어떻게 할지 구현하는 메소드

- 첫번째 : 우리가 지정한 타입
- 두번째 : JSON 생성 도구, JSON 을 직접 조립하게 해줌 ( writeStartArray(`[`), writeStartObject(`{`) 등 제공 )
- 세번째 : 직렬화 설정등 제공해주는 객체 ( 활성화 된 뷰, 타임존 등등 제공 )

```java
@Override  
public void serializeWithType(List<GenerateFileInfo> value, JsonGenerator gen,  
                              SerializerProvider serializers, TypeSerializer typeSer)  
        throws IOException {  
    ...
}
```

Jackson 이다형성 처리를 할 때 사용하는 메소드
직렬화 할 때, 데이터의 원본 타입이 뭔지를 명시한다.

예시로, 이런 클래스가 있다고 가정할 때

```java
// 부모 클래스
public abstract class Animal {
    public String name;
}

// 자식 클래스들
public class Dog extends Animal {
    public int barkVolume;
}

public class Cat extends Animal {
    public boolean likesCream;
}

// 동물들을 담는 리스트
public class Zoo {
    public List<Animal> animals;
}
```

타입을 명시하지 않으면

```java
{
  "animals": [
    {
      "name": "Buddy",
      "barkVolume": 10
    },
    {
      "name": "Whiskers",
      "likesCream": true
    }
  ]
}
```

Jackson 은 어떤 하위 클래스인지 파악을 할 수 없다.
-> 그래서, 타입을 어떻게 명시할지, 이해하면 되는지에 대해 작성해야 한다.

```java
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.PROPERTY,
	property = "@type"
)
```

`Id` 는 타입 정보를 무엇으로 표현할지
`As` 는 타입 정보를 어떻게 포함시킬지
(이에 대해선 클래스 들어가면 자세히 나와있다.)

설명이 길었는데 왜 설명했냐면

```java
public void serializeWithType(T value, JsonGenerator gen, SerializerProvider serializers,  
        TypeSerializer typeSer)  
    throws IOException  
{  
    Class<?> clz = handledType();  
    if (clz == null) {  
        clz = value.getClass();  
    }  
    serializers.reportBadDefinition(clz, String.format(  
            "Type id handling not implemented for type %s (by serializer of type %s)",  
            clz.getName(), getClass().getName()));  
}

@Override  
public <T> T reportBadDefinition(JavaType type, String msg) throws JsonMappingException {  
    throw InvalidDefinitionException.from(getGenerator(), msg, type);  
}
```

Serializer 의 기본 메소드는 예외를 던지게 되어있다.
그래서, 우리 로직에서 타입을 저장하는 로직을 사용한다면? 해당 로직도 구현을 해줘야 한다.

> 이때는 JsonTypeInfo, 위에서 말한 요소들이 우리가 직렬화 선언한 변수에 있더라도 적용되지 않는다.

4번째 매개변수 TypeSerializer 를 통해서 타입 정보를 우리가 원하는 대로 넣을 수 있다.
( writeTypePrefix, writeTypeSuffix, typeId 등등 제공 )

```
타입 정의
직렬화 로직
타입 정의
```

의 느낌으로 작성하자.

### ContextualSerializer

Jackson 의 어마어마한 기능이다. 위에서 말한 내용 대로라면 헷갈리는 내용이 있을 것이다.

- 한 타입에 대해 Serializer 는 하나의 처리만 할 수 없는거 아닌가?
  또는
- 조금씩 달라지는 요구사항에 ( 어드민 뷰이면, 특정 파일을 필터링 or 클라이언트 뷰이면, 원본 파일 필터링 등등 ) 매번 만들어야 하나?

이를 해결해주는게 ContextualSerializer 이다.

하나의 Serializer 를 만들고, 문맥을 활용해 특정 상황에 맞게 동적으로 만들어서 사용할 수 있는 것이다.

```java
@Override  
public JsonSerializer<?> createContextual(SerializerProvider prov,  
                                          BeanProperty property)  
        throws JsonMappingException {  
    if (property != null) {  
        ExcludeFileWithView annotation = getAnnotation(property);  
        if (annotation != null) {  
            return new ExcludeGenerateFileSerializer(annotation.view(), annotation.excludeTypes());  
        }  
    }  
    // 어노테이션 없으면 기본 리스트 시리얼라이저 사용  
    return prov.findValueSerializer(property.getType(), property);  
}
```

- 첫번째 : 직렬화 과정의 전반적 상태 관리, 다른 자원 조회할 수 있는 제공자
- 두번째 : 문맥 정보를 가지고 있는 프로퍼티

이 두번째 매개변수를 통해 다양한 문맥을 제공 받을 수 있다.

```java
@ExcludeFileWithView(  
        view = View.Client.class,  
        excludeTypes = { GenerateFileType.ORIGINAL_VIDEO}  
)  
protected List<GenerateFileInfo> fileList = new ArrayList<>();

@JacksonAnnotationsInside  
@JsonSerialize(using = FileSerializer.class)  
public @interface ExcludeFileWithView {  
     Class<? extends View> view();  
  
     FileType[] excludeTypes();  
}
```

- `JacksonAnnotationsInside` 는 커스텀 어노테이션 내부에 Jackson 어노테이션을 선언할 때 사용하는 마커 어노테이션

어노테이션을 통해 매번 클래스를 만드는게 아닌

```java
public ExcludeGenerateFileSerializer(Class<? extends View> view,  
                                     FileType[] excludeTypes) {  
    this();  
    this.view = view;  
    this.excludeTypes = Arrays.asList(excludeTypes);  
}
```

직렬화 로직에 필요한 요소들을 받아서 다르게 처리할 수 있다.
