쿼리 WHERE 조건, GROUP BY, ORDER BY 가 어떤 경우
인덱스를 사용할 수 있고, 어떤 방식으로 사용할 수 있는지 식별할 수 있어야 한다.

```sql
SELECT *
FROM dept_emp
WHERE dept_no = 'd002' AND emp_no >= 10114;
```

- INDEX ( dept_no, emp_no )
- INDEX ( emp_no, dept_no )

A 는 "dept_no='d002' AND emp_no >= 10144" 레코드 찾은 후, `d002` 아닐 때 까지 쭉 읽기만 하면 된다.
-> 두 칼럼 다 작업 범위 결정 조건

B 는 "emp_no >= 10144 AND dept_no 'd002'" 찾은 후 계속 d002 인지 비교해야 한다.
-> emp_no 만 작업 범위 결정 조건, dept_no 는 필터링 조건

## 가용성과 효율성 판단

`작업 범위 결정 조건` 으로는 사용할 수 없다.
체크 조건으로는 인덱스 사용 가능 

### `NOT-EQUAL` ( "<>", "NOT IN", "NOT BETWEEN", "IS NOT NULL" )

- column <> 'N'
- column NOT IN (10,11,12)
- column is NOT NULL

### LIKE '%??' ( 앞부분이 아닌 뒷부분 일치 ) 형태로 문자열 패턴 비교
- column LIKE `'%승환'`
- column LIKE `'_승환'`
- column LIKE `'%승%'`

### 스토어드 함수나 다른 연산자로 인덱스 칼럼 변형된 후 비교된 경우
- WHERE SUBSTRING(column,1,1) = 'X'
- WHERE DAYOFMONTH(column) = 1
### 데이터 타입이 서로 다른 비교 ( 칼럼 타입 변환해야 비교 가능 경우 )
- WHERE char_column = 10


