import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;

// TODO: IMPLEMENT ERROR HANDLING
public class StockHelper
{
	ClientSocket clientSocket;
	SendMessages sender; // send data to server
	ReadMessages receiver; // receive from server
	BufferedReader reader; // read user input
	private int STOCK_ID = 0;
	
	// Creates socket with default settings
	public StockHelper()
	{
		clientSocket = new ClientSocket( "192.168.0.48", 5000 );
		//clientSocket = new ClientSocket( "inp.io", 5000 );
        // 10.18.8.199
		reader = new BufferedReader(new InputStreamReader(System.in));
	}
	
	public boolean connect() 
	{

        if (clientSocket.open()) {
            print("Connected to server.");

            // Create input and output
            sender = new SendMessages(clientSocket.getSocket());
            receiver = new ReadMessages(clientSocket.getSocket());

            register(); // auto register to server

            return true;
        }

        print("Failed to connect to server.");

		return false;
	}
	
	public boolean disconnect()
	{
		if (receiver.isAlive())
		{
			receiver.interrupt();
		}
		
		return clientSocket.close();
	}
	
	public void exit()
	{
		sendCommand("EXIT");
		String response = receiver.popResponse();
		print(response);
	}
	
	public boolean register()
	{
		// REGI RESPONSE FORMAT REGI:SUCCESS:ID
		sendCommand("REGI");

		String[] output = splitResponse(receiver.popResponse());
		
		if (output[1].equals("SUCCESS"))
		{
			// saving the ID
			print("Registered to Stock System with ID: " + output[2]);
			STOCK_ID = Integer.parseInt(output[2]);

			//sendCommand("HELP");
            //print(receiver.popResponse());
			return true;
		}
		return false;
	}
	
	public boolean buy(String company, int amount)
	{
		sendCommand("BUY:" + company + ":" + amount + ":" + STOCK_ID);
        print(receiver.popResponse());
		return true;
	}
	
	public boolean sell(String company, int amount)
	{
        sendCommand("SELL:" + company + ":" + amount + ":" + STOCK_ID);
        print(receiver.popResponse());
		return true;
	}
	
	public void getStocks()
	{
		sendCommand("DISP:" + STOCK_ID); // this might require ID
		print(receiver.popResponse());
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
		return ""; // TODO: Not sure what else to put here
	}
	
	protected void sendCommand(String command)
	{
		sender.send(command);
	}
	

}