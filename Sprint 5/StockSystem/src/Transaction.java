import java.sql.Timestamp;

/// TODO
// rename this to Transaction because we're retarded.
public class Transaction
{
    private String name;
    private double price;
    private int amount;
    private Timestamp time;

    public Transaction(String stockName, double transactionPrice, int transactionQuantity, Timestamp transactionTime)
    {
        name = stockName;
        price = transactionPrice;
        amount = transactionQuantity;
        time = transactionTime;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getAmount() {
        return amount;
    }

    public Timestamp getTime() {
        return time;
    }

    public String toString()
    {
        return "Stock Name: " + name + "\t Transaction Price: " + price + "\tTransaction Time: " + time + "\tAmount: " + amount;
    }
}