import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class SystemFacade {
    private SQLHelper sql;
    private StockHelper stock;

    private String stockServer = "127.0.0.1";
    private int stockPort = 5000;

    private String sqlServer = "inp.io";
    private String sqlDatabase = "StockSystem";
    private String sqlUser = "stocksystem";
    private String sqlPassword = "uolstocksystem";

    public SystemFacade() {
        sql = new SQLHelper(sqlServer, sqlDatabase, sqlUser, sqlPassword);
        stock = new StockHelper(stockServer, stockPort);
    }

    public Boolean start() {
        if (sql.connect()) {
            if (stock.connect()) {
                if (register()) {
                    return true;
                } else {
                    System.out.println("Failed to register with server.");
                    stock.disconnect();
                    sql.disconnect();
                    return false;
                }
            } else {
                sql.disconnect();
            }
        }

        return false;
    }

    public void quit() {
        stock.disconnect();
        sql.disconnect();
    }

    private boolean register() {
        // Register with stock server; -1 is invalid registration.
        Account acc = stock.register();

        if (acc.getId() >= 0) {
            // Store account in server
            return sql.storeAccount(acc);
        }

        return false;
    }

    public Account getAccount() {
        return stock.getAccount();
    }

    public boolean buy(String name, int amount) {
        if (amount <= 0) {
            System.out.println("Can't buy 0 or less shares!");
            return false;
        }

        Stock toBuy = sql.getStock(name, new Timestamp(new Date().getTime()));

        if (stock.buy(toBuy, amount)) {
            sql.storeTransaction(stock.getAccount(), toBuy, amount);
            sql.updateAccount(stock.getAccount());
            System.out.println("Brought " + amount + " " + toBuy.getName() + " shares @ " + toBuy.getPrice());
            return true;
        } else {
            System.out.println("Failed to buy stock " + toBuy.getName());
            return false;
        }
    }

    public boolean sell(String name, int amount) {
        if (amount <= 0) {
            System.out.println("Can't sell 0 or less shares!");
            return false;
        }

        Stock toSell = sql.getStock(name, new Timestamp(new Date().getTime()));

        if (stock.sell(toSell, amount)) {
            sql.storeTransaction(stock.getAccount(), toSell, -amount);
            sql.updateAccount(stock.getAccount());
            System.out.println("Sold " + amount + " " + toSell.getName() + " shares @ " + toSell.getPrice());
            return true;
        } else {
            System.out.println("Failed to sell stock " + toSell.getName());
            return false;
        }
    }

    public ArrayList<String> getStockNames() {
        return sql.getStockNames();
    }

    public Stock getStock(String name, Timestamp when) {
        return sql.getStock(name, when);
    }

    public ArrayList<Stock> getStockHistory(String name, Timestamp from, Timestamp until) {
        return sql.getStockHistory(name, from, until);
    }

    public ArrayList<Transaction> getTransactionHistory(Account acc, Timestamp from, Timestamp to) {
        return sql.getTransactionHistory(acc, from, to);
    }

    public ArrayList<Stock> getCurrentStockMarket()
    {
        return stock.getStocks();
    }
}
