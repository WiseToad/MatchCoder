import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


public final class TestBatch
{
  public static void main(String[] args)
    throws IOException
  {
    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    System.out.println("Started at " + dateFmt.format(LocalDateTime.now()));

    try (
      BufferedReader in = 
        new BufferedReader(
          new InputStreamReader(
            new FileInputStream("data/reference-data.txt"), "utf8"
          )
        )
    ) {
      in.lines()
        .parallel() // Does serial read gets to concurrent processing here? Not really sure. Nevermind so far..
        .forEach(line ->
        {
          String[] parts = line.split("\t");
          if(parts.length == 3) {
            String id = parts[0];
            String name = parts[1];
            String refMatchCode = parts[2];
            try {
              String matchCode = MatchCoder.calcPriv(name);
              if(!matchCode.equals(refMatchCode))
                System.out.println(id + " " + name + ": no match: " + refMatchCode + " <> " + matchCode);
            }
            catch(Exception e) {
              System.out.println(id + " " + name + ":\r\n" + e.getMessage());
            }
          }
        });
    }

    System.out.println("Finished at " + dateFmt.format(LocalDateTime.now()));
  }
}
