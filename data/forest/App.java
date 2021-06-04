import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.Map;
import java.util.stream.Collectors;


public final class App
{
  private static final Logger logger = 
    Logger.getLogger(App.class.getName());

  //  

  private static void saveForest(Tokenizer.Forest forest, String fileName)
    throws IOException
  {
    try (
      BufferedWriter out = new BufferedWriter(new FileWriter(fileName))
    ) {
      for(Map.Entry<String, Tokenizer.Tree> entry: forest.getTreeMap().entrySet()) {
        String key = entry.getKey();
        Tokenizer.Tree tree = entry.getValue();
        out.write(String.format("%s\t%d\t%d\t%s\r\n",
          key, 
          tree.getWeight(), 
          tree.getOrder(),
          tree.getRangeList().stream()
            .map(range -> range.getStart() + "," + range.getLength())
            .collect(Collectors.joining(","))
        ));
      }
    }
    System.out.println("Saved to " + fileName);
  }
  
  public static void main(String[] args)
    throws IOException
  {
    Tokenizer.Forest forest;

    System.out.println("Building PRIV forest");
    forest = new Tokenizer.ForestPriv();
    System.out.println(forest.getTreeMap().size() + " trees built");
    saveForest(forest, "target/IRBICON Name.forest.txt");

    System.out.println("Building ORG forest");
    forest = new Tokenizer.ForestOrg();
    System.out.println(forest.getTreeMap().size() + " trees built");
    saveForest(forest, "target/IRBICON Organization.forest.txt");
    
    System.out.println("Done.");
  }
}
