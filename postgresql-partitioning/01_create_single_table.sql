--=============== 01_create_single_table.sql ===============

-- 터미널에 실행 시간을 출력합니다.
\timing on

-- 1. 파티션 없는 단일 테이블 생성
CREATE TABLE logs_single (
    id BIGSERIAL PRIMARY KEY,
    log_level VARCHAR(10),
    message TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

-- 2. 날짜 검색을 위한 인덱스 생성
CREATE INDEX idx_logs_single_created_at ON logs_single (created_at);

-- 3. 24개월(2년) 동안 매월 1000만 건의 데이터 삽입
DO $$
DECLARE
    v_start_date TIMESTAMPTZ := '2023-01-01 00:00:00 KST';
BEGIN
    FOR i IN 0..23 LOOP
        RAISE NOTICE '% 월 데이터 삽입 시작...', to_char(v_start_date + (i * INTERVAL '1 month'), 'YYYY-MM');

        INSERT INTO logs_single (log_level, message, created_at)
        SELECT
            (ARRAY['INFO', 'WARN', 'ERROR'])[floor(random() * 3) + 1],
            md5(random()::text),
            (v_start_date + (i * INTERVAL '1 month')) + (n * INTERVAL '1 second')
        FROM generate_series(1, 10000000) n;

        RAISE NOTICE '% 월 데이터 삽입 완료.', to_char(v_start_date + (i * INTERVAL '1 month'), 'YYYY-MM');
    END LOOP;
END $$;

-- 4. 쿼리 플래너를 위한 통계 정보 갱신
ANALYZE logs_single;

\echo '단일 테이블 생성 및 데이터 삽입 완료. 총 2억 4천만 건.'

\timing off

