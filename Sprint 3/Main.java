import java.io.IOException;

public class Main
{
    public static void main(String[] args)
    {
		StockHelper stockHelper = new StockHelper();
		
        if(stockHelper.connect())
        {
            stockHelper.print("\r\nAvailable commands (key): \r\n" +
                    "Display the stocks (d)\r\n" +
                    "Buy some stocks (b)\r\n" +
                    "Sell some stocks (s)\r\n" +
                    "Exit the program (q)");

            while (true)
            {
				String command = stockHelper.readInput();

				if (command.equals("Q"))
				{
					stockHelper.exit();
					break;
				}
				
				// once the system is automated this becomes unnecessary
				switch (command)
				{
					case "B":
                        /*while (true) {
                            stockHelper.buy("Google", 100000);
                            stockHelper.buy("Microsoft", 100000);
                            stockHelper.buy("IBM", 100000);
                            stockHelper.buy("GSK", 100000);
                            stockHelper.buy("BT", 100000);
                            stockHelper.buy("TALKTALK", 100000);
                            stockHelper.buy("ITV PLC", 100000);
                            stockHelper.buy("APPLE", 100000);
                            stockHelper.buy("EA", 100000);
                            stockHelper.buy("TESCO", 100000);
                        }*/
                        stockHelper.buy("Google", 1);
                        break;
					case "S":
					stockHelper.sell("Google", 1);
						break;
					case "D":
					stockHelper.getStocks();
						break;
					default:
						//invalid command, ignore
				}
            }
			
			stockHelper.disconnect();
        }

    }
}