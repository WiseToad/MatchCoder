import java.util.ArrayList;
import java.sql.*;
import java.math.BigDecimal;
import oracle.jdbc.*;
import oracle.CartridgeServices.*;


public class MatchCoderPrivOra
  implements SQLData
{
  private static final class Context
  {
    private ResultSet input;

    public Context(ResultSet input)
    {
      this.input = input;
    }

    public ResultSet getInput()
    {
      return input;
    }
  }

  public BigDecimal key;

  private static final BigDecimal SUCCESS = new BigDecimal(0);
  private static final BigDecimal FAILURE = new BigDecimal(1);
  
  // SQLData interface implementation

  private String typeName;

  public String getSQLTypeName()
    throws SQLException 
  {
    return typeName;
  }

  public void readSQL(SQLInput stream, String typeName)
    throws SQLException 
  {
    this.typeName = typeName;
    this.key = stream.readBigDecimal();
  }

  public void writeSQL(SQLOutput stream)
    throws SQLException 
  {
    stream.writeBigDecimal(key);
  }
  
  // ODCITable interface implementation

  public static BigDecimal ODCITableStart(Struct[] matchCoderPrivOra, ResultSet input)
    throws SQLException 
  {
    Connection conn = DriverManager.getConnection("jdbc:default:connection:");
  
    int key;
    try {
      key = ContextManager.setContext(new Context(input));
    }
    catch (CountException e) {
      return FAILURE;
    }
  
    matchCoderPrivOra[0] = conn.createStruct("MATCHCODER_PRIV_ORA_T",
                                         new Object[] { new BigDecimal(key) });
    return SUCCESS;
  }

  public BigDecimal ODCITableFetch(BigDecimal rowCount, Array[] output)
    throws SQLException 
  {
    Connection conn = DriverManager.getConnection("jdbc:default:connection:");
  
    Context context;
    try {
      context = (Context)ContextManager.getContext(key.intValue());
    }
    catch (InvalidKeyException e) {
      return FAILURE;
    }
    ResultSet input = context.getInput();
  
    int rowLimit = Math.min(rowCount.intValue(), 4096);
    ArrayList<Object> outputList = new ArrayList<>();
    for(int i = 0; i < rowLimit; ++i) {
      if(!input.next())
        break;
  
      BigDecimal id = input.getBigDecimal(1);
      String name = input.getString(2);
  
      outputList.add(
        (Object)conn.createStruct("MATCHCODER_RECORD_T",
                                  new Object[] { id, name, MatchCoder.calcPriv(name) })
      );
    }
  
    if(!outputList.isEmpty())
      output[0] = ((OracleConnection)conn).createOracleArray("MATCHCODER_TABLE_T",
                                                             outputList.toArray());
    return SUCCESS;
  }

  public BigDecimal ODCITableClose()
    throws SQLException
  {
    Context context;
    try {
      context = (Context)ContextManager.clearContext(key.intValue());
    }
    catch (InvalidKeyException e) {
      return FAILURE;
    }
    ResultSet input = context.getInput();
    
    Statement stmt = input.getStatement();
    input.close();
    if(stmt != null)
      stmt.close();

    return SUCCESS;
  }
}
