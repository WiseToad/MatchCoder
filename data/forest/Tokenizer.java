import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.Map;
import java.util.Collections;


public class Tokenizer
{
  private static final Logger logger = 
    Logger.getLogger(Tokenizer.class.getName());

  //  

  private static final class GrammarRule
  {
    private int weight;
    private List<String> codeList;

    public GrammarRule(int weight, String... codes)
    {
      this.weight = weight;
      this.codeList = Arrays.asList(codes);
    }

    public int getWeight()
    {
      return weight;
    }

    public List<String> getCodeList()
    {
      return Collections.unmodifiableList(codeList);
    }
  }
  
  private static final class GrammarCategory
  {
    private String code;
    private List<GrammarRule> ruleList;

    @SafeVarargs
    public GrammarCategory(String code, GrammarRule... rules)
    {
      this.code = code;
      this.ruleList = Arrays.asList(rules);
    }

    public String getCode()
    {
      return code;
    }

    public List<GrammarRule> getRuleList()
    {
      return Collections.unmodifiableList(ruleList);
    }
  }

  private static final class Grammar
  {
    private Map<String, GrammarCategory> categoryMap;

    @SafeVarargs
    public Grammar(GrammarCategory... categories)
    {
      categoryMap = new HashMap<String, GrammarCategory>();
      for(GrammarCategory c: categories)
        categoryMap.put(c.getCode(), c);
    }

    public Map<String, GrammarCategory> getCategoryMap()
    {
      return Collections.unmodifiableMap(categoryMap);
    }
  }

  /*  
   *  Weight correspondence:
   *
   *  Cod Description Weight
   *  VL  Very Low    10
   *  L   Low         30
   *  M   Medium      50
   *  H   High        70
   *  VH  Very High   90? (never occurs)
   */

  private static final Grammar grammarPriv =
    new Grammar(
      // basic categories
      new GrammarCategory("COMMA"), // Comma
      new GrammarCategory("DW"),    // academic degree
      new GrammarCategory("F"),     // female (for gender analize)
      new GrammarCategory("FW"),    // family name word
      new GrammarCategory("IPLFW"), // 
      new GrammarCategory("M"),     // male (for gender analize)
      new GrammarCategory("NSEPW"), // Name Separator Word
      new GrammarCategory("NUM"),   // Number
      new GrammarCategory("NW"),    // name word
      new GrammarCategory("PFW"),   // Prefix word
      new GrammarCategory("PW"),    // patronymic word
      new GrammarCategory("SLASH"), // Slash
      new GrammarCategory("SPW"),   // separator
      new GrammarCategory("SW"),    // academic status
      new GrammarCategory("TW"),    // titles (like ОГЛЫ)
      // derived categories
      new GrammarCategory(
        "AAW", // all academic degree and status
        new GrammarRule(50, "AD"),
        new GrammarRule(50, "AD", "AS"),
        new GrammarRule(50, "AS"),
        new GrammarRule(50, "AS", "AD")
      ),
      new GrammarCategory(
        "AD", // all academic degrees
        new GrammarRule(50, "DW"),
        new GrammarRule(50, "DW", "DW"),
        new GrammarRule(50, "DW", "DW", "DW"),
        new GrammarRule(50, "DW", "DW", "SPW", "DW", "DW"),
        new GrammarRule(50, "DW", "SPW", "DW")
      ),
      new GrammarCategory(
        "AFW", // family without titles
        new GrammarRule(50, "FW"),
        new GrammarRule(50, "FW", "SPW", "FW")
      ),
      new GrammarCategory(
        "AFWTW", // family with titles
        new GrammarRule(50, "AFW"),
        new GrammarRule(30, "AFW", "TW"),
        new GrammarRule(10, "ANW"),
        new GrammarRule(10, "PW"),
        new GrammarRule(30, "TW", "AFW")
      ),
      new GrammarCategory(
        "ANW", // all name words
        new GrammarRule(50, "NW"),
        new GrammarRule(10, "NW", "NW"),
        new GrammarRule(10, "NW", "SPW", "NW")
      ),
      new GrammarCategory(
        "ANWTW", // all name words with titles
        new GrammarRule(50, "ANW"),
        new GrammarRule(10, "ANW", "TW"),
        new GrammarRule(30, "TW", "ANW")
      ),
      new GrammarCategory(
        "APWTW", // all patronymic words with titles
        new GrammarRule(30, "NW", "SPW", "PW"),
        new GrammarRule(30, "NW", "SPW", "TW"),
        new GrammarRule(30, "NW", "TW"),
        new GrammarRule(50, "PW"),
        new GrammarRule(50, "PW", "TW"),
        new GrammarRule(30, "TW", "NW"),
        new GrammarRule(30, "TW", "PW")
      ),
      new GrammarCategory(
        "AS", // all academic status
        new GrammarRule(50, "SW"),
        new GrammarRule(50, "SW", "SPW", "SW", "SW"),
        new GrammarRule(50, "SW", "SW")
      ),
      new GrammarCategory(
        "FIO", // family name patronymic
        new GrammarRule(10, "AFWTW"),
        new GrammarRule(50, "AFWTW", "ANWTW"),
        new GrammarRule(50, "AFWTW", "ANWTW", "APWTW"),
        new GrammarRule(50, "ANWTW", "AFWTW"),
        new GrammarRule(50, "ANWTW", "APWTW", "AFWTW")
      ),
      new GrammarCategory(
        "FIOWPFW", // FIO with prefix word
        new GrammarRule(50, "FIO"),
        new GrammarRule(50, "FIO", "IPLFW_R"),
        new GrammarRule(50, "IPLFW_R", "FIO"),
        new GrammarRule(50, "PFWFULL", "FIO")
      ),
      new GrammarCategory(
        "FULL",
        new GrammarRule(50, "AAW", "FIOWPFW"),
        new GrammarRule(50, "AD", "FIOWPFW"),
        new GrammarRule(50, "AS", "FIOWPFW", "AD"),
        new GrammarRule(50, "FIOWPFW"),
        new GrammarRule(30, "FIOWPFW", "AAW")
      ),
      new GrammarCategory(
        "IPLFW_R",
        new GrammarRule(50, "IPLFW"),
        new GrammarRule(50, "IPLFW", "IPLFW_R") // be aware of infinite loop here, Andy!
      ),
      new GrammarCategory(
        "MULT", // multiple names
        new GrammarRule(50, "N1"),
        new GrammarRule(50, "N1", "NSEP", "N2")
      ),
      new GrammarCategory(
        "N1", // name 1
        new GrammarRule(50, "FULL")
      ),
      new GrammarCategory(
        "N2", // name 2
        new GrammarRule(50, "FULL")
      ),
      new GrammarCategory(
        "NSEP", // name separator
        new GrammarRule(50, "COMMA"),
        new GrammarRule(50, "NSEPW"),
        new GrammarRule(50, "SLASH")
      ),
      new GrammarCategory(
        "PFWFULL", // prefix words
        new GrammarRule(50, "PFW"),
        new GrammarRule(50, "PFW", "SPW", "PFW")
      )
    );

  private static final Grammar grammarOrg =
    new Grammar(
      // basic categories
      new GrammarCategory("AW"),  // Adjective (Российский, Московский, Волгоградская, ...)
      new GrammarCategory("CNW"), // Name word (Газпром, синтетических, импульс, ...)
      new GrammarCategory("LFW"), // Legal form word (ООО, общество, товарищество, ...)
      new GrammarCategory("NUM"), // Number
      new GrammarCategory("OTW"), // Organization type word (завод, компания, банк, ...)
      // derived categories
      new GrammarCategory(
        "A", // Adjective
        new GrammarRule(50, "AW"),
        new GrammarRule(50, "AW", "AW"),
        new GrammarRule(50, "AW", "AW", "AW")
      ),
      new GrammarCategory(
        "LF", // Legal form
        new GrammarRule(50, "LFW"),
        new GrammarRule(50, "LFW", "LFW"),
        new GrammarRule(50, "LFW", "LFW", "LFW"),
        new GrammarRule(50, "LFW", "LFW", "LFW", "LFW"),
        new GrammarRule(50, "LFW", "LFW", "LFW", "LFW", "LFW")
      ),
      new GrammarCategory(
        "N", // Name
        new GrammarRule(50, "CNW"),
        new GrammarRule(50, "CNW", "N"),
        new GrammarRule(50, "CNW", "NUM"),
        new GrammarRule(50, "NUM", "CNW")
      ),
      new GrammarCategory(
        "ON", // Organization Name
        new GrammarRule(50, "A"),
        new GrammarRule(50, "A", "N"),
        new GrammarRule(50, "A", "N", "OT"),
        new GrammarRule(50, "A", "OT"),
        new GrammarRule(50, "A", "OT", "N"),
        new GrammarRule(30, "CNW", "ON"),
        new GrammarRule(70, "N"),
        new GrammarRule(30, "N", "A"),
        new GrammarRule(50, "N", "A", "OT"),
        new GrammarRule(50, "N", "OT"),
        new GrammarRule(30, "OT", "A"),
        new GrammarRule(50, "OT", "A", "N"),
        new GrammarRule(50, "OT", "N")
      ),
      new GrammarCategory(
        "ORG", // Organization (ROOT)
        new GrammarRule(50, "LF", "ON"),
        new GrammarRule(50, "ON"),
        new GrammarRule(50, "ON", "LF")
      ),
      new GrammarCategory(
        "OT", // Organization type
        new GrammarRule(50, "OTW"),
        new GrammarRule(50, "OTW", "OTW"),
        new GrammarRule(50, "OTW", "OTW", "OTW")
      )
    );

  //

  private static final String rootCodePriv = "FULL";

  private static final String rootCodeOrg = "ORG";

  // Do not consider the array below is trivial - in general there can
  // be any kind of fractional number set of any reasonable length
  // TODO: perfectionist's note: array elements are not immutable!
  private static final int[] weightCoeffs = {6, 5, 4, 3, 2, 1};
  private static final int maxDepth = weightCoeffs.length;
  private static final int maxWidth = 8;

  private static final List<String> targetListPriv =
    Collections.unmodifiableList(Arrays.asList("AFWTW", "ANWTW", "APWTW"));

  private static final List<String> targetListOrg =
    Collections.unmodifiableList(Arrays.asList("LF", "ON"));

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

  public static final class TokenMapping
  {
    private List<String> codeList;
    private List<Range> rangeList;

    public TokenMapping(List<String> codeList, List<Range> rangeList)
    {
      this.codeList = codeList;
      this.rangeList = rangeList;
    }

    public List<String> getCodeList()
    {
      return Collections.unmodifiableList(codeList);
    }

    public List<Range> getRangeList()
    {
      return Collections.unmodifiableList(rangeList);
    }

    public int calcOrder()
    {
      boolean hasGaps = false;
      int end = 0;
      for(Range range: rangeList) {
        int length = range.getLength();
        if(length <= 0)
          continue;
        int start = range.getStart();
        if(start < end)
          return 0;
        if(start > end)
          hasGaps = true;
        end = start + length;
      }
      //return (hasGaps ? 1 : 2);
      return 1;
    }
  }

  private static final class TreeNode
  {
    private final class Stats
    {
      public int totalWeight = 0;
      public int nodeCount = 0;
    }
    
    private Grammar grammar;
    private String code;
    private int level;
    private List<String> targetList;
    private int mappingIndex;
    private int ruleIndex;
    private List<GrammarRule> ruleList;
    private GrammarRule rule;
    private ArrayList<TreeNode> subNodeList;

    public TreeNode(Grammar grammar, String code, int level, List<String> targetList)
    {
      this.grammar = grammar;
      this.code = code;
      this.level = level;
      this.targetList = targetList;

      mappingIndex = targetList.indexOf(code);

      ruleIndex = 0;

      if(level >= maxDepth)
        return;

      GrammarCategory category = grammar.getCategoryMap().get(code);
      if(category != null)
        ruleList = category.getRuleList();

      if(ruleList != null && ruleIndex < ruleList.size()) {
        subNodeList = new ArrayList<TreeNode>();
        setRule();
      }
    }

    public boolean nextSubTree()
    {
      if(subNodeList == null)
        return false;
      
      Iterator<TreeNode> it = subNodeList.iterator();
      while(it.hasNext())
        if(it.next().nextSubTree())
          return true;

      if(ruleList.size() > 1) {
        if(++ruleIndex >= ruleList.size())
          ruleIndex = 0;
        setRule();
      }
      return (ruleIndex > 0);
    }

    private void setRule()
    {
      rule = ruleList.get(ruleIndex);

      subNodeList.clear();
      for(String subCode: rule.getCodeList())
        subNodeList.add(new TreeNode(grammar, subCode, level + 1, targetList));
    }

    public int calcWeight()
    {
      List<Stats> statsList = new ArrayList<>();
      for(int i = 0; i < maxDepth; ++i)
        statsList.add(new Stats());

      calcStats(statsList);

      int weight = 0;
      for(int i = 0; i < maxDepth; ++i) {
        Stats stats = statsList.get(i);
        if(stats.nodeCount > 0)
          weight += stats.totalWeight * weightCoeffs[i] / stats.nodeCount;
        else
          weight += 50 * weightCoeffs[i];
      }

      return weight;
    }

    private void calcStats(List<Stats> statsList)
    {
      if(rule == null)
        return;

      Stats stats = statsList.get(level);
      stats.totalWeight += rule.weight;
      ++stats.nodeCount;

      for(TreeNode subNode: subNodeList)
        subNode.calcStats(statsList);
    }

    public TokenMapping calcMapping()
    {
      ArrayList<String> codeList = new ArrayList<>();
      List<Range> rangeList = new ArrayList<>();
        
      for(int i = 0; i < targetList.size(); ++i)
        rangeList.add(new Range(0, 0));

      calcMapping(codeList, rangeList);

      return new TokenMapping(codeList, rangeList);
    }

    private void calcMapping(ArrayList<String> codeList, List<Range> rangeList)
    {
      int rangeStart = codeList.size();        
    
      if(subNodeList == null)
        codeList.add(code);
      else
        subNodeList.forEach(subNode -> subNode.calcMapping(codeList, rangeList));

      if(mappingIndex >= 0)
        rangeList.set(mappingIndex, new Range(rangeStart, codeList.size() - rangeStart));
    }

    public List<String> calcOutline()
    {
      ArrayList<String> lines = new ArrayList<String>();
      calcOutline(lines);
      return lines;
    }

    private void calcOutline(ArrayList<String> lines)
    {
      String line = String.join("", Collections.nCopies(level, " ")) + code;
      if(rule != null)
        line = line + "(" + rule.weight + ")";
      lines.add(line);
  
      if(subNodeList != null)
        subNodeList.forEach(subNode -> subNode.calcOutline(lines));
    }
  }

  public static final class Tree
  {
    private int weight;
    private int order;
    private List<Range> rangeList;
    private List<String> outline; // just for debugging purposes

    public Tree(int weight, int order, List<Range> rangeList)
    {
      this(weight, order, rangeList, null);
    }

    public Tree(int weight, int order, List<Range> rangeList, List<String> outline)
    {
      this.weight = weight;
      this.order = order;
      this.rangeList = rangeList;
      this.outline = outline;
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

    public List<String> getOutline()
    {
      return Collections.unmodifiableList(outline);
    }
  }

  //

  public static interface Forest
  {
    public Map<String, Tree> getTreeMap();
  }

  private static class ForestBase
    implements Forest
  {
    private Map<String, Tree> treeMap;

    public ForestBase(Grammar grammar, String rootCode, List<String> targetList)
    {
      treeMap = new HashMap<String, Tree>();

      TreeNode root = new TreeNode(grammar, rootCode, 0, targetList);
      do {
        TokenMapping mapping = root.calcMapping();

        List<String> codeList = mapping.getCodeList();
        if(codeList.size() > maxWidth)
          continue;

        for(Range range: mapping.getRangeList()) {
          // Check if there is non-empty range exists within the list
          // If so, then add/replace tree and exit the loop immediately
          if(range.getLength() > 0) {
            String key = String.join(" ", codeList);
            Tree tree = treeMap.get(key);
            int weight = root.calcWeight();
            int order = mapping.calcOrder();
            if(tree == null || weight > tree.getWeight() ||
                (weight == tree.getWeight() && order > tree.getOrder()))
            {
              treeMap.put(key, new Tree(weight, order, mapping.getRangeList()));
            }
            break;
          }
        }
      }
      while(root.nextSubTree());
    }

    public Map<String, Tree> getTreeMap()
    {
      return Collections.unmodifiableMap(treeMap);
    }
  }

  public static final class ForestPriv
    extends ForestBase
  {
    public ForestPriv() {
      super(grammarPriv, rootCodePriv, targetListPriv);
    }
  }

  public static final class ForestOrg
    extends ForestBase
  {
    public ForestOrg() {
      super(grammarOrg, rootCodeOrg, targetListOrg);
    }
  }
}
