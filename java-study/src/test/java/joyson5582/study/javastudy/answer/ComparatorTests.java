package joyson5582.study.javastudy.answer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ComparatorTests {

    record Person(String name, int age, String city) {

    }

    private List<Person> people;

    @BeforeEach
    void setUp() {
        people = new ArrayList<>(List.of(
            new Person("Alice", 30, "New York"),
            new Person("Bob", 25, "London"),
            new Person("Charlie", 30, "Paris"),
            new Person("David", 25, "London")
        ));
    }

    @Test
    @DisplayName("comparing: 나이를 기준으로 오름차순 정렬한다.")
    void comparator_comparing_by_age() {
        // given
        // people 리스트는 @BeforeEach 에서 초기화됩니다.

        // then
        people.sort(Comparator.comparing(Person::age));

        // then
        assertThat(people).extracting(Person::name)
            .containsExactly("Bob", "David", "Alice", "Charlie");
        assertThat(people).extracting(Person::age)
            .containsExactly(25, 25, 30, 30);
    }

    @Test
    @DisplayName("reversed: 나이를 기준으로 내림차순 정렬한다.")
    void comparator_reversed() {
        // given
        // people 리스트는 @BeforeEach 에서 초기화됩니다.

        people.sort(Comparator.comparing(Person::age).reversed());

        // then
        assertThat(people).extracting(Person::name)
            .containsExactly("Alice", "Charlie", "Bob", "David");
        assertThat(people).extracting(Person::age)
            .containsExactly(30, 30, 25, 25);
    }

    @Test
    @DisplayName("thenComparing: 나이, 그 다음 이름으로 오름차순 정렬한다.")
    void comparator_thenComparing() {
        // given
        // people 리스트는 @BeforeEach 에서 초기화됩니다.

        people.sort(Comparator.comparing(Person::age).thenComparing(Person::name));

        // then
        assertThat(people).extracting(Person::name)
            .containsExactly("Bob", "David", "Alice", "Charlie");
    }

    @Test
    @DisplayName("comparingInt: 나이를 기준으로 정렬 (성능을 위해 박싱 방지)")
    void comparator_comparingInt() {
        // given
        // people 리스트는 @BeforeEach 에서 초기화됩니다.

        people.sort(Comparator.comparingInt(Person::age));

        // then
        assertThat(people).extracting(Person::age)
            .isSorted();
        assertThat(people.get(0).age()).isEqualTo(25);
        assertThat(people.get(3).age()).isEqualTo(30);
    }

    @Test
    @DisplayName("naturalOrder: 문자열 리스트를 자연 순서(사전 순)로 정렬한다.")
    void comparator_naturalOrder() {
        // given
        final List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "David", "Bob"));

        // naturalOrder : Comparable 인터페이스를 구현한 객체의 자연스러운 순서에 따라 정렬
        names.sort(Comparator.naturalOrder());

        // then
        assertThat(names).containsExactly("Alice", "Bob", "Charlie", "David");
    }

    @Test
    @DisplayName("reverseOrder: 문자열 리스트를 역순으로 정렬한다.")
    void comparator_reverseOrder() {
        // given
        final List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "David", "Bob"));

        // naturalOrder - reversed 는 불가능 ( 타입 추론 X )
        names.sort(Comparator.reverseOrder());

        // then
        assertThat(names).containsExactly("David", "Charlie", "Bob", "Alice");
    }

    @Test
    @DisplayName("nullsFirst: null 값을 가진 요소를 리스트의 맨 앞에 위치시킨다.")
    void comparator_nullsFirst() {
        // given
        people.add(new Person(null, 40, "Berlin"));

        // nullsFirst 안에 Comparator 구현체 주입

        // name 순을 비교하는데 -> null 이 먼저 -> 자연스러운 정렬
        people.sort(Comparator.comparing(Person::name,
            Comparator.nullsFirst(Comparator.naturalOrder())));

        // then
        assertThat(people.get(0).name()).isNull();
        assertThat(people.get(1).name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("nullsLast: null 값을 가진 요소를 리스트의 맨 뒤에 위치시킨다.")
    void comparator_nullsLast() {
        // given
        people.add(new Person(null, 40, "Berlin"));

        people.sort(
            Comparator.comparing(Person::name, Comparator.nullsLast(Comparator.naturalOrder())));

        // then
        assertThat(people.get(4).name()).isNull();
        assertThat(people.get(0).name()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("람다 표현식: 도시 이름의 길이로 정렬한다.")
    void comparator_with_lambda() {
        // given
        // Paris(5), London(6), New York(8)

        people.sort(Comparator.comparing(person -> person.city.length()));

        // then
        assertThat(people).extracting(Person::city)
            .containsExactly("Paris", "London", "London", "New York");
    }

    @Test
    @DisplayName("thenComparing: 복합 정렬 - 도시(내림차순), 나이(내림차순), 이름(오름차순)")
    void comparator_complex_multi_level_sorting() {
        // given
        // people 리스트는 @BeforeEach 에서 초기화됩니다.

        people.sort(Comparator.comparing(Person::city).reversed()
            .thenComparing(Person::age).reversed()
            .thenComparing(Person::name));

        // then
        assertThat(people).extracting(Person::name)
            .containsExactly("Bob", "David", "Alice", "Charlie");
    }

}
