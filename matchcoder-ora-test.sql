-- regular approach
select matchcoder_pkg.calc_priv('Вася Пупкин') from dual;
select matchcoder_pkg.calc_org('ООО Рога и Копыта') from dual;

-- pipelined function
select * from table(matchcoder_pkg.calc_priv_pipe(cursor(select 1, 'Вася Пупкин' from dual)));
select * from table(matchcoder_pkg.calc_org_pipe(cursor(select 1, 'ООО Рога и Копыта' from dual)));
