import java.sql.Timestamp;

public class Stock
{
    private String name;
    private double price;
    private double diff;
    private Timestamp time;

    public Stock(String stockName, double stockPrice, double stockDiff, Timestamp stockTime)
    {
        name = stockName;
        price = stockPrice;
        diff = stockDiff;
        time = stockTime;
    }

    String getName()
    {
        return name;
    }

    double getPrice()
    {
        return price;
    }

    double getDiff()
    {
        return diff;
    }

    Timestamp getTime()
    {
        return time;
    }

    public String toString()
    {
        return "Name: " + name + "\t Price: " + price + "\t Diff: " + diff + "\t Time: " + time;
    }
}