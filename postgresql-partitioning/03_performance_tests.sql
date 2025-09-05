--=============== 03_performance_tests.sql ===============

\echo '===== 테스트 1: 특정 월(1개월) 데이터 조회 (범위 검색) ====='
\echo '--- 단일 테이블 (거대 인덱스 전체를 스캔해야 함)'
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*) FROM logs_single
WHERE created_at >= '2024-09-01 00:00:00 KST' AND created_at < '2024-10-01 00:00:00 KST';

\echo '--- 파티션된 테이블 (9월 파티션 하나만 스캔 - Partition Pruning)'
EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*) FROM logs_partitioned
WHERE created_at >= '2024-09-01 00:00:00 KST' AND created_at < '2024-10-01 00:00:00 KST';


\echo '\n\n===== 테스트 2: 오래된 데이터(1개월 치) 삭제 ====='
\echo '--- 단일 테이블 (매우 느리고 DB 부하가 큰 DELETE 작업)'
-- 실제 실행 시 시간이 매우 오래 걸릴 수 있으므로 EXPLAIN만 실행합니다.
EXPLAIN DELETE FROM logs_single
WHERE created_at >= '2023-01-01 00:00:00 KST' AND created_at < '2023-02-01 00:00:00 KST';

\echo '--- 파티션된 테이블 (매우 빠르고 부하가 적은 DDL 작업)'
-- 실제 실행해도 거의 즉시 완료됩니다.
-- DROP TABLE logs_p2023_01; -- (실제 삭제 명령어)
\echo '파티션된 테이블의 삭제는 "DROP TABLE [파티션이름]" 명령어로 즉시 끝납니다.'


\echo '\n\n===== 테스트 3: 인덱스 없는 컬럼 검색 (Full Scan) ====='
\echo '--- 단일 테이블 (거대 테이블 전체를 스캔)'
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM logs_single WHERE message LIKE '%abcdef%' LIMIT 10;

\echo '--- 파티션된 테이블 (여러 파티션을 병렬로 스캔하여 더 빠를 수 있음)'
EXPLAIN (ANALYZE, BUFFERS)
SELECT * FROM logs_partitioned WHERE message LIKE '%abcdef%' LIMIT 10;


\echo '\n\n===== 테스트 4: 유지보수 (인덱스 재구성) ====='
\echo '--- 단일 테이블 (전체 인덱스를 재구성. 매우 오래 걸리고 테이블이 잠길 수 있음)'
-- 실제 실행 시 시간이 매우 오래 걸립니다.
-- REINDEX INDEX idx_logs_single_created_at;
\echo '단일 테이블의 REINDEX는 매우 오래 걸립니다.'

\echo '--- 파티션된 테이블 (특정 파티션의 인덱스만 빠르게 재구성)'
-- 파티션에 생성된 인덱스의 실제 이름 확인 필요
-- REINDEX INDEX logs_p2024_09_created_at_idx;
\echo '파티션된 테이블은 특정 파티션의 인덱스만 빠르게 재구성할 수 있습니다.'
