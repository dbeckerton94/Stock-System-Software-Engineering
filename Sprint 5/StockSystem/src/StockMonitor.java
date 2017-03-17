import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StockMonitor extends Thread
{
    private SystemFacade system;
    private Jarvis jarvis;


    /// TODO: MERGE THIS
    private HashMap<String, ArrayDeque< Stock >> market;
    public static final int MIN_HISTORY = 4;

    public StockMonitor(SystemFacade system)
    {
        this.system = system;
        market = new HashMap<>();
    }

    private void updateWindow() {

        ArrayList<String> stockNames = system.getStockNames();

        // Initialise market data struct
        if (market.size() != stockNames.size())
        {
            market = new HashMap<>(stockNames.size());

            for(String s : stockNames)
            {
                market.put(s, new ArrayDeque<>());
            }
        }

        Iterator it = market.entrySet().iterator();

        while(it.hasNext())
        {
            Map.Entry<String, ArrayDeque<Stock>> entry = (Map.Entry)it.next();

            ArrayDeque<Stock> hist = entry.getValue();

            if(hist.size() >= MIN_HISTORY) { hist.poll(); }

            hist.add(system.getStockHistory(entry.getKey(), 1).get(0));
        }
        // Break after here
        System.out.println("Updated window.");
    }

    public HashMap<String, ArrayDeque< Stock >> getMarketWindow()
    {
        return market;
    }

    public int getMarketWindowSize()
    {
        if(market.size() > 0) {
           return market.entrySet().iterator().next().getValue().size();
        }
        else
        {
            return 0;
        }
    }

    public void run() {

        jarvis = new Jarvis( system, this );

        ArrayList<Stock> stocks = system.getCurrentStockMarket();
        system.updateStockMarket(stocks);

        Stock before = stocks.get(0);
        Stock after = stocks.get(0);

        while(before.getPrice() == after.getPrice())
        {
            stocks = system.getCurrentStockMarket();
            after = stocks.get(0);
        }

        Date currentTime;
        Date lastUpdate = new Date(new Date().getTime() - system.SERVER_UPDATE_PERIOD);

        system.getUI().printToLog("Update synced with stock server.", false);
        jarvis.start();

        while( !isInterrupted() ) {

            currentTime = new Date();

            if((currentTime.getTime() - lastUpdate.getTime()) >= system.SERVER_UPDATE_PERIOD ) {

                lastUpdate = new Date();

                ArrayList<Stock> updated = system.getCurrentStockMarket();
                system.updateStockMarket(updated);

                //updateWindow();
            }

        }

        jarvis.interrupt();

    }
}
