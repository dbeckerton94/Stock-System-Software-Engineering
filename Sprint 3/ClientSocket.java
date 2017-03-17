import java.io.IOException;
import java.net.Socket;

public class ClientSocket
{

    private Socket conn;
    private String ip;
    private int port;

    public ClientSocket(String ip, int port)
    {
        this.ip = ip;
        this.port = port;
    }

    public Boolean open()
    {
        try
        {
            conn = new Socket(ip, port);

            return true;
        }
        catch( IOException e )
        {
            //e.printStackTrace();
            return false;
        }

    }

    public Boolean close()
    {
        try
        {
            conn.close();

            return true;

        } catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public Socket getSocket()
    {
        return conn;
    }
}
