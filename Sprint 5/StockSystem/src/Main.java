import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

// TODO
// Stock helper class needs re-doing a little. REMOVE reading from console from this!! It's a UI thing so needs putting in your separate UI class.
// Also needs modifying when Murray updates server to actually buy and sell!

public class Main
{
    public static void main(String[] args)
    {
		SystemFacade system = new SystemFacade();
		// initialise UI with reference to system facade

		// Setup / start
		if(system.start())
		{
			// Get share names
			/*ArrayList<String> names = system.getStockNames();

			// Buy shares
			boolean brought = system.buy("google", 1000);

			// Sell shares
			boolean sold = system.sell("itv plc", 1000);

			// Get stock history for google between two times; this gets all history up until now
			ArrayList<Stock> stockHistory = system.getStockHistory("google", new Timestamp(0), new Timestamp(new Date().getTime()));

			// Get an individual stock information at a certain time (now in this case)
			Stock googleStock = system.getStock("google", new Timestamp(new Date().getTime()));

			// Access the account
			Account acc = system.getAccount();

			// Get a list of transactions
			ArrayList<Transaction> transactionHistory = system.getTransactionHistory(acc, new Timestamp(0), new Timestamp(new Date().getTime()));

			// Quit
			system.quit();*/

		}

		//while (system.alive) {}
    }
}