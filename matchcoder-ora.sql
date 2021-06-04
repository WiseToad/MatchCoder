create or replace type matchcoder_record_t is object
(
  id number,
  name varchar2(250),
  matchcode varchar2(60)
)
/

create or replace type matchcoder_table_t as table of matchcoder_record_t
/

/*
-- obsolete object from version 1.0.0
drop type matchcoder_ora_t
/
*/

create or replace type matchcoder_priv_ora_t as object
(
  key integer,

  static function ODCITableStart(matchcoder_priv_ora out matchcoder_priv_ora_t, input sys_refcursor) return number
    as language java name 'MatchCoderPrivOra.ODCITableStart(java.sql.Struct[], java.sql.ResultSet) return java.math.BigDecimal',

  member function ODCITableFetch(row_count in number, output out matchcoder_table_t) return number
    as language java name 'MatchCoderPrivOra.ODCITableFetch(java.math.BigDecimal, java.sql.Array[]) return java.math.BigDecimal',

  member function ODCITableClose return number
    as language java name 'MatchCoderPrivOra.ODCITableClose() return java.math.BigDecimal'
)
/

create or replace type matchcoder_org_ora_t as object
(
  key integer,

  static function ODCITableStart(matchcoder_org_ora out matchcoder_org_ora_t, input sys_refcursor) return number
    as language java name 'MatchCoderOrgOra.ODCITableStart(java.sql.Struct[], java.sql.ResultSet) return java.math.BigDecimal',

  member function ODCITableFetch(row_count in number, output out matchcoder_table_t) return number
    as language java name 'MatchCoderOrgOra.ODCITableFetch(java.math.BigDecimal, java.sql.Array[]) return java.math.BigDecimal',

  member function ODCITableClose return number
    as language java name 'MatchCoderOrgOra.ODCITableClose() return java.math.BigDecimal'
)
/

create or replace package matchcoder_pkg is

  type record_t is record
  (
    id number,
    name varchar2(250)
  );

  type refcursor_t is ref cursor return record_t;

  function calc_priv(input varchar2) return varchar2
    as language java name 'MatchCoder.calcPriv(java.lang.String) return java.lang.String';

  function calc_priv_pipe(input refcursor_t) return matchcoder_table_t
    pipelined parallel_enable (partition input by any)
    using matchcoder_priv_ora_t;

  function calc_org(input varchar2) return varchar2
    as language java name 'MatchCoder.calcOrg(java.lang.String) return java.lang.String';

  function calc_org_pipe(input refcursor_t) return matchcoder_table_t
    pipelined parallel_enable (partition input by any)
    using matchcoder_org_ora_t;

end;
/
