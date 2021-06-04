import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public class MatchCoder
{
  private static final Logger logger = 
    Logger.getLogger(MatchCoder.class.getName());

  //  

  private static final class ResourceLoader
  {
    public static boolean load(String resourceName, Function<String, Boolean> lineLoader)
    {
      try {
        try (
          BufferedReader in = 
            new BufferedReader(
              new InputStreamReader(
                ResourceLoader.class.getResourceAsStream(resourceName), "utf8"
              )
            )
        ) {
          while(true) {
            String line = in.readLine();
            if(line == null)
              break;
            if(!lineLoader.apply(line))
              return false;
          }
        }
      }
      catch(IOException e) {
        logger.severe(resourceName + ": " + e.getMessage());
        return false;
      }
      return true;
    }
  }
  
  //
  
  @FunctionalInterface
  private static interface StringOperator
    extends UnaryOperator<String>
  {
    public static StringOperator identity()
    {
      return new StringOperator()
      {
        public String apply(String string)
        {
          return string;
        }
      };
    }
  }

  private static final class StringOperatorChain
    implements StringOperator
  {
    private List<? extends StringOperator> operatorList;

    public StringOperatorChain(List<? extends StringOperator> operatorList)
    {
      this.operatorList = operatorList;
    }

    @Override
    public String apply(String string)
    {
      for(StringOperator operator: operatorList)
        string = operator.apply(string);
      return string;
    }
  }

  //

  private static final class Replacer
    implements StringOperator
  {
    private Pattern pattern;
    private String replaceStr;

    public Replacer(String findStr, String replaceStr)
    {
      this.pattern = Pattern.compile(findStr);
      this.replaceStr = replaceStr;
    }

    @Override
    public String apply(String string)
    {
      Matcher matcher = pattern.matcher(string);
      return matcher.replaceAll(replaceStr);
    }
  }

  private static final class ReplacerChain
    implements StringOperator
  {
    private StringOperator operator;

    public ReplacerChain(String resourceName)
    {
      List<Replacer> replacerList = new ArrayList<>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length == 2)
          replacerList.add(new Replacer(parts[0], parts[1]));  
        else if(parts.length == 1)
          replacerList.add(new Replacer(parts[0], ""));
        return true;
      });

      if(!isLoaded) {
        operator = StringOperator.identity();
        return;
      }

      operator = new StringOperatorChain(replacerList);

      logger.info(resourceName + ": loaded " + replacerList.size() + " entries");
    }

    @Override
    public String apply(String string)
    {
      return operator.apply(string);
    }
  }

  private static final StringOperator preprocPriv =
    new StringOperatorChain(Arrays.asList(
      new ReplacerChain("IRBICON Name Organization Preprocessing.rgx.txt"),
      new ReplacerChain("IRBICON Name Parse 2 letters to n-p.rgx.txt")
    ));

  private static final StringOperator preprocOrg =
    new ReplacerChain("IRBICON Organization Parse Pre-processing.rgx.txt");

  private static final StringOperator translit =
    new ReplacerChain("IRBICON Latin to Cyrillic Transliteration.rgx.txt");

  //

  private static final class Phonetics
    implements StringOperator
  {
    private static final class PhoneticReplacer
    {
      public final Pattern pattern;
      public final String replaceStr;

      public PhoneticReplacer(String findStr, String replaceStr)
      {
        this.pattern = Pattern.compile(findStr);
        this.replaceStr = replaceStr;
      }
    }
    
    private List<PhoneticReplacer> replacerList;

    public Phonetics(String resourceName)
    {
      replacerList = new ArrayList<PhoneticReplacer>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length == 2)
          replacerList.add(new PhoneticReplacer(parts[0], parts[1]));  
        else if(parts.length == 1)
          replacerList.add(new PhoneticReplacer(parts[0], ""));
        return true;
      });

      if(!isLoaded) {
        replacerList.clear();
        return;
      }

      logger.info(resourceName + ": loaded " + replacerList.size() + " entries");
    }

    @Override
    public String apply(String string)
    {
      StringBuffer result = new StringBuffer();

      int currentPos = 0;
      List<PhoneticReplacer> useList = replacerList;
      Matcher matcher = Pattern.compile("").matcher(string);
      while(currentPos < string.length()) {
        int startPos = string.length();
        int endPos = startPos;
        String replaceStr = "";
        List<PhoneticReplacer> newList = new ArrayList<>();
        for(PhoneticReplacer replacer: useList) {
          if(startPos > currentPos) {
            matcher.usePattern(replacer.pattern);
            if(!matcher.find(currentPos))
              continue;
            if(matcher.start() < startPos) {
              startPos = matcher.start();
              endPos = matcher.end();
              replaceStr = replacer.replaceStr;
            }
          }
          newList.add(replacer);
        }
        useList = newList;

        result.append(string.substring(currentPos, startPos));
        result.append(replaceStr);

        currentPos = endPos;
      } 
      
      return result.toString();
    }
  }

  private static final StringOperator prePhonetics =
    new Phonetics("IRBICON Double Letter Removal.phx.txt");

  private static final StringOperator postPhonetics =
    new StringOperatorChain(Arrays.asList(
      // Next two are disabled for sensitivity of 85:
      //new Phonetics("IRBICON Fricative Consonant Reduction.phx.txt"),
      //new Phonetics("IRBICON Fricative Consonant Advanced Reduction.phx.txt"),
      new Phonetics("IRBICON Vowel Transformation and Removal.phx.txt")
    ));

  private static final StringOperator namePhonetics =
    new StringOperatorChain(Arrays.asList(
      prePhonetics, 
      postPhonetics
    ));
        
  private static final StringOperator patronymPhonetics =
    new StringOperatorChain(Arrays.asList(
      prePhonetics, 
      new Phonetics("IRBICON Patronymic Suffix Removal.phx.txt"),
      postPhonetics
    ));

  private static final StringOperator finalPhonetics =
    new Phonetics("MatchCode Final Replace.phx.txt");

  //

  private static final class Finder
  {
    private List<Pattern> patternList;

    public Finder(String resourceName)
    {
      patternList = new ArrayList<Pattern>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length == 1)
          patternList.add(Pattern.compile(parts[0]));
        return true;
      });

      if(!isLoaded) {
        patternList.clear();
        return;
      }

      logger.info(resourceName + ": loaded " + patternList.size() + " entries");
    }

    public boolean find(String string)
    {
      for(Pattern pattern: patternList)
        if(pattern.matcher(string).find())
          return true;
      return false;
    }
  }

  private static final Finder familyFinder =
    new Finder("IRBICON Family Name Categorization.rgx.txt");

  private static final Finder patronymFinder =
    new Finder("IRBICON Patronymic Name Categorization.rgx.txt");

  private static final Finder orgAdjFinder =
    new Finder("IRBICON Organization Adjective Word Categorization.rgx.txt");

  //

  private static final class Chopper
  {
    private Pattern pattern;

    public Chopper()
    {
      pattern = Pattern.compile(
        "(.*?)(" +
          "\\u0009|\\u0020|\\u0021|\\u0022|\\u0023|" +
          "\\u0024|\\u0025|\\u0026|\\u0028|\\u0029|" +
          "\\u002a|\\u002b|\\u002c|\\u002d|\\u002e|" +
          "\\u002f|\\u003a|\\u003b|\\u003c|\\u003d|" +
          "\\u003e|\\u003f|\\u0040|\\u005b|\\u005c|" +
          "\\u005d|\\u005e|\\u005f|\\u0060|\\u007b|" +
          "\\u007c|\\u007d|\\u007e|\\u007f|\\u00a0" +
        ")");
    }

    public List<String> apply(String string)
    {
      ArrayList<String> list = new ArrayList<>();
      Matcher matcher = pattern.matcher(string + " ");
      while(matcher.find()) {
        String token = matcher.group(1);
        String delim = matcher.group(2);
        if(!token.isEmpty())
          list.add(token);
        if(delim.equals("-"))
          list.add(delim);
      }
      return list;
    }
  }

  private static final Chopper chopper = new Chopper();

  //

  private static class TokenCategory
  {
    private int weight;
    private String code;

    public TokenCategory(int weight, String code)
    {
      this.weight = weight;
      this.code = code;
    }

    public int getWeight()
    {
      return weight;
    }

    public String getCode()
    {
      return code;
    }
  }

  private static class TokenVocab
  {
    private static final ArrayList<TokenCategory> defaultEntry = new ArrayList<>();
    private HashMap<String, ArrayList<TokenCategory>> entryMap;
    
    public TokenVocab(String resourceName)
    {
      entryMap = new HashMap<String, ArrayList<TokenCategory>>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length != 3)
          return true;
         
        int weight;
        try {
          weight = Integer.parseInt(parts[2]);
        }
        catch(NumberFormatException e) {
          return true;
        }
        if(weight <= 0 || weight > 100)
          return true;

        ArrayList<TokenCategory> entry = entryMap.get(parts[0]);
        if(entry == null) {
          entry = new ArrayList<TokenCategory>();
          entryMap.put(parts[0], entry);
        }
        entry.add(new TokenCategory(weight, parts[1]));
        return true;
      });

      if(!isLoaded) {
        entryMap.clear();
        return;
      }

      logger.info(resourceName + ": loaded " + entryMap.size() + " entries");
    }

    public List<TokenCategory> find(String token)
    {
      return Collections.unmodifiableList(entryMap.getOrDefault(token, defaultEntry));
    }
  }

  private static class TokenVocabChain
  {
    private List<TokenVocab> vocabList;

    public TokenVocabChain(List<TokenVocab> vocabList)
    {
      this.vocabList = vocabList;
    }

    public List<TokenCategory> find(String token)
    {
      List<TokenCategory> list = new ArrayList<>();
      for(TokenVocab vocab: vocabList)
        list.addAll(vocab.find(token));
      return list;
    }
  }

  private static final TokenVocabChain tokenVocabPriv =
    new TokenVocabChain(Arrays.asList(
      new TokenVocab("IRBICON Name.vcb.txt"),
      new TokenVocab("IRBICON Organization.vcb.txt")
    ));

  private static final TokenVocabChain tokenVocabOrg =
    new TokenVocabChain(Arrays.asList(
      new TokenVocab("IRBICON Organization.vcb.txt")
    ));

  //

  public static final class Range
  {
    private int start;
    private int length;

    public Range(int start, int length)
    {
      this.start = start;
      this.length = length;
    }

    public int getStart()
    {
      return start;
    }

    public int getLength()
    {
      return length;
    }
  }

  public static final class TokenizerTree
  {
    private int weight;
    private int order;
    private List<Range> rangeList;

    public TokenizerTree(int weight, int order, List<Range> rangeList)
    {
      this.weight = weight;
      this.order = order;
      this.rangeList = rangeList;
    }

    public int getWeight()
    {
      return weight;
    }

    public int getOrder()
    {
      return order;
    }

    public List<Range> getRangeList()
    {
      return Collections.unmodifiableList(rangeList);
    }
  }

  public static final class TokenizerForest
  {
    private HashMap<String, TokenizerTree> treeMap;

    public TokenizerForest(String resourceName, int rangeCount)
    {
      treeMap = new HashMap<String, TokenizerTree>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length != 4)
          return true;

        int weight;
        try {
          weight = Integer.parseInt(parts[1]);
        }
        catch(NumberFormatException e) {
          return true;
        }
        if(weight < 0)
          return true;
  
        int order;
        try {
          order = Integer.parseInt(parts[2]);
        }
        catch(NumberFormatException e) {
          return true;
        }
        if(order < 0)
          return true;
  
        String[] rangeParts = parts[3].split(",");
        if(rangeParts.length != rangeCount * 2)
          return true;

        ArrayList<Range> rangeList = new ArrayList<>();
        try {
          for(int i = 0; i < rangeCount; ++i) {
            rangeList.add(new Range(Integer.parseInt(rangeParts[i * 2]),
                                    Integer.parseInt(rangeParts[i * 2 + 1])));
          }
        }
        catch(NumberFormatException e) {
          return true;
        }

        treeMap.put(parts[0], new TokenizerTree(weight, order, rangeList));
        return true;
      });

      if(!isLoaded) {
        treeMap.clear();
        return;
      }

      logger.info(resourceName + ": loaded " + treeMap.size() + " entries");
    }

    public TokenizerTree find(String key)
    {
      return treeMap.get(key);
    }
  }

  private static final TokenizerForest tokenizerForestPriv =
    new TokenizerForest("IRBICON Name.forest.txt", 3);

  private static final TokenizerForest tokenizerForestOrg =
    new TokenizerForest("IRBICON Organization.forest.txt", 2);

  //

  private static final class TransformScheme
    implements StringOperator
  {
    private Map<String, String> schemeMap;

    public TransformScheme(String resourceName)
    {
      schemeMap = new HashMap<String, String>();

      boolean isLoaded = ResourceLoader.load(resourceName, line ->
      {
        String[] parts = line.split("\t");
        if(parts.length == 2)
          schemeMap.put(parts[0], parts[1]);
        return true;
      });

      if(!isLoaded) {
        schemeMap.clear();
        return;
      }

      logger.info(resourceName + ": loaded " + schemeMap.size() + " entries");
    }

    @Override
    public String apply(String string)
    {
      String found = schemeMap.get(string);
      return (found != null ? found : string);
    }
  }

  private static final TransformScheme nameTransformScheme =
    new TransformScheme("IRBICON Given Name Match Values.sch.txt");

  private static final TransformScheme legalFormTransformScheme =
    new TransformScheme("IRBICON Organization Legal Form Standards.sch.txt");

  private static final TransformScheme orgNameTransformScheme =
    new TransformScheme("IRBICON Organization Name Match Values.sch.txt");

  //

  public static String calcPriv(String fullName)
  {
    if(fullName == null)
      return null;
    
    String fullNameConcat = fullName.replaceAll("[ \\u00a0]", "");
    if(fullNameConcat.isEmpty())
      return "";

    List<String> tokenList = chopper.apply(preprocPriv.apply(fullName));

    ArrayList<TokenCategory> solutionList = new ArrayList<>();
    solutionList.add(new TokenCategory(0, ""));

    for(String token: tokenList) {
      List<TokenCategory> categoryList = 
        tokenVocabPriv.find(translit.apply(token.toUpperCase()));

      if(categoryList.isEmpty()) {
        if(familyFinder.find(token))
          categoryList.add(new TokenCategory(70, "FW"));
        if(patronymFinder.find(token))
          categoryList.add(new TokenCategory(70, "PW"));
      }

      if(token.replaceAll("[0-9]", "").isEmpty())
        categoryList.add(new TokenCategory(50, "NUM"));

      if(categoryList.isEmpty()) {
        categoryList.add(new TokenCategory(30, "FW"));
        categoryList.add(new TokenCategory(30, "NW"));
      }

      /* DEBUG (add a slash at the start of this line to open code):
      // Print categories per token:
      System.out.println(token);
      for(TokenCategory cat: categoryList)
      System.out.println("  " + cat.getWeight() + " " + cat.getCode());
      //*/

      ArrayList<TokenCategory> newSolutionList = new ArrayList<>();
      for(TokenCategory solution: solutionList)
        for(TokenCategory category: categoryList)
          newSolutionList.add(new TokenCategory(solution.getWeight() + category.getWeight() * 10,
                                                solution.getCode() + " " + category.getCode()));
      solutionList = newSolutionList;
    }

    /* DEBUG (add a slash at the start of this line to open code):
    // Print solutions:
    System.out.println("solutions:");
    for(TokenCategory cat: solutionList)
      System.out.println("  " + cat.getWeight() + " " + cat.getCode());
    //*/

    TokenizerTree tree = null;
    int weight = 0;
    for(TokenCategory s: solutionList) {
      TokenizerTree t = tokenizerForestPriv.find(s.getCode().substring(1));
      if(t != null) {
        int w = s.getWeight() + t.getWeight();
        if(tree == null || w > weight || 
            (w == weight && t.getOrder() > tree.getOrder()))
        {
          tree = t;
          weight = w;
        }
      }
    }

    if(tree == null)
      return finalPhonetics.apply(
        (fullNameConcat + "$$$$$$$$$$$$$$$$$$$$$$$$").substring(0, 24)
      );

    List<Range> rangeList = tree.getRangeList();
    Range familyRange = rangeList.get(0);
    Range nameRange = rangeList.get(1);
    Range patronymRange = rangeList.get(2);

    String family = "";
    if(familyRange.getLength() > 0) {
      family = tokenList.get(
          familyRange.getStart() + familyRange.getLength() - 1
        );
      family = family.toUpperCase();
      family = translit.apply(family);
      family = namePhonetics.apply(family);
    }

    String name = "";
    if(nameRange.getLength() > 0) {
      name = tokenList.get(nameRange.getStart());
      name = name.toUpperCase();
      name = translit.apply(name);
      name = nameTransformScheme.apply(name);
      name = namePhonetics.apply(name);
    }

    String patronym = "";
    if(patronymRange.getLength() > 0) {
      patronym = String.join("", tokenList.subList(
            patronymRange.getStart(),
            patronymRange.getStart() + patronymRange.getLength())
        );
      patronym = patronym.toUpperCase();
      patronym = translit.apply(patronym);
      //FIXME: Is bug here? Whether the phonetics should be applied word by word?
      patronym = patronymPhonetics.apply(patronym);
    }

    return finalPhonetics.apply(
      (family   + "$$$$$$$$$$$").substring(0, 11) +
      (name     + "$$$$$$$$$"  ).substring(0, 9) +
      (patronym + "$$$$"       ).substring(0, 4)
    );
  }

  public static String calcOrg(String fullName)
  {
    if(fullName == null)
      return null;
    
    String fullNameConcat = fullName.replaceAll("[ \\u00a0]", "");
    if(fullNameConcat.isEmpty())
      return "";

    List<String> tokenList = chopper.apply(preprocOrg.apply(fullName));

    ArrayList<TokenCategory> solutionList = new ArrayList<>();
    solutionList.add(new TokenCategory(0, ""));

    for(String token: tokenList) {
      List<TokenCategory> categoryList = 
        tokenVocabOrg.find(token.toUpperCase());

      if(categoryList.isEmpty()) {
        if(orgAdjFinder.find(token))
          categoryList.add(new TokenCategory(50, "AW"));
      }

      if(token.replaceAll("[0-9]", "").isEmpty())
        categoryList.add(new TokenCategory(50, "NUM"));

      if(categoryList.isEmpty())
        categoryList.add(new TokenCategory(50, "CNW"));

      ArrayList<TokenCategory> newSolutionList = new ArrayList<>();
      for(TokenCategory solution: solutionList)
        for(TokenCategory category: categoryList)
          newSolutionList.add(new TokenCategory(solution.getWeight() + category.getWeight() * 10,
                                                solution.getCode() + " " + category.getCode()));
      solutionList = newSolutionList;
    }

    TokenizerTree tree = null;
    int weight = 0;
    for(TokenCategory s: solutionList) {
      TokenizerTree t = tokenizerForestOrg.find(s.getCode().substring(1));
      if(t != null) {
        int w = s.getWeight() + t.getWeight();
        if(tree == null || w > weight || 
            (w == weight && t.getOrder() > tree.getOrder()))
        {
          tree = t;
          weight = w;
        }
      }
    }

    if(tree == null)
      return finalPhonetics.apply(
        (fullNameConcat + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$").substring(0, 60)
      );

    List<Range> rangeList = tree.getRangeList();
    Range legalFormRange = rangeList.get(0);
    Range orgNameRange = rangeList.get(1);

    String legalForm = "";
    if(legalFormRange.getLength() > 0) {
      legalForm = String.join("", tokenList.subList(
            legalFormRange.getStart(),
            legalFormRange.getStart() + legalFormRange.getLength())
        );
      legalForm = legalForm.toUpperCase();
      legalForm = legalFormTransformScheme.apply(legalForm);
    }

    String orgName = "";
    if(orgNameRange.getLength() > 0) {
      List<String> orgNameWordList = tokenList.subList(
          orgNameRange.getStart(),
          orgNameRange.getStart() + orgNameRange.getLength()
        );
      //Transform scheme and phonetics should be applied word by word
      for(String orgNameWord: orgNameWordList) {
        orgNameWord = orgNameWord.toUpperCase();
      	orgNameWord = orgNameTransformScheme.apply(orgNameWord);
        orgNameWord = namePhonetics.apply(orgNameWord);
        orgName = orgName + orgNameWord + "$";
      }
    }

    return finalPhonetics.apply(
      (legalForm + "$$$$").substring(0, 4) +
      (orgName   + "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$").substring(0, 56)
    );
  }
}
