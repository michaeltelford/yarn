
package yarncli;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

/**
 * Connection class for connecting to Yarn servers and providing the ability
 * to send and receive textual data once connected.  Provides a thread for the
 * receiving of data and relies on the Main class for displaying the data to a
 * user via a CLI.
 * @author Michael Telford
 */
public class Connection extends Socket implements Runnable {

    private InputStream in = null;
    private OutputStream out = null;

    public Connection() {
        super();
    }

    public void run(){
        // Create a delay on connection to wait for server response.
        try {
            Thread.sleep(1500);
        }
        catch (InterruptedException ie){
            // Do nothing after delay, continue to receive loop below.
        }
        // Continously tries to receive data once connected.
        while (true){
            try {
                if (this != null && this.isConnected()){
                    String text = this.receive();
                    if (!text.trim().isEmpty())
                        Main.setReceivedText(text);
                }
            }
            catch (NegativeArraySizeException nase){
                Main.setReceivedText("Server has disconnected this client");
                System.exit(0);
            }
            catch (SocketException se){
                Main.setReceivedText("Server has disconnected this client");
                System.exit(0);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    // NOT CURRENTLY USED
    public void bind() throws Exception {

        InetAddress addr = InetAddress.getLocalHost();
        int port = Configuration.localPort;
        SocketAddress sockAddr = new InetSocketAddress(addr, port);

        super.bind(sockAddr);

    }

    public void connect(String address) throws Exception {

        InetAddress addr = InetAddress.getByName(address);
        int port = Configuration.sendToPort;
        SocketAddress sockaddr = new InetSocketAddress(addr, port);

        super.connect(sockaddr, Configuration.connectTimeout);

    }

    public int send(String text) throws Exception {

        if (this.out == null)
            out = this.getOutputStream();

        byte[] data = text.getBytes();
        out.write(data);
        return data.length;

    }

    public String receive() throws Exception {

        if (this.in == null)
            this.in = this.getInputStream();

        byte[] b = new byte[Configuration.maxTextChars];
        // Blocks until data is received
        int numRead = in.read(b, 0, b.length);

        byte[] data = new byte[numRead];
        System.arraycopy(b, 0, data, 0, numRead);
        String text = new String(data);
        return text;

    }
}