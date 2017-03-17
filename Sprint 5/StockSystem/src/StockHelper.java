import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

public class StockHelper
{
	ClientSocket clientSocket;
	Account acc;
	SendMessages sender; // send data to server
	ReadMessages receiver; // receive from server
	BufferedReader reader; // read user input
	
	// Creates socket
	public StockHelper(String ip, int port)
	{
		clientSocket = new ClientSocket(ip, port);
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public boolean connect() 
	{
        if (clientSocket.open()) {
            print("Connected to server.");

            // Create input and output
            sender = new SendMessages(clientSocket.getSocket());
            receiver = new ReadMessages(clientSocket.getSocket());

            return true;
        }

        print("Failed to connect to server.");

		return false;
	}
	
	public boolean disconnect()
	{
		if(clientSocket.close())
		{
			System.out.println("Disconnected from stock server.");
			return true;
		}
		else
		{
			System.out.println("Could not disconnect from stock server.");
			return false;
		}
	}
	
	public void exit()
	{
		sendCommand("EXIT");
		String response = receiver.popResponse();
		print(response);
	}
	
	public Account register()
	{
		// REGI RESPONSE FORMAT REGI:SUCCESS:ID
		sendCommand("REGI");

		String[] output = splitResponse(receiver.popResponse());
		
		if (output[1].equals("SUCCESS"))
		{
			// saving the ID
			int id = Integer.parseInt(output[2]);

			print("Registered to Stock System with ID: " + id);

			acc	= new Account(id, getBalanceFromServer(id));

			return acc;
		}

		return new Account(-1, 0.0);
	}

	public Account getAccount()
	{
		return acc;
	}

	private double getBalanceFromServer(int id)
	{
		sendCommand("CASH:" + id);

		String[] output = splitResponse(receiver.popResponse());

		return Double.valueOf(output[2]);
	}
	
	public boolean buy(Stock stock, int amount)
	{
		Double total = stock.getPrice() * amount;

		if(acc.getBalance() >= total)
		{
			sendCommand("BUY:" + stock.getName() + ":" + amount + ":" + acc.getId());

			System.out.println(receiver.popResponse());

			// TODO
			// Check if actually bought when implemented
			acc.setBalance(acc.getBalance() - total);

			return true;
		}

		return false;
	}
	
	public boolean sell(Stock stock, int amount)
	{
        sendCommand("SELL:" + stock.getName() + ":" + amount + ":" + acc.getId());

		System.out.println(receiver.popResponse());

		// TODO
		// Check if we can actually sell stock when its implemented
		Double total = stock.getPrice() * amount;
		acc.setBalance(acc.getBalance() + total);

		return true;
	}
	
	public ArrayList<Stock> getStocks()
	{
		sendCommand("DISP:" + acc.getId());

		ArrayList<Stock> stocks = new ArrayList<Stock>();

		String[] stockRows = receiver.popResponse().split("\n");

		for(int i = 0; i < stockRows.length; ++i)
		{
			if(stockRows[i].startsWith("STK:"))
			{
				String[] stockData = stockRows[i].split(":");
				stocks.add(new Stock(stockData[1], Double.parseDouble(stockData[2]), Double.parseDouble(stockData[3]), new Timestamp(new Date().getTime())));
			}
		}

		return stocks;
	}
	
	// if main ever has to check if the connection is alive
	public boolean getConnectionStatus()
	{
		return receiver.isAlive();
	}
	
	// Say you have to construct a command, e.g. BUY:COMPANY:AMOUNT:ID
	// both buy/sell would use similar methods, so that code should go here
	// also should append the ID to relevant commands
	public String formatNextCommand(String[] args)
	{
		return "";
	}

    private String[] splitResponse(String response) { return response.split(":"); }
	
	public void print(String output)
	{
		if (output != null && !output.isEmpty())
		{
			System.out.println(output);
		}
	}
	
	public String readInput()
	{
		try 
		{
			return reader.readLine().toUpperCase();
		} 
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		return "";
	}
	
	protected void sendCommand(String command)
	{
		sender.send(command);
	}
	

}