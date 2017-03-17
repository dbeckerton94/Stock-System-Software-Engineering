public class Share
{
    private Stock stock;
    private int amount;

    public Share(Stock stock, int amount)
    {
        this.stock = stock;
        this.amount = amount;
    }

    public Stock getStock() {
        return stock;
    }

    public int getAmount() {
        return amount;
    }
}
