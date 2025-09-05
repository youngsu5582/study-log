--=============== 02_create_partitioned_table.sql ===============

\timing on

-- 1. 파티션된 부모 테이블 생성
CREATE TABLE logs_partitioned (
    id BIGSERIAL,
    log_level VARCHAR(10),
    message TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 2. 24개월(2년) 동안 매월 파티션을 생성하고 데이터를 삽입
DO $$
DECLARE
    v_start_date TIMESTAMPTZ := '2023-01-01 00:00:00 KST';
    v_partition_name TEXT;
    v_partition_start TIMESTAMPTZ;
    v_partition_end TIMESTAMPTZ;
BEGIN
    FOR i IN 0..23 LOOP
        v_partition_start := v_start_date + (i * INTERVAL '1 month');
        v_partition_end   := v_start_date + ((i + 1) * INTERVAL '1 month');
        v_partition_name  := 'logs_p' || to_char(v_partition_start, 'YYYY_MM');

        -- 파티션 테이블 생성
        RAISE NOTICE '% 파티션 생성...', v_partition_name;
        EXECUTE format('CREATE TABLE %I PARTITION OF logs_partitioned FOR VALUES FROM (%L) TO (%L)',
                       v_partition_name, v_partition_start, v_partition_end);

        -- 데이터 삽입
        RAISE NOTICE '% 데이터 삽입 시작...', v_partition_name;
        INSERT INTO logs_partitioned (log_level, message, created_at)
        SELECT
            (ARRAY['INFO', 'WARN', 'ERROR'])[floor(random() * 3) + 1],
            md5(random()::text),
            v_start_date + (n * INTERVAL '1 second')
        FROM generate_series(1, 10000000) n;
        RAISE NOTICE '% 데이터 삽입 완료.', v_partition_name;

    END LOOP;
END $$;

-- 3. 쿼리 플래너를 위한 통계 정보 갱신
ANALYZE logs_partitioned;

\echo '파티션된 테이블 생성 및 데이터 삽입 완료. 총 2억 4천만 건.'

\timing off
