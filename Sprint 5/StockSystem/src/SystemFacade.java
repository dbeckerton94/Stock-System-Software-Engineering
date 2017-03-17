import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SystemFacade {
    private SQLHelper sql;
    private StockHelper stock;
    private StockUI ui;
    private StockMonitor monitor;

    private String stockServer = "inp.io";
    private int stockPort = 5000;

    private String sqlServer = "inp.io";

    private String sqlDatabase = "StockSystem";
    private String sqlUser = "stocksystem";
    private String sqlPassword = "uolstocksystem";

    final static int SERVER_CONNECTED = 1;
    final static int SERVER_DISCONNECTED = 0;
    final static int SERVER_ERROR = -1;
    final static int SERVER_CONNECTING = 2;
    final static int SERVER_UPDATE_PERIOD = 15 * 1000;
    boolean alive;

    public SystemFacade() {
        sql = new SQLHelper(sqlServer, sqlDatabase, sqlUser, sqlPassword);
        stock = new StockHelper(stockServer, stockPort);
        ui = StockUI.getInstance(this);
        monitor = new StockMonitor(this);
        alive = true;
    }

    public Boolean start() {
        ui.notifyConnectionChanged(SERVER_CONNECTING);
        if (sql.connect()) {
            ui.printToLog("Connected to SQL server", false);
            if (stock.connect()) {
                ui.notifyConnectionChanged(SERVER_CONNECTED);
                ui.printToLog("Connected to stock server", false);
                if (register()) {
                    ui.printToLog("Registered with stock server", false);
                    ui.updateAccount(stock.getAccount());
                    monitor.start();
                    return true;
                } else {
                    ui.printToLog("Failed to register with stock server.", true);
                    stock.disconnect();
                    sql.disconnect();
                    return false;
                }
            } else {
                sql.disconnect();
                ui.notifyConnectionChanged(SERVER_ERROR);
            }
        } else {
            ui.notifyConnectionChanged(SERVER_ERROR);
            ui.printToLog("Failed to connect to SQL server.", true);
        }

        return false;
    }

    public boolean isAlive() {
        return alive;
    }

    public void quit() {
        try {
            monitor.interrupt();
            stock.disconnect();
            sql.disconnect();
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }
        alive = false;
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

    public StockUI getUI()
    {
        return ui;
    }

    public Account getAccount(){
        return stock.getAccount();
    }

    public boolean buy(String name, int amount) {

        if (amount <= 0) {
            ui.printToLog("Invalid volume for purchase", true);
            return false;
        }

        Stock toBuy = sql.getStock(name, new Timestamp(new Date().getTime()));

        if(toBuy == null)
        {
            ui.printToLog("Failed to buy " + amount + " of " + name + " - stock not found", true);
            return false;
        }

        Account acc = stock.getAccount();

       if((amount * toBuy.getPrice()) > acc.getBalance())
       {
           ui.printToLog("Failed to buy " + amount + " of " + name + " - insufficient funds", true);
           return false;
       }

        if (stock.buy(toBuy, amount))
        {
            sql.storeTransaction(acc, toBuy, amount);
            updateAccount(acc);
            ui.updateOwnedShares(sql.getShares(acc));
            ui.printToLog("Bought " + amount + " " + toBuy.getName() + " shares @ " + toBuy.getPrice(), false);

            return true;

        } else {

            ui.printToLog("Failed to buy stock " + toBuy.getName(), true);
            return false;

        }
    }

    public boolean sell(String name, int amount)
    {
        if (amount <= 0) {
            ui.printToLog("Invalid volume for sale", true);
            return false;
        }

        Share toSell = sql.getShare(stock.getAccount(), name);

        if(toSell == null || toSell.getAmount() < amount)
        {
            ui.printToLog("Failed to sell " + amount + " of " + name + " - not enough volume", true);
            return false;
        }

        if (stock.sell(toSell.getStock(), amount)) {

            sql.storeTransaction(stock.getAccount(), toSell.getStock(), -amount);
            updateAccount(stock.getAccount());
            ui.updateOwnedShares(sql.getShares(stock.getAccount()));
            ui.printToLog("Sold " + amount + " " + toSell.getStock().getName() + " shares @ " + toSell.getStock().getPrice(), false);

            return true;

        } else {

            ui.printToLog("Failed to sell stock " + toSell.getStock().getName(), true);
            return false;

        }
    }

    public ArrayList<String> getStockNames() {
        return sql.getStockNames();
    }

    public ArrayList<String> getShareNames()
    {
        return sql.getShareNames(stock.getAccount());
    }

    public Stock getStock(String name, Timestamp when) {
        return sql.getStock(name, when);
    }

    public Stock getStock(String name) { return sql.getStock(name, new Timestamp(new Date().getTime())); }

    public ArrayList<Stock> getStockHistory(String name, Timestamp from, Timestamp until) {
        return sql.getStockHistory(name, from, until);
    }

    public ArrayList< Stock > getStockHistory( String name, int n ){
        return sql.getStockHistory( name, n );
    }

    public ArrayList<Share> getShares( Account acc ){
        return sql.getShares( acc );
    }

    public ArrayList<Transaction> getTransactionHistory(Account acc, Timestamp from, Timestamp to) {
        return sql.getTransactionHistory(acc, from, to);
    }

    public ArrayList<Stock> getCurrentStockMarket()
    {
        return stock.getStocks();
    }

    public void updateStockMarket(ArrayList<Stock> updated)
    {
        for(Stock s : updated)
        {
            sql.storeStockUpdate(s);
        }

        ui.updateStocks(updated);
        ui.updateOwnedShares(sql.getShares(stock.getAccount()));
    }

    private void updateAccount(Account account)
    {
        sql.updateAccount(stock.getAccount());
        ui.updateAccount(stock.getAccount());
    }
}
