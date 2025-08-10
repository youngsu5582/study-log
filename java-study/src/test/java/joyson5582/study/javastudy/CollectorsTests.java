package joyson5582.study.javastudy;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.filtering;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.teeing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.AssertionsForClassTypes.within;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CollectorsTests {

    enum Category {
        ELECTRONICS, BOOKS, CLOTHING, FOOD
    }

    record Product(String name, Category category, BigDecimal price) {

    }

    enum LogLevel {
        INFO, WARN, ERROR
    }

    record LogEntry(LogLevel level, String message, Instant timestamp) {

    }

    record Purchase(String customerId, List<Product> items, LocalDateTime purchaseDate) {

    }

    // 테스트용 레코드 클래스 정의
    record Employee(String name, String department, int salary) {

    }

    record Student(String name, int score) {

    }

    record Order(String customerName, List<String> items) {

    }

    @Test
    @DisplayName("toSet 은 Set 으로 변환한다.")
    void convert_toSet() {
        // given
        final List<String> list = List.of("Hello", "World", "World");

        // when
        final var result = list.stream().collect(Collectors.toSet());

        // then
        assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder("Hello", "World");
        // 순서가 보장되지 않으므로 containsExactlyInAnyOrder 사용
    }

    // groupingBy 와 partitioningBy 는 downStream 의 개념을 이해하는게 쉽다.
    // classifier : 그룹화 기준이 되는 함수
    // EX) Employee::department 라면, department 필드를 기준으로 그룹화 한다는 의미
    // downStream : 스트림의 더 아래쪽, 현재 작업의 다음 단계
    // EX) Collectors.toList() 는 그룹화된 결과를 List 로 모은다는 의미
    // EX) Collectors.counting() 는 그룹화된 결과를 센다는 의미

    // => stream 처럼 Functional Chaining 의 느낌으로 생각
    // Collectors.collectingAndThen() 은 수집된 결과에 추가 작업을 수행할 때 사용

    @Test
    @DisplayName("groupingBy 와 counting 을 사용하여 부서별 직원 수를 계산한다.")
    void collectors_groupingBy_and_counting() {
        // given
        final List<Employee> employees = List.of(
            new Employee("Alice", "HR", 60000),
            new Employee("Bob", "Engineering", 75000),
            new Employee("Charlie", "Engineering", 80000),
            new Employee("David", "HR", 70000),
            new Employee("Frank", "Engineering", 90000)
        );

        // when: 부서별로 그룹화하고, 각 그룹의 직원 수를 센다.
        final Map<String, Long> result = employees.stream().collect(Collectors.groupingBy(
            Employee::department, counting()
        ));

        // then: 결과 맵을 검증한다.
        assertThat(result).hasSize(2)
            .contains(Map.entry("HR", 2L), Map.entry("Engineering", 3L));
    }

    @Test
    @DisplayName("groupingBy 와 mapping 을 사용하여 부서별 직원 이름을 조회한다.")
    void collectors_groupingBy_and_mapping() {
        // given
        final List<Employee> employees = List.of(
            new Employee("Alice", "HR", 60000),
            new Employee("Bob", "Engineering", 75000),
            new Employee("Charlie", "Engineering", 80000),
            new Employee("Joyson", "Engineering", 100000)
        );

        // when: 부서별로 그룹화하고, 각 그룹에 속한 직원의 '이름'만 리스트로 모은다.
        final var result = employees.stream().collect(Collectors.groupingBy(
            Employee::department,
            Collectors.mapping(Employee::name, Collectors.toList())
        ));

        // then: 결과 맵을 검증한다.
        assertThat(result).hasSize(2)
            .contains(Map.entry("HR", List.of("Alice")),
                Map.entry("Engineering", List.of("Bob", "Charlie", "Joyson")));
    }

    @Test
    @DisplayName("partitioningBy 를 사용하여 합격/불합격 그룹으로 나누고, 각 그룹의 평균 점수를 계산한다.")
    void collectors_partitioningBy_and_averagingInt() {
        // given
        final List<Student> students = List.of(
            new Student("Amy", 90),
            new Student("Bill", 55),
            new Student("Casey", 82),
            new Student("Dale", 45),
            new Student("Eve", 78)
        );

        // when: 점수가 60점 이상이면 true, 미만이면 false로 그룹을 나누고, 각 그룹의 평균 점수를 계산한다.

        // groupBy 랑 동일하나, partitioningBy 는 boolean 그룹으로 나눔
        // 즉, 무조건 2개의 그룹 ( true, false )
        final Map<Boolean, Double> result = students.stream().collect(
            Collectors.partitioningBy(student -> student.score >= 60,
                collectingAndThen(
                    Collectors.averagingInt(Student::score),
                    avg -> Math.round(avg * 100.0) / 100.0
                )
            )
        );

        // then: 합격(true) 그룹과 불합격(false) 그룹의 평균 점수를 검증한다.
        assertThat(result)
            .contains(
                // true : 90 + 82 + 78 / 3 = 83.33
                Map.entry(true, 83.33),
                // false : 55 + 45 / 2 = 50.0
                Map.entry(false, 50.0)
            );
    }

    @Test
    @DisplayName("groupingBy 와 filtering 을 사용하여 부서별 고연봉자 리스트를 만든다.")
    void collectors_groupingBy_and_filtering() {
        // given
        final List<Employee> employees = List.of(
            new Employee("Alice", "HR", 60000),
            new Employee("Bob", "Engineering", 75000),
            new Employee("Charlie", "Engineering", 90000),
            new Employee("David", "HR", 80000)
        );

        // when: 부서별로 그룹화하되, 연봉이 78000 이상인 직원만 필터링하여 리스트에 포함시킨다.
        final var result = employees.stream().collect(Collectors.groupingBy(
            Employee::department,
            filtering(employee -> employee.salary() >= 78000, Collectors.toList())
        ));

        // then: HR 부서에는 David만, Engineering 부서에는 Charlie만 포함되어야 한다.
        assertThat(result.get("HR")).extracting(Employee::name).containsExactly("David");
        assertThat(result.get("Engineering")).extracting(Employee::name).containsExactly("Charlie");
    }

    @Test
    @DisplayName("groupingBy 와 flatMapping 을 사용하여 고객별로 주문한 모든 상품 목록을 중복 없이 조회한다.")
    void collectors_groupingBy_and_flatMapping() {
        // given
        final List<Order> orders = List.of(
            new Order("Alice", List.of("Apple", "Banana")),
            new Order("Bob", List.of("Cherry", "Date")),
            new Order("Alice", List.of("Apple", "Eggplant"))
        );

        // when: 고객 이름으로 그룹화하고, 각 고객이 주문한 모든 상품들을 하나의 Set으로 합친다.
        final Map<String, Set<String>> result =
            orders.stream().collect(Collectors.groupingBy(
                Order::customerName,
                // mapping 과 다르게, 1:N 관계 처리 (즉, 단일 요소가 다중 요소로 변환)
                // flatMapping 이므로, stream 으로 평탄화 해줘야 함
                Collectors.flatMapping(order -> order.items.stream(), Collectors.toSet())));

        // then: Alice가 주문한 모든 상품은 중복 없이 Apple, Banana, Eggplant 여야 한다.
        assertThat(result.get("Alice")).containsExactlyInAnyOrder("Apple", "Banana", "Eggplant");
        assertThat(result.get("Bob")).containsExactlyInAnyOrder("Cherry", "Date");
    }

    @Test
    @DisplayName("collectingAndThen 을 사용하여 수집된 리스트에서 추가 작업을 수행한다.")
    void collectors_collectingAndThen() {
        // given
        final List<String> names = List.of("Alice", "Bob", "Charlie", "David");

        // when: 이름을 리스트로 모은 뒤(collecting), 그 리스트를 수정 불가능한 리스트로 변환한다(andThen).
        final List<String> result =
//             names.stream().collect(Collectors.toUnmodifiableList());
            names.stream().collect(collectingAndThen(
                Collectors.toList(),
                List::copyOf
            ));

        // then: 결과 리스트는 수정하려고 하면 UnsupportedOperationException을 던져야 한다.
        assertThat(result).containsExactly("Alice", "Bob", "Charlie", "David");
        org.junit.jupiter.api.Assertions.assertThrows(UnsupportedOperationException.class,
            () -> result.add("Eve"));
    }

    // 와; 있는지도 처음 알았음
    // Java 12 에서 추가된 static method, 두 Collector 를 합쳐서 하나의 결과를 만드는 기능
    @Test
    @DisplayName("teeing 을 사용하여 스트림에서 최소값과 최대값을 동시에 찾아 Range 객체를 생성한다.")
    void collectors_teeing() {
        // given
        final List<Integer> numbers = List.of(10, 5, 8, 22, 15);

        record Range(int min, int max) {

        }

        // when: 하나의 스트림에서 minBy와 maxBy를 동시에 실행하고, 두 결과를 합쳐 Range 객체를 만든다.
        final Range result = numbers.stream().collect(
            teeing(
                // minBy, maxBy 는 Optional 반환 ( 값이 없을 수도 있으므로 )
                Collectors.minBy(Integer::compareTo),
                maxBy(Integer::compareTo),
                (max, min) -> new Range(max.orElseThrow(), min.orElseThrow())
            ));

        // then: 생성된 Range 객체의 min과 max 값이 올바른지 검증한다.
        assertThat(result.min()).isEqualTo(5);
        assertThat(result.max()).isEqualTo(22);
    }


    @Test
    @DisplayName("실무 퀴즈 1: 다단계 그룹화 - 카테고리별, 상품별 판매 개수 집계")
    void advanced_multi_level_grouping() {
        // given: 여러 고객의 구매 기록 리스트
        final List<Purchase> purchases = List.of(
            new Purchase("user1", List.of(
                new Product("Laptop", Category.ELECTRONICS, new BigDecimal("1200.00")),
                new Product("Java Book", Category.BOOKS, new BigDecimal("45.00"))
            ), LocalDateTime.now()),
            new Purchase("user2", List.of(
                new Product("T-Shirt", Category.CLOTHING, new BigDecimal("25.00")),
                new Product("Laptop", Category.ELECTRONICS, new BigDecimal("1200.00"))
            ), LocalDateTime.now()),
            new Purchase("user1", List.of(
                new Product("Another Java Book", Category.BOOKS, new BigDecimal("55.00")),
                new Product("Laptop", Category.ELECTRONICS, new BigDecimal("1200.00"))
            ), LocalDateTime.now())
        );
        // when:
        // 모든 구매 기록에서 판매된 상품들을 카테고리별로 그룹화하고,
        // 각 카테고리 내에서 다시 상품 이름으로 그룹화하여 총 몇 개가 팔렸는지 집계하세요.
        // 최종 결과 타입: Map<Category, Map<String, Long>>
        final Map<Category, Map<String, Long>> salesReport = purchases.stream().collect(
            // Collectors 는 downStream 으로 이루어져있는걸 명심
            Collectors.flatMapping(purchase -> purchase.items.stream(),
                Collectors.groupingBy(
                    Product::category,
                    Collectors.groupingBy(
                        Product::name,
                        counting()
                    )
                )
            ));

        // then
        assertThat(salesReport).hasSize(3)
            .containsEntry(Category.ELECTRONICS, Map.of("Laptop", 3L))
            .containsEntry(Category.BOOKS, Map.of("Java Book", 1L, "Another Java Book", 1L))
            .containsEntry(Category.CLOTHING, Map.of("T-Shirt", 1L));
    }

    @Test
    @DisplayName("실무 퀴즈 2: 통계 집계 - 카테고리별 상품 가격 통계 분석")
    void advanced_summarizing_by_category() {
        // given: 판매되고 있는 모든 상품 리스트
        final List<Product> products = List.of(
            new Product("Laptop", Category.ELECTRONICS, new BigDecimal("1500.00")),
            new Product("Mouse", Category.ELECTRONICS, new BigDecimal("25.50")),
            new Product("Keyboard", Category.ELECTRONICS, new BigDecimal("75.00")),
            new Product("Java Book", Category.BOOKS, new BigDecimal("49.99")),
            new Product("Poetry Book", Category.BOOKS, new BigDecimal("19.99")),
            new Product("T-Shirt", Category.CLOTHING, new BigDecimal("22.00")),
            new Product("Jeans", Category.CLOTHING, new BigDecimal("89.00"))
        );

        // when:
        // 상품들을 카테고리별로 그룹화하고, 각 카테고리에 속한 상품들의
        // 가격에 대한 전체 통계(개수, 합계, 평균, 최소, 최대)를 계산하세요.
        // 최종 결과 타입: Map<Category, DoubleSummaryStatistics>
        final Map<Category, DoubleSummaryStatistics> statsByCategory = products.stream()
            .collect(Collectors.groupingBy(
                Product::category,
                Collectors.summarizingDouble(product -> product.price().doubleValue()
                )));
        // then:
        // then:
        // 1. 전체 맵의 크기와 키 존재 여부 확인
        assertThat(statsByCategory).hasSize(3)
            .containsKey(Category.ELECTRONICS)
            .containsKey(Category.BOOKS)
            .containsKey(Category.CLOTHING);

        // 2. 각 카테고리별 통계치를 개별적으로 검증
        // AssertJ의 satisfies를 사용하면 관련 검증을 그룹화하여 가독성을 높일 수 있습니다.
        assertThat(statsByCategory.get(Category.ELECTRONICS)).satisfies(stats -> {
            assertThat(stats.getCount()).isEqualTo(3);
            // isCloseTo(기대값, 오차범위)를 사용하여 부동소수점 비교
            assertThat(stats.getSum()).isCloseTo(1600.50, within(0.01));
            assertThat(stats.getMin()).isCloseTo(25.50, within(0.01));
            assertThat(stats.getMax()).isCloseTo(1500.00, within(0.01));
            assertThat(stats.getAverage()).isCloseTo(533.50, within(0.01));
        });

        assertThat(statsByCategory.get(Category.BOOKS)).satisfies(stats -> {
            assertThat(stats.getCount()).isEqualTo(2);
            assertThat(stats.getSum()).isCloseTo(69.98, within(0.01));
            assertThat(stats.getMin()).isCloseTo(19.99, within(0.01));
            assertThat(stats.getMax()).isCloseTo(49.99, within(0.01));
            assertThat(stats.getAverage()).isCloseTo(34.99, within(0.01));
        });

        assertThat(statsByCategory.get(Category.CLOTHING)).satisfies(stats -> {
            assertThat(stats.getCount()).isEqualTo(2);
            assertThat(stats.getSum()).isCloseTo(111.00, within(0.01));
            assertThat(stats.getMin()).isCloseTo(22.00, within(0.01));
            assertThat(stats.getMax()).isCloseTo(89.00, within(0.01));
            assertThat(stats.getAverage()).isCloseTo(55.50, within(0.01));
        });
    }

    @Test
    @DisplayName("실무 퀴즈 3: 파티셔닝과 복합 분석 - 고가/저가 상품 분석 리포트 생성")
    void advanced_partitioning_and_teeing() {
        // given: 판매되고 있는 모든 상품 리스트
        final List<Product> products = List.of(
            new Product("Laptop", Category.ELECTRONICS, new BigDecimal("1500.00")),
            new Product("Monitor", Category.ELECTRONICS, new BigDecimal("350.00")),
            new Product("Java Book", Category.BOOKS, new BigDecimal("49.99")),
            new Product("T-Shirt", Category.CLOTHING, new BigDecimal("22.00")),
            new Product("Luxury Watch", Category.ELECTRONICS, new BigDecimal("2500.00")),
            new Product("Jeans", Category.CLOTHING, new BigDecimal("89.00"))
        );

        final Map<Boolean, List<Product>> partitionedProducts = products.stream().
            collect(Collectors.partitioningBy(
                product -> product.price.compareTo(BigDecimal.valueOf(100.00)) >= 0));

        final var name = partitionedProducts.getOrDefault(true, new ArrayList<>())
            .stream()
            .max(comparing(product -> product.price))
            .map(Product::name)
            .orElse("N/A");

        final var count = partitionedProducts.getOrDefault(false, new ArrayList<>())
            .stream()
            .count();

        // 분석 결과를 담을 레코드
        record PartitionAnalysis(String mostExpensiveHighValueItem, long lowValueItemCount) {

        }

        // 이렇게도 가능은 하다...?
//        final PartitionAnalysis analysisResult = products.stream().collect(
//            teeing(
//// 첫 번째 Collector: 고가 상품 중 가장 비싼 상품의 이름 찾기
//                filtering(
//                    product -> product.price().compareTo(new BigDecimal("100.00")) >= 0, // 100.00 이상인 상품만 필터링
//                    collectingAndThen(
//                        maxBy(comparing(Product::price)), // 필터링된 상품 중 가장 비싼 상품 찾기 (Optional<Product> 반환)
//                        optionalProduct -> optionalProduct.map(Product::name).orElse("N/A") // Optional에서 이름 추출, 없으면 "N/A"
//                    )
//                ),
//// 두 번째 Collector: 저가 상품의 총 개수 세기
//                filtering(
//                    product -> product.price().compareTo(new BigDecimal("100.00")) < 0, // 100.00 미만인 상품만 필터링
//                    counting() // 필터링된 상품의 개수 세기
//                ),
//// Merger Function: 두 Collector의 결과를 받아 PartitionAnalysis 객체 생성
//                (mostExpensiveItemName, lowValueCount) -> new PartitionAnalysis(mostExpensiveItemName, lowValueCount)
//            )
//        );

        // when:
        // 상품 가격이 100.00 이상인 그룹(true)과 그렇지 않은 그룹(false)으로 나눕니다.
        // teeing Collector를 사용하여,
        // 1. true 그룹에서는 가장 비싼 상품의 '이름'을 찾으세요. (상품이 없다면 "N/A")
        // 2. false 그룹에서는 상품의 '총개수'를 세세요.
        // 두 결과를 조합하여 최종적으로 PartitionAnalysis 객체 하나를 생성하세요.
        // 최종 결과 타입: PartitionAnalysis
        final PartitionAnalysis analysisResult = new PartitionAnalysis(name, count);

        // then:
        assertThat(analysisResult.mostExpensiveHighValueItem).isEqualTo("Luxury Watch");
        assertThat(analysisResult.lowValueItemCount).isEqualTo(3L); // T-Shirt, Jeans, Java Book
    }

    @Test
    @DisplayName("실무 퀴즈 4: 시간 기반 그룹화와 후처리 - 시간대별 로그 메시지 타임라인 생성")
    void advanced_time_based_grouping_and_collectingAndThen() {
        // given: 특정 시간대에 발생한 로그 엔트리 리스트
        final List<LogEntry> logs = List.of(
            new LogEntry(LogLevel.INFO, "User logged in", Instant.parse("2023-08-08T10:15:30Z")),
            new LogEntry(LogLevel.INFO, "Data processed", Instant.parse("2023-08-08T10:25:00Z")),
            new LogEntry(LogLevel.WARN, "Low memory warning",
                Instant.parse("2023-08-08T11:30:00Z")),
            new LogEntry(LogLevel.INFO, "Task completed", Instant.parse("2023-08-08T10:45:00Z")),
            new LogEntry(LogLevel.ERROR, "Connection failed",
                Instant.parse("2023-08-08T11:55:00Z")),
            new LogEntry(LogLevel.WARN, "Deprecated API used",
                Instant.parse("2023-08-08T10:50:00Z"))
        );

        // when:
        // 로그를 발생 '시간(hour)'으로 그룹화하고, 각 시간대에 발생한 로그 메시지들을
        // 발생 순서대로 쉼표와 공백(", ")으로 구분하여 하나의 문자열로 합치세요.
        // 최종적으로 맵의 키(시간)가 오름차순으로 정렬된 SortedMap을 반환하세요.
        // 최종 결과 타입: SortedMap<Integer, String>
        final SortedMap<Integer, String> timeline = logs.stream()
            .collect(Collectors.groupingBy(
                logEntry -> logEntry.timestamp().atZone(ZoneOffset.UTC).getHour(), // 시간(hour)으로 그룹화
                TreeMap::new,
                // 역순
                // 제거 해도 된다고 뜨는데, 제거하면 Integer,String 타입 추론 못해서 예외 발생
                //method Collectors.<T#1,K#1>groupingBy(Function<? super T#1,? extends K#1>) is not applicable
//                (Supplier<TreeMap<Integer, String>>) () -> new TreeMap<>(Comparator.reverseOrder()),
                Collectors.mapping(LogEntry::message, Collectors.joining(", "))
            ));

        // then:
        assertThat(timeline).hasSize(2)
            .containsEntry(10,
                "User logged in, Data processed, Task completed, Deprecated API used")
            .containsEntry(11, "Low memory warning, Connection failed");

    }

    @Test
    @DisplayName("실무 퀴즈 5: Top N 찾기 - 전체 기간 동안 가장 많이 팔린 상품 Top 3 찾기")
    void advanced_find_top_n_items() {
        // given: 여러 고객의 구매 기록 리스트
        final List<Purchase> purchases = List.of(
            new Purchase("user1", List.of(new Product("A", Category.BOOKS, null),
                new Product("B", Category.CLOTHING, null)), LocalDateTime.now()),
            new Purchase("user2", List.of(new Product("C", Category.ELECTRONICS, null),
                new Product("A", Category.BOOKS, null)), LocalDateTime.now()),
            new Purchase("user3", List.of(new Product("D", Category.FOOD, null),
                new Product("B", Category.CLOTHING, null)), LocalDateTime.now()),
            new Purchase("user4", List.of(new Product("A", Category.BOOKS, null),
                new Product("C", Category.ELECTRONICS, null)), LocalDateTime.now()),
            new Purchase("user5", List.of(new Product("B", Category.CLOTHING, null),
                new Product("E", Category.ELECTRONICS, null)), LocalDateTime.now()),
            new Purchase("user6", List.of(new Product("C", Category.ELECTRONICS, null),
                new Product("A", Category.BOOKS, null)), LocalDateTime.now())
        );

        // when:
        final var productToCountMap = purchases.stream().collect(
            Collectors.flatMapping(
                purchase -> purchase.items.stream(),
                Collectors.groupingBy(
                    Product::name,
                    Collectors.counting()
                )
            ));
        final var result = productToCountMap.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .limit(3).toList();

        // then:
        assertThat(result).hasSize(3)
            .extracting(Entry::getKey, Entry::getValue)
            .containsExactlyInAnyOrder(
                // 상품 이름, 판매 개수
                // A 는 4개 팔렸고, B 는 3개 팔렸고, C 는 3개 팔렸다.
                tuple("A", 4L),
                tuple("B", 3L),
                tuple("C", 3L)
            );
    }
}
