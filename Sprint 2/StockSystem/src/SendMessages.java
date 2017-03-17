import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class SendMessages {

    PrintWriter out;

    public SendMessages(Socket conn)
    {
        try
        {
            out = new PrintWriter( conn.getOutputStream(), true);
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }

    public synchronized void send(String msg)
    {
        if( !out.checkError() ) {
            out.println(msg);
        }

    }


}
