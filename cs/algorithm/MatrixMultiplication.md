# 행렬 정리 및 피보나치 행렬 곱셈으로 풀기

## 행렬 ( Matrix )

숫자를 직사각형 형태로 배열한 수학적 구조
데이터 구조화, 변환 표현하는데 사용

```
[1 1]
[1 0]
```
2 * 2 행렬

### 벡터

크기와 방향을 가지는 수학적 객체
- 1차원 배열
- 데이터 나열 및 특정 상태 표현

- 열 벡터 :
```
[2]
[3]
[3]
```
- 행 벡터 :
`[2 3 5]`

연결된 값을 묶어서 사용한다.

## 행렬 곱셈

두 행렬을 곱해 새로운 행렬 생성하는 연산

- 첫 번째 행렬의 열 개수와 두 번째 행렬의 열 개수가 같아야 함

```
A
[1 2]
[3 4]

B
[5 6]
[7 8]
```

- 0,0 : 1 * 5 + 2 * 7 = 19
- 0,1 : 1 * 6 + 2 * 8 = 22
- 1,0 : 3 * 5 + 4 * 7 = 43
- 1,1 : 3 * 6 + 4 * 8 = 50

## 행렬 제곱

행렬 A 를 n번 곱하는 것
A^n = A*A*A*A ...

분할 정복이 가능하다.

- 단순 계산 : O(n)
- 분할 정복 : O(logn)

- n이 짝수일 때 : (A^(n/2)) * (A^(n/2))

n = 2k
A^n = A^2k = A^k * A^k

- n이 홀수일 때 : (A^((n-1)/2)) * (A^((n-1)/2)) * A

n = 2k +1
A^n = A^2k * A = A^K * A^K * A

짝수와 홀수

## 피보나치 수열 

F_2 = F_1 + F_0
...
F_n = F_(n-1) + F_(n-2)

```
[F_(n+1)] = [1 1] * [F_(n)] 
[ F_(n) ] = [1 0]  [F_(n-1)]
```

### in Code

```kotlin
fun pow(value: Fibo, exp: Long): Fibo {
    if (exp.toInt() == 1) {
        return value
    } else if (exp.toInt() == 0) {
        return Fibo(Pair(0, 0), Pair(0, 0))
    }
    val result = pow(value, exp / 2)
    return if (exp % 2 == 1L) {
        mul(mul(result, result), value)
    } else {
        mul(result, result)
    }
}

fun mul(first: Fibo, second: Fibo): Fibo {
    val f = (first.first.first * second.first.first + first.first.second * second.second.first)%MOD
    val s = (first.first.first * second.first.second + first.first.second * second.second.second)%MOD
    val t = (first.second.first * second.first.first + first.second.second * second.second.first)%MOD
    val four = (first.second.first * second.first.second + first.second.second * second.second.second)%MOD
    return Fibo(Pair(f, s), Pair(t, four))
}

const val MOD = 1_000_000_007
val ary: Array<IntArray> = arrayOf(intArrayOf(1, 1), intArrayOf(1, 0))

data class Fibo(
    val first: Pair<Long, Long>,
    val second: Pair<Long, Long>
)
```
