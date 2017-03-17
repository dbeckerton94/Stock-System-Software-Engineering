import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main
{
    public static void main(String[] args)
    {
        ClientSocket clientSocket = new ClientSocket( "10.18.8.252", 5000 );
        //ClientSocket clientSocket = new ClientSocket( "inp.io", 5000 );
        // 10.18.8.199

        if(clientSocket.open())
        {

            System.out.println("Connected to server.");

            SendMessages sender = new SendMessages(clientSocket.getSocket());
            ReadMessages receiver = new ReadMessages(clientSocket.getSocket());

            boolean flag = true;

            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            while (flag && receiver.isAlive())
            {
                try
                {
                    String command = br.readLine();

                    if (command.equals("exit") || command.equals("EXIT"))
                    {
                        flag = false;
                    }

                    sender.send(command);
                    System.out.println(receiver.popResponse());
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            if (receiver.isAlive())
            {
                receiver.interrupt();
            }

            clientSocket.close();

        }
        else
        {
            System.out.println("Failed to connect to server.");
        }

    }
}