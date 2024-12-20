CREATE TABLE ip_table (
    cidr_block VARCHAR2(32),
    network_int NUMBER GENERATED ALWAYS AS (
        CASE 
            WHEN INSTR(cidr_block, '/') > 0 THEN 
                CAST(
                    BITAND(
                        (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 1)) * POWER(256, 3)) +
                        (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 2)) * POWER(256, 2)) +
                        (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 3)) * POWER(256, 1)) +
                        (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 4)) * POWER(256, 0)),
                        BITMASK_FUNCTION(SUBSTR(cidr_block, INSTR(cidr_block, '/') + 1))
                    )
                AS NUMBER)
            ELSE NULL
        END
    ) VIRTUAL,
    broadcast_int NUMBER GENERATED ALWAYS AS (
        CASE 
            WHEN INSTR(cidr_block, '/') > 0 THEN 
                CAST(
                    BITOR(
                        BITAND(
                            (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 1)) * POWER(256, 3)) +
                            (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 2)) * POWER(256, 2)) +
                            (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 3)) * POWER(256, 1)) +
                            (TO_NUMBER(REGEXP_SUBSTR(cidr_block, '[0-9]+', 1, 4)) * POWER(256, 0)),
                            BITMASK_FUNCTION(SUBSTR(cidr_block, INSTR(cidr_block, '/') + 1))
                        ),
                        BROADCAST_OFFSET_FUNCTION(SUBSTR(cidr_block, INSTR(cidr_block, '/') + 1))
                    )
                AS NUMBER)
            ELSE NULL
        END
    ) VIRTUAL
);

CREATE OR REPLACE FUNCTION BITMASK_FUNCTION(mask_length NUMBER) RETURN NUMBER IS
BEGIN
    RETURN (POWER(2, mask_length) - 1) * POWER(2, 32 - mask_length);
END;


CREATE OR REPLACE FUNCTION BROADCAST_OFFSET_FUNCTION(mask_length NUMBER) RETURN NUMBER IS
BEGIN
    RETURN POWER(2, 32 - mask_length) - 1;
END;

CREATE OR REPLACE FUNCTION BIT_OR(a IN NUMBER, b IN NUMBER) 
RETURN NUMBER
IS
BEGIN
    RETURN -(BITAND(-(a + 1), -(b + 1)) + 1);
END;

CREATE OR REPLACE FUNCTION BIT_AND(a IN NUMBER, b IN NUMBER)
RETURN NUMBER
DETERMINISTIC
IS
BEGIN
    RETURN BITAND(a, b);
END;
