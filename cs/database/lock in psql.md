# LOCK in PostgreSQL

MVCC 아래에서 동시성 및 일관성을 지키게 해줌

- 읽기, 쓰기 작업은 MVCC 를 통해 서로 방해 없이 수행하도록 설계되어 있음
- 스키마 변경(DDL), VACUUM FULL, 인덱스 생성, TRUNCATE 같은 특정 명령은 데이터 파일 자체를 변경하므로 필요함

## Lock Level

- Table-Level Lock : 테이블 전체 잠금
- Row-Level Lock : 특정 행 잠금, UPDATE 및 DELETE 에 사용
- Page-Level Lock : 테이블, 행 사이인 페이지 잠금, 인덱스 잠금때 사용되며 사용자가 제어할 일 X
- Advisory Locks : 사용자가 직접 특정 이름에 대해 잠금 획득하고 해제, Mysql 의 Named Lock

## Lock Mode

- Access Share : 가장 약한 잠금, `SELECT` 쿼리, 테이블 데이터를 읽을때 사용
- Row Share : `SELECT ... FOR UPDATE`, `SELECT ... FOR SHARE`쿼리, 테이블 특정 행에 잠금
- Row Exclusive : `INSERT, UPDATE, DELETE` 쿼리, 데이터 변경하는 DML 이 사용


- Share Update Exclusive : VACUUM , ANALYZE, DDL 중 동시성을 해치지 않음
- Share : CREATE INDEX, 해당 잠금 시 DML 불가 + 읽기만 가능

- Access Exclusive : DROP TABLE, ALTER TABLE, VACUUM FULL, TRUNCATE 등 테이블 구조 바꾸는 DDL, SELECT 포함 모든 접근 차단

```sql
SELECT
    a.pid,
    a.usename,
    a.query,
    l.locktype,
    c.relname AS locked_table,
    l.mode,
    l.granted,
    age(now(), a.query_start) AS query_duration
FROM
    pg_stat_activity a
JOIN
    pg_locks l ON l.pid = a.pid
LEFT JOIN
    pg_class c ON c.oid = l.relation
WHERE
    a.pid <> pg_backend_pid()
    AND a.state IS NOT NULL
ORDER BY
    a.pid,
    c.relname;
```

> by Gemini

- activity : 서버에 연결된 모든 프로세스의 현재 상태를 보여주는 system view
- locks : 시스템에 존재하는 모든 잠금의 목록을 담고있는 system view
- pg_class : DB의 모든 테이블, 인덱스, 뷰 등 담고있는 system catalog

`pg_blocking_pids` : 특정 pid 의 실행을 막고있는 다른 프로세스의 ID 목록을 배열로 반환해주는 함수

- query : LOCK 을 발생시킨 실제 쿼리
- locktype : 잠금 종류 - relation ( table/lindex ), transactionId
- mode : 잠금 강도 - RowExclusiveLock, AccessExclusiveLock 등등
- granted : 잠금 획득 여부 - true 일 시 잠금을 성공적으로 획득 및 소유, false 일 시 다른 프로세스 가 먼저 건 잠금 때문에 대기

---

### Row Level Lock

테이블의 특정 행 하나하나를 잠금

- 암시적 잠금 : DML 을 통해 DB가 자동으로 획득
- 명시적 잠금 : 코드에서 의도적으로 행을 잠금때 사용

`SELECT ... FOR UPDATE` : 트랜잭션이 끝날 때까지 다른 누구도 이 행을 수정 및 삭제하지 못하게 막음
(`+` `SELECT ... FOR UPDATE/SHARE`, `UPDATE,DELETE` 를 막음, `SELECT` 만 가능)
`SELECT ... FOR SHARE` : 트랜잭션이 끝날 때까지 데이터가 변경되지 못하게 막음
( 여러 트랜잭션이 동시 한 행을 `FOR SHARE` 가능, `SELECT ... FOR UPDATE/SHARE`, `UPDATE,DELETE` 를 막음)

-> 높은 동시성, 서로 다른 행 수행하는 트랜잭션들을 충돌없이 실행 가능

### Table Level Lock

데이터 내용이 아닌, 테이블 구조 및 전체 상태를 변경할 때 사용

- 암시적 잠금 : DDL, 관리 명령어에 의해 자동으로 획득

해당 테이블에 대한 모든 접근을 관리한다.

---

### 똑같은 요소에 UPDATE 를 계속 걸면??

두 개의 로직이 동시에 UPDATE 하면?

첫 번째 UPDATE 구문은

관련있는 요소 `relation` 에 ( 테이블, 인덱스, pkey 등등 포함 ) `RowExclusiveLock` 획득
`virtualxid` ,`transactionid` 에 ExclusiveLock 획득

다음 UPDATE 구문은
위에 말한 Lock 들을 전부 획득하나
`transactionid` 의 ShareLock 을 추가로 요청하고, `granted = false` 승인을 받지 못한다.

다음 UPDATE 를 추가로 걸면
그 다음부터는 AccessExclusiveLock 을 추가로 요청하고, `granted = false` 승인을 받지 못한다.