public class Account
{
    private int id;
    private double bal;

    public Account(int userID, double balance)
    {
        id = userID;
        bal = balance;
    }

    public int getId()
    {
        return id;
    }

    public double getBalance()
    {
        return bal;
    }

    public void setBalance(double balance)
    {
        bal = balance;
    }
}
