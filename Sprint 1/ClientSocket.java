import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientSocket {

    // Private members for holding connection information.
    // Should only access these through public methods of Client Socket.
    private Socket conn;
    private BufferedReader in;
    private PrintWriter out;

    // Opens a socket to the given ip on the given port.
    // Returns a boolean of whether it's connected.
    public Boolean open(String ip, int port)
    {
        try
        {
            conn = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            out = new PrintWriter(conn.getOutputStream(), true);

            return true;

        } catch(IOException e)
        {
            System.out.println(e.toString());
            return false;
        }
    }

    // Closes the socket, returning successful.
    public Boolean close()
    {
        try
        {
            conn.close();

            return true;

        } catch(IOException e)
        {
            System.out.println(e.toString());
            return false;
        }
    }

    // Write a string to the socket using the PrintWriter.
    public Boolean write(String msg)
    {
        try
        {
            out.println(msg);

            return true;

        } catch (Exception e)
        {
            System.out.println(e.toString());
            return false;
        }
    }

    // Read a message from the server, from the BufferedReader.
    public String read()
    {
        try
        {
            return in.readLine();

        } catch (IOException e)
        {
            System.out.println(e.toString());
            return null;
        }
    }

}
