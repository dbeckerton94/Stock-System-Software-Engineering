import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Jarvis extends Thread{

    private SystemFacade system;
    private StockMonitor monitor;
    private HashMap< String, Double > avgWindow;

    private static final int BUY_TREND_PERIOD = 3;
    private static final int SELL_TREND_PERIOD = 4;
    private static final int BUY_TREND_EXPO = 2;
    private static final int SELL_TREND_EXPO = 2;


    private static final double MIN_BUY_TREND = 4.5;
    private static final double MIN_SELL_TREND = 0.5;
    private static final int MAX_BUY_AMOUNT = 7;
    private static final int MAX_SELL_AMOUNT = 28;

    private Date currentTime;
    private Date lastUpdate;

    /*private void updateAvg(){
        Iterator it = monitor.getMarketWindow().entrySet().iterator();

        while(it.hasNext())
        {
            Map.Entry<String, ArrayDeque<Stock>> entry = (Map.Entry)it.next();

            // Simple weighted average.
            // Oldest to newest diff weighting i^2
            double average = 0.0;
            int weight = 0;
            int i = 1;

            for( Stock s : entry.getValue() )
            {
                average = (i * i) * s.getDiff();
                weight += i * i;
                ++i;
            }

            average /= weight;

            avgWindow.put( entry.getKey(), average  );
            //System.out.println(entry.getKey() + ": " + average);
        }
    }

    private ArrayList< String > getGoodStocks(){
        ArrayList< String > res = new ArrayList<>();

        HashMap<String, ArrayDeque< Stock >> market = monitor.getMarketWindow();

        Iterator it = avgWindow.entrySet().iterator();

        while(it.hasNext())
        {
            Map.Entry<String, Double> entry = (Map.Entry)it.next();

            if(entry.getValue() >= MIN_BUY_TREND && market.get(entry.getKey()).getLast().getDiff() > 0){

                if( system.getStock( entry.getKey() ).getPrice() > 0 ) {
                    res.add(entry.getKey());
                    System.out.println("Good: " + entry.getKey() + " diff: " + market.get(entry.getKey()).getLast().getDiff());
                }
            }
        }

        return res;
    }

    private ArrayList< Share > getBadStocks(){
        ArrayList< Share > res = new ArrayList<>();
        ArrayList< Share > shares = system.getShares( system.getAccount() );

        HashMap<String, ArrayDeque< Stock >> market = monitor.getMarketWindow();

        Iterator it = avgWindow.entrySet().iterator();

        while(it.hasNext())
        {
            Map.Entry<String, Double> entry = (Map.Entry)it.next();

            for( Share sh : shares ){
                if (sh.getStock().getName().equals( entry.getKey() ) ){
                    //System.out.println( entry.getValue() );
                    if( entry.getValue() <= -MIN_SELL_TREND  && market.get(entry.getKey()).getLast().getDiff() < 0){
                        if( system.getStock( entry.getKey() ).getPrice() > 0 ) {
                            res.add(sh);
                            System.out.println("Bad: " + entry.getKey() + " diff: " + market.get(entry.getKey()).getLast().getDiff());
                        }
                    }
                }
            }
        }

        return res;
    }*/

    public Jarvis( SystemFacade f, StockMonitor m) {
        this.system = f;
        //this.monitor = m;

        //avgWindow = new LinkedHashMap<>();

        //for( String s : system.getStockNames() ){
        //    ArrayDeque<Stock> h = system.getStockHistory(s, StockMonitor.MIN_HISTORY).stream().collect(Collectors.toCollection(() -> new ArrayDeque<Stock>()));
        //    market.put( s, h );
        //}

        //updateAvg();
    }

    public void run() {

        system.getUI().printToLog("Jarvis online", false);
        lastUpdate = new Date(new Date().getTime() - system.SERVER_UPDATE_PERIOD);

        while( !isInterrupted() ){

            currentTime = new Date();

            if((currentTime.getTime() - lastUpdate.getTime()) >= system.SERVER_UPDATE_PERIOD )
            {

                lastUpdate = new Date();

                for(String name : getGoodStocks())
                {
                    if(system.getAccount().getBalance() >= system.getStock(name).getPrice() * MAX_BUY_AMOUNT)
                    {
                        system.buy(name, MAX_BUY_AMOUNT);
                    }
                }

                ArrayList<Share> shares = system.getShares(system.getAccount());

                for(String name : getBadStocks())
                {
                    for(Share sh : shares)
                    {
                        if(sh.getStock().getName().equals(name))
                        {
                            system.sell(name, Math.min(sh.getAmount(), MAX_SELL_AMOUNT));
                        }
                    }
                }

                System.out.println("\n\n");

                /*if(monitor.getMarketWindowSize() >= monitor.MIN_HISTORY)
                {

                    updateAvg();

                    //System.out.println("Good: " + getGoodStocks().size());
                    //System.out.println("Bad: " + getBadStocks().size());

                    for (String stockName : getGoodStocks()) {
                        if (system.getAccount().getBalance() >= system.getStock(stockName).getPrice() * MAX_BUY_AMOUNT) {
                            system.buy(stockName, MAX_BUY_AMOUNT);
                        }
                    }

                    for (Share share : getBadStocks()) {
                        system.sell(share.getStock().getName(), Math.min(share.getAmount(), MAX_SELL_AMOUNT));
                    }

                }*/

            }
        }

    }

    private HashMap<String, Double> calculateStockAverages(int period, int exponent)
    {
        HashMap<String, Double> averages = new HashMap<>();
        ArrayList<String> stockNames = system.getStockNames();

        for(String name : stockNames)
        {
            ArrayList<Stock> history = system.getStockHistory(name, period);

            // Simple weighted average.
            // Oldest to newest diff weighting i^expo
            double average = 0.0;
            int weight = 0;
            int i = 1;

            for(Stock s : history)
            {
                average = Math.pow(i, exponent) * s.getDiff();
                weight += Math.pow(i, exponent);
                ++i;
            }

            average /= weight;
            averages.put(name, average);
        }

        return averages;
    }

    private ArrayList<String> getGoodStocks()
    {
        ArrayList<String> good = new ArrayList<String>();

        HashMap<String, Double> averages = calculateStockAverages(BUY_TREND_PERIOD, BUY_TREND_EXPO);

        for(Map.Entry<String, Double> entry : averages.entrySet())
        {
            Stock s = system.getStock(entry.getKey());

            // A stock is GOOD when its trend is above MIN_BUY_TREND, and its latest diff is > 0.
            // > 0 price check added due to Murray's server spitting out negative prices..
            if((entry.getValue() >= MIN_BUY_TREND) && (s.getPrice() > 0) && (s.getDiff() > 0))
            {
                System.out.println("Good:" + entry.getKey() + " avg: " + entry.getValue() + " diff: " + s.getDiff());
                good.add(entry.getKey());
            }
        }

        return good;
    }

    private ArrayList<String> getBadStocks()
    {
        ArrayList<String> bad = new ArrayList<String>();

        HashMap<String, Double> averages = calculateStockAverages(SELL_TREND_PERIOD, SELL_TREND_EXPO);

        for(Map.Entry<String, Double> entry : averages.entrySet())
        {
            Stock s = system.getStock(entry.getKey());

            // A stock is BAD when its trend is below -MIN_SELL_TREND, and its latest diff is < 0.
            // > 0 price check added due to Murray's server spitting out negative prices..
            if((entry.getValue() <= -MIN_SELL_TREND) && (s.getPrice() > 0) && (s.getDiff() < 0))
            {
                System.out.println("Bad:" + entry.getKey() + " avg: " + entry.getValue() + " diff: " + s.getDiff());
                bad.add(entry.getKey());
            }
        }

        return bad;
    }

}
