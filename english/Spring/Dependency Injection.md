> Link : https://docs.spring.io/spring-framework/reference/core/beans/dependencies/factory-collaborators.html

Dependency injection (DI) is a process whereby objects define their dependencies (that is, the other objects with which they work) only through constructor arguments, arguments to a factory method, or properties that are set on the object instance after it is constructed or returned from a factory method.

-> 객체(자신)의 의존성을 생성자 인자, 팩토리 메소드 인자 또는 생성된 객체 인스턴스의 프로퍼티를 통해 설정하는 과정이다.

The container then injects those dependencies when it creates the bean.

컨테이너가 빈이 만들어질때 그들의 의존성을 주입한다.

This process is fundamentally the inverse (hence the name, Inversion of Control) of the bean itself controlling the instantiation or location of its dependencies on its own by using direct construction of classes or the Service Locator pattern.

이 과정은 빈이 스스로 초기화를 컨트롤 하거나 그들의 의존성을 배치시키는 것과 근본적으로 반전되어있다. ( hence IoC )

Code is cleaner with the DI principle, and decoupling is more effective when objects are provided with their dependencies. The object does not look up its dependencies and does not know the location or class of the dependencies. As a result, your classes become easier to test, particularly when the dependencies are on interfaces or abstract base classes, which allow for stub or mock implementations to be used in unit tests.

코드는 DI 원칙과 함께 더 깨끗해지고, 객체가 그들의 의존성을 제공받을때 디커플링은 더 효과적이게 된다.
객체는 그들의 의존성을 찾지않고, 그들의 위치나 의존성의 클래스들을 알지못한다.
특히 의존성이 인터페이스나 추상 클래스일 떄, 클래스는 테스트 하기 더 쉬워진다. 스텁이나 목 구현을 단위 테스트에서 사용 가능하기 때문에

- whereby : 이로써
- only through : 오직 통해서만

### Constructor-based Dependency Injection

Constructor-based DI is accomplished by the container invoking a constructor with a number of arguments, each representing a dependency. Calling a `static` factory method with specific arguments to construct the bean is nearly equivalent, and this discussion treats arguments to a constructor and to a `static` factory method similarly.

생성자 주입은 컨테이너가 인자 수에 맞는 생성자를 호출하며, 각 인자는 주어진 의존성들로 구성된다.

빈을 생성하기 위한 특별한 인자와 함께 정적 팩토리 메소드를 호출하는 것과 거의 동일하다.
이 설명은 생성자와 정적 팩토리 메소드가 매우 유사한걸로 대한다.

- accomplished : 뛰어난
- equivalent : 동등한

```java
public class SimpleMovieLister {

	// the SimpleMovieLister has a dependency on a MovieFinder
	private final MovieFinder movieFinder;

	// a constructor so that the Spring container can inject a MovieFinder
	public SimpleMovieLister(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	// business logic that actually uses the injected MovieFinder is omitted...
}
```

Notice that there is nothing special about this class. It is a POJO that has no dependencies on container specific interfaces, base classes, or annotations.

해당 클래스에는 특별한 요소가 없는걸 주목해라.
이것은 POJO 이다. 컨테이너에 대한 의존성도 없고, 특별한 인터페이스, 기반 클래스 어노테이션 등이 없는

### Constructor Argument Resolution

Constructor argument resolution matching occurs by using the argument’s type. If no potential ambiguity exists in the constructor arguments of a bean definition, the order in which the constructor arguments are defined in a bean definition is the order in which those arguments are supplied to the appropriate constructor when the bean is being instantiated.

생성자 인자는 인자의 타입을 통해 조립한다. 생성자 조립 중 모호함이 없다면, 매개변수 순서와 타입에 맞게 적절한 생성자를 통해 생성한다.

### Setter-based Dependency Injection

Setter-based DI is accomplished by the container calling setter methods on your beans after invoking a no-argument constructor or a no-argument `static` factory method to instantiate your bean.

The following example shows a class that can only be dependency-injected by using pure setter injection. This class is conventional Java. It is a POJO that has no dependencies on container specific interfaces, base classes, or annotations.

세터 기반 의존성은 인자가 없는 생성자나, 팩토리 메소드에 컨테이너가 빈에 세터 메소드를 호출함으로 이루어진다.

```java
public class SimpleMovieLister {

	// the SimpleMovieLister has a dependency on the MovieFinder
	private MovieFinder movieFinder;

	// a setter method so that the Spring container can inject a MovieFinder
	public void setMovieFinder(MovieFinder movieFinder) {
		this.movieFinder = movieFinder;
	}

	// business logic that actually uses the injected MovieFinder is omitted...
}
```

이 역시도 특별한 의존성이 없는 POJO 객체다.

The ApplicationContext supports constructor-based and setter-based DI for the beans it manages. It also supports setter-based DI after some dependencies have already been injected through the constructor approach.

애플리케이션 컨텍스트는 빈 관리를 위해 생성자, 세터 방식 두개를 제공한다.
또한, 일부 의존성은 이미 생성자를 통해 주입된 후에도 세터 방식을 제공한다.

You configure the dependencies in the form of a BeanDefinition, which you use in conjunction with PropertyEditor instances to convert properties from one format to another.

너의 의존성은 BeanDefinition 의 형태로 구성하고, 속성을 다른 형식으로 변환하기 위해 PropertyEditor 를 사용한다.

- configure : 구성하다

However, most Spring users do not work with these classes directly (that is, programmatically) but rather with XML bean definitions, annotated components (that is, classes annotated with @Component, @Controller, and so forth), or @Bean methods in Java-based @Configuration classes. These sources are then converted internally into instances of BeanDefinition and used to load an entire Spring IoC container instance.

하지만, 스프링 유저는 이 클래스만 사용해서 일하지 않는다. XML 빈 정의, 어노테이션, 자바 기반 Configuration 내 빈 메소드 등을 선호한다.
이런 요소들은 내부적으로 BeanDefinition 의 인스턴스로 변환되고, IoC 컨테이너의 인스턴스로 로드하는데 사용된다.

#### Constructor-based or setter-based DI?

Since you can mix constructor-based and setter-based DI, it is a good rule of thumb to use constructors for mandatory dependencies and setter methods or configuration methods for optional dependencies.

생성자와 세터 DI 를 섞을수도 있다. 주요 의존성은 생성자로 주입하고, 세터나 설정 메소드를 선택형 의존성으로 주입할 수 있다.

Note that use of the [@Autowired](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/autowired.html) annotation on a setter method can be used to make the property be a required dependency; however, constructor injection with programmatic validation of arguments is preferable.

세터 메소드에 `@Autowired` 어노테이션을 통해 프로퍼티를 의존성이 필요하게 지정할 수 있다.
그러나, 프로그래밍적 검증과 함꼐 생성자 주입이 선호된다.

The Spring team generally advocates constructor injection, as it lets you implement application components as immutable objects and ensures that required dependencies are not `null`. Furthermore, constructor-injected components are always returned to the client (calling) code in a fully initialized state. As a side note, a large number of constructor arguments is a bad code smell, implying that the class likely has too many responsibilities and should be refactored to better address proper separation of concerns.

스프링은 일반적으로 생성자 주입을 옹호한다. 애플리케이션 요소들을 불변 요소가 되게 하고, 필요한 의존성들이 null 이 될 수 없게 한다.
게다가, 생성자 주입은 클라이언트에게 항상 완벽히 초기화된 상태로 반환한다.
추가적으로, 많은 인자의 생성자 주입은 악취를 풍기고, 너무 많은 책임을 가지고 있는걸 암시하고, 더 적절히 분리하도록 리팩토링 하게 한다.

- advocates : 옹호하다
- implying : 암시하다

Setter injection should primarily only be used for optional dependencies that can be assigned reasonable default values within the class. Otherwise, not-null checks must be performed everywhere the code uses the dependency. One benefit of setter injection is that setter methods make objects of that class amenable to reconfiguration or re-injection later. Management through [JMX MBeans](https://docs.spring.io/spring-framework/reference/integration/jmx.html) is therefore a compelling use case for setter injection.

세터 주입은 합리적인 이유를 통해 클래스에 기본적인 값을 넣고 선택적 의존성에 사용해야 한다.
그럼에도 불구하고, not null 체크는 반드시 수행되어야 한다. 세터 주입의 장점중 하나는 재 설계하거나 재 주입을 나중에 해야할 때 유리하다.

- amenable : 적합하다 / 가능하다

Use the DI style that makes the most sense for a particular class. Sometimes, when dealing with third-party classes for which you do not have the source, the choice is made for you. For example, if a third-party class does not expose any setter methods, then constructor injection may be the only available form of DI.

특정 클래스마다 가장 적합한 DI 스타일을 사용하자. 때로, 우리 코드가 아닌 서드 파티 클래스를 사용할 때는 선택이 정해져있다.
특별한 세터 메소드를 호출하지 않으면 생성자 주입이 가능한 유일한 방법이다.

### Dependency Resolution Process

- The `ApplicationContext` is created and initialized with configuration metadata that describes all the beans. Configuration metadata can be specified by XML, Java code, or annotations.

애플리케이션 컨텍스트는 모든 빈들을 설명하는 설정 메타데이터와 함께 만들어지고 초기화된다.
설정 메타데이터는 XML, 자바 코드, 어노테이션 등으로 특정될 수 있다.

- For each bean, its dependencies are expressed in the form of properties, constructor arguments, or arguments to the static-factory method (if you use that instead of a normal constructor). These dependencies are provided to the bean, when the bean is actually created.

각각 빈들은, 의존성들은 프로퍼트의 형태, 생성자 인자 또는 정적 팩토리 메소드의 인자로 표현된다.
이런 의존성들은 빈이 실제로 만들어질때 빈에 제공된다.

- Each property or constructor argument is an actual definition of the value to set, or a reference to another bean in the container.

각각 프로퍼티 또는 생성자 인자는 설정 되기 위한 값들의 실제 정의 이거나, 컨테이너에 있는 다른 빈들의 참조다.

- Each property or constructor argument that is a value is converted from its specified format to the actual type of that property or constructor argument. By default, Spring can convert a value supplied in string format to all built-in types, such as `int`, `long`, `String`, `boolean`, and so forth.

각 프로퍼티 또는 생성자 인자는 특별한 형태에서 실제 값으로 변환된다.
기본적으로, 스프링은 변환할 수 있다. String 형태로 제공된 값들을 모든 타입으로, int long string boolean 등등등