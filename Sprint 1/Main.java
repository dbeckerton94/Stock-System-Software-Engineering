

public class Main
{
    public static void main(String[] args)
    {
        ClientSocket clientSocket = new ClientSocket();

        if(clientSocket.open("192.168.0.48", 5000)) {

            System.out.println("Connected to server");

            clientSocket.write("HELO");
            System.out.println(clientSocket.read());

            clientSocket.write("HELP");
            System.out.println(clientSocket.read());

            if (clientSocket.close()) {
                System.out.println("Disconnected from server");
            }
        }
        else
        {
            System.out.println("Failed to connect to server.");
        }
    }
}