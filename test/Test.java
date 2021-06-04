public final class Test
{
  public static void main(String[] args)
  {
    System.out.println(MatchCoder.calcPriv("Вася Пупкин"));
    System.out.println(MatchCoder.calcOrg("ООО Рога и Копыта"));

    /*
    -- examples of results divergent from reference
    System.out.println(MatchCoder.calcPriv("БАК ДАРЬЯ"));       //reference is БК$$$$$$$$$ДРЙ$$$$$$$$$$
    System.out.println(MatchCoder.calcPriv("НГУЕН ВАН НОЙ"));   //reference is НГЙН$$$$$$$ВН$$$$$$$$$$$
    System.out.println(MatchCoder.calcPriv("ДЁ ДМИТРИЙ"));      //reference is ДЕ$$$$$$$$$ДМТРЙ$$$$$$$$
    //*/
  }
}
