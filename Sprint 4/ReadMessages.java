import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ReadMessages extends Thread
{
    private BufferedReader in;
    public ConcurrentLinkedQueue<String> responseCache;

    public ReadMessages(Socket conn)
    {
        responseCache = new ConcurrentLinkedQueue<String>();

        try
        {
            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }

        start();
    }

    public void run()
    {
        try
        {
            while( !isInterrupted() ) {
                String temp;
                StringBuilder read = new StringBuilder();

                while ((temp = in.readLine()) != null && !temp.isEmpty()) {
                    read.append(temp);
                    read.append('\n');
                }

                // Remove last newline
                if (read.length() > 1) {
                    read.delete(read.length() - 1, read.length());
                }

                synchronized (this) {
                    responseCache.offer(read.toString());
                    notify();
                }
            }
        }
        catch(SocketException e)
        {
            // Break out of our thread and return control as we've just killed ourselves by force closing the socket.
            return;
        }
        catch( IOException e )
        {
             e.printStackTrace();
        }
    }

    public synchronized String popResponse()
    {
        if( !isAlive() )
        {
            return null;
        }

        try
        {
            this.wait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (responseCache.size() > 0)
        {
            return responseCache.poll();
        }
        else
        {
            return null;
        }
    }
}