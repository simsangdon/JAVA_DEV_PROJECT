/* careat 쿼리 */
/*------------------------------------------------------------------------------
-- 개체 이름 : SYSTEM.ADMIN
-- 만든 날짜 : 2017-12-21 오후 3:46:56
-- 마지막으로 수정한 날짜 : 2017-12-21 오후 3:46:56
-- 상태 : VALID
------------------------------------------------------------------------------*/
DROP TABLE SYSTEM.ADMIN CASCADE CONSTRAINTS;

CREATE TABLE SYSTEM.ADMIN (
  ADMIN_NO    NUMBER           NOT NULL, 
  ADMIN_ID    VARCHAR2(20)     NOT NULL, 
  PWD         VARCHAR2(20)     NOT NULL
)
TABLESPACE SYSTEM
PCTFREE 10
PCTUSED 40
INITRANS 1
MAXTRANS 255
STORAGE (
    INITIAL 64 K
    MINEXTENTS 1
    MAXEXTENTS UNLIMITED
    FREELISTS 1
    FREELIST GROUPS 1
)
LOGGING
NOCACHE
MONITORING
NOPARALLEL
;

ALTER TABLE SYSTEM.ADMIN ADD
(
    PRIMARY KEY ( ADMIN_NO )
        USING INDEX
        TABLESPACE SYSTEM 
        PCTFREE 10
        INITRANS 2
        MAXTRANS 255
        STORAGE (
            INITIAL 64 K
            MINEXTENTS 1
            MAXEXTENTS UNLIMITED
            FREELISTS 1
            FREELIST GROUPS 1
        )
);

ALTER TABLE SYSTEM.ADMIN ADD
(
    UNIQUE ( ADMIN_ID )
        USING INDEX
        TABLESPACE SYSTEM 
        PCTFREE 10
        INITRANS 2
        MAXTRANS 255
        STORAGE (
            INITIAL 64 K
            MINEXTENTS 1
            MAXEXTENTS UNLIMITED
            FREELISTS 1
            FREELIST GROUPS 1
        )
);



/* 조회쿼리 */
select count(*) from admin  where  admin_id=#{admin_id} and pwd=#{pwd}