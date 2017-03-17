import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

public class SQLHelper
{
    SQLServer sql;

    public SQLHelper(String db_server, String db_name, String db_user, String db_pass)
    {
        sql = new SQLServer(db_server, db_name, db_user, db_pass);
    }

    private void setup()
    {
        sql.sendStatement(
                "CREATE TABLE IF NOT EXISTS Accounts(\n" +
                "    userID INT NOT NULL,\n" +
                "    balance double NOT NULL DEFAULT 0,\n" +
                "    CONSTRAINT acc_prim\n" +
                "    \tPRIMARY KEY( userID )\n" +
                "    );\n"
        );

        sql.sendStatement("CREATE TABLE IF NOT EXISTS Stocks(\n" +
                "    name VARCHAR( 30 )  NOT NULL DEFAULT 'Undefined',\n" +
                "    CONSTRAINT stocks_pk\n" +
                "    \tPRIMARY KEY( name )\n" +
                "    );\n"
        );

        sql.sendStatement(
                "CREATE TABLE IF NOT EXISTS TransactionHistory(\n" +
                "    userID INT NOT NULL,\n" +
                "    stockName VARCHAR( 30 ) NOT NULL,\n" +
                "    time datetime NOT NULL,\n" +
                "    amount INT NOT NULL,\n" +
                "    price DOUBLE NOT NULL,\n" +
                "    CONSTRAINT transaction_fk_acc\n" +
                "    \tFOREIGN KEY( userID )\n" +
                "    \tREFERENCES Accounts( userID )\n" +
                "    \tON UPDATE CASCADE\n" +
                "    \tON DELETE RESTRICT,\n" +
                "    CONSTRAINT transaction_fk_stock\n" +
                "    \tFOREIGN KEY( stockName )\n" +
                "    \tREFERENCES Stocks( name )\n" +
                "    \tON UPDATE CASCADE\n" +
                "    \tON DELETE RESTRICT,\n" +
                "    CONSTRAINT transaction_pk\n" +
                "    \tPRIMARY KEY( userID, stockName, time )\n" +
                "    );\n"
        );

        sql.sendStatement(
                "CREATE TABLE IF NOT EXISTS StockHistory(\n" +
                "    name VARCHAR( 30 ) NOT NULL,\n" +
                "    time datetime NOT NULL,\n" +
                "    price double NOT NULL,\n" +
                "    diff double NOT NULL, \n" +
                "    CONSTRAINT history_pk\n" +
                "    \tPRIMARY KEY( name, time ),\n" +
                "    CONSTRAINT fk_history_stock\n" +
                "    \tFOREIGN KEY( name )\n" +
                "    \tREFERENCES Stocks( name )\n" +
                "    \tON UPDATE CASCADE\n" +
                "    \tON DELETE RESTRICT\n" +
                "    );\n"
        );
    }

    public Boolean connect()
    {
        if(sql.connect())
        {
            System.out.println("Connected to SQL server.");
            setup();
            return true;
        }

        System.out.println("Failed to connect to SQL server.");
        return false;
    }

    public Boolean disconnect()
    {
        if(sql.isConnected())
        {
            if(sql.disconnect())
            {
                System.out.println("Disconnected from SQL server.");
                return true;
            }
            else
            {
                System.out.println("Failed to disconnect from SQL server.");
                return false;
            }
        }
        else
        {
            System.out.println("Cannot disconnect from SQL server; already disconnected.");
            return false;
        }
    }

    public Boolean storeAccount(Account account)
    {
        String query = "INSERT INTO `Accounts`(`userID`, `balance`) VALUES (" + account.getId() + "," + account.getBalance() + ")";
        return sql.sendStatement(query);
    }

    public Boolean updateAccount(Account account)
    {
        String query = "UPDATE `Accounts` SET `balance`=" + account.getBalance() + " WHERE `userID`= " + account.getId();
        return sql.sendStatement(query);
    }

    public ArrayList<String> getStockNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        ResultSet results = sql.sendQuery("SELECT * FROM `Stocks`");

        try
        {
            while(results.next())
            {
                names.add(results.getString("name"));
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }

        return names;
    }

    public Boolean storeStockUpdate(Stock stock)
    {
        Stock previous = getStock(stock.getName(), new Timestamp(new Date().getTime()));

        if(previous != null)
        {
            return sql.sendStatement("INSERT INTO `StockHistory`(`name`, `time`, `price`, `diff`) VALUES ('" + stock.getName().toLowerCase() + "','" + stock.getTime() + "'," + stock.getPrice() + "," + (stock.getPrice() - previous.getPrice()) + ")");
        }
        else
        {
            String statement1 = "INSERT IGNORE INTO `Stocks`(`name`) VALUES('" + stock.getName().toLowerCase() + "');";
            String statement2 = "INSERT INTO `StockHistory`(`name`, `time`, `price`, `diff`) VALUES ('" + stock.getName().toLowerCase() + "','" + stock.getTime() + "'," + stock.getPrice() + "," + stock.getDiff() + ")";

            return sql.sendStatement(statement1) && sql.sendStatement(statement2);
        }
    }

    public Stock getStock(String name, Timestamp when)
    {
        ResultSet result = sql.sendQuery("SELECT * FROM `StockHistory` WHERE `name` = '" + name + "' AND `time` <= '" + when + "' ORDER BY `time` DESC LIMIT 1");

        try
        {
            if( result.next() ) {
                return new Stock(result.getString("name"), result.getDouble("price"), result.getDouble("diff"), result.getTimestamp("time"));
            }
            return null;
        }
        catch(SQLException e)
        {
            return null;
        }
    }

    public ArrayList<Stock> getStockHistory(String name, Timestamp from, Timestamp until)
    {
        ArrayList<Stock> history = new ArrayList<Stock>();

        ResultSet results = sql.sendQuery("SELECT * FROM `StockHistory` WHERE `name` = '" + name.toLowerCase() + "' AND `time` >= '" + from + "' AND `time` <= '" + until + "';");

        try
        {
            while (results.next())
            {
                history.add(new Stock(results.getString("name"), results.getDouble("price"), results.getDouble("diff"), results.getTimestamp("time")));
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

        return history;
    }

    // userID, stockToBuy, time_bought, amount
    public Boolean storeTransaction( Account acc, Stock s, int amount )
    {
        return sql.sendStatement( "INSERT INTO `TransactionHistory`(`userID`, `stockName`, `time`, `amount`, `price`) VALUES ( "+ acc.getId() + ",'" + s.getName().toLowerCase() + "','" + new Timestamp( new Date().getTime() ) +"',"+ amount + "," + s.getPrice() +" )" );
    }

    public ArrayList< Transaction > getTransactionHistory(Account acc, Timestamp from, Timestamp to )
    {
        ArrayList< Transaction > transactions = new ArrayList< Transaction >();

        ResultSet res = sql.sendQuery( "SELECT * FROM `TransactionHistory` WHERE `time` >= '" + from + "' AND `time` <= '" + to + "';");

        try
        {
            while( res.next() )
            {
                transactions.add( new Transaction( res.getString("stockName"), res.getDouble("price"), res.getInt("amount"), res.getTimestamp("time") ) );
            }
        }
        catch( SQLException e )
        {
            e.printStackTrace();
        }

        return transactions;
    }
}
