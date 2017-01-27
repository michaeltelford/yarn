
package yarngui;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Connection class for connecting to the server and providing the ability
 * to send data once connected.  Starts a thread for the receiving of data once 
 * successfully connected to a server instance.  All corresponding methods 
 * are coded so that both secure and non secure communications are possible. 
 * Secure communications can be initialised in the configuration.  
 * 
 * NOTE FOR DEVELOPERS : Different exceptions can be thrown with different 
 * network configurations e.g. Testing on 'localhost' might not prove the same
 * as testing across a real LAN network in regards to the exceptions being 
 * thrown.  In other words thorough testing is required.  
 * @author Michael Telford
 */
public class Connection {

    private ProxySocket socket = null;
    private SSLSocket sslSocket = null;
    private GUI gui;

    /**
     * Constructor used to initialise the main GUI dialog instance.  
     * @param yarnGUI The calling class which is the main GUI dialog instance.
     */
    public Connection(GUI yarnGUI){
        gui = yarnGUI;
    }
  
    /**
     * Returns the main GUI dialog instance.
     * @return the main GUI dialog instance.
     */
    public GUI getYarnGUI(){
        return gui;
    }
    
    /**
     * This method sets the socket timeout value which decides how long the 
     * receive methods block for before throwing a SocketTimeoutException. 
     * @param timeoutInMilliSeconds The desired socket timeout value.
     */
    public synchronized void setSoTimeout(int timeoutInMilliSeconds){
        try {
            if (Configuration.useSecureComms)
                this.sslSocket.setSoTimeout(timeoutInMilliSeconds);
            else
                this.socket.setSoTimeout(timeoutInMilliSeconds);
        }
        catch (NullPointerException npe){} // Do nothing.
        catch (SocketException ex){
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * This method returns the current socket timeout value.
     * @return The current socket timeout value.
     * @throws SocketException If an underlying socket exception is thrown.
     */
    public int getSoTimeout() throws SocketException {
        int timeout = 0;
        if (Configuration.useSecureComms)
            timeout = this.sslSocket.getSoTimeout();
        else
            timeout = this.socket.getSoTimeout();
        return timeout;
    }
    
    /**
     * This method returns the buffered input stream initialised using the 
     * connected socket.  
     * @return The buffered input stream for the connected socket.
     */
    public BufferedInputStream getBufferedInputStream(){
        BufferedInputStream buf;
        try {
            if (!Configuration.useSecureComms)
                buf = new BufferedInputStream(this.socket.getInputStream(), 
                                                Configuration.DATA_BUFFER_SIZE);
            else
                buf = new BufferedInputStream(this.sslSocket.getInputStream(), 
                                                Configuration.DATA_BUFFER_SIZE);
        } catch (IOException ex) {
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        return buf;
    }

    /**
     * This method returns whether or not the client is currently connected. 
     * @return True if the client is connected, false otherwise.  
     */
    public synchronized boolean isConnected(){
        if (!Configuration.useSecureComms){
            if (this.socket != null && !this.socket.isClosed()
                   && this.socket.isConnected())
                return true;
            else
                return false;
        }
        else {
            if (this.sslSocket != null && !this.sslSocket.isClosed()
                   && this.sslSocket.isConnected())
                return true;
            else
                return false;
        }
    }

    /**
     * This method is responsible for the connection of this client to a server 
     * instance.  Both secure and non secure connections with or without proxy 
     * connectivity is supported.  The server address from the main GUI dialog 
     * is used along with the server port number contained in the configuration 
     * to formulate a server address. Once connected to the server a receive 
     * thread instance is initialised and started (as it is a sub class of 
     * Thread).  If an error occurs the user is alerted through the main GUI 
     * dialog instance.  
     * @param address The server address taken from the main GUI dialog 
     * instance.  
     */
    public synchronized void connect(String address){
        
        address = address.trim();
        SSLSocketFactory sslSocketFactory = null;
        
        // If NOT using secure comms.
        if (!Configuration.useSecureComms){
            
            // Make sure socket is disconnected and null before re/connecting.
            // It always should be, this is a precautionary procedure.
            if (this.socket != null && this.socket.isConnected())
                disconnect();
            if (this.socket != null)
                this.socket = null;
            
            if (Configuration.useProxy){
                // Set up socket to connect to server via proxy address and port.
                InetSocketAddress proxySockAddr = new InetSocketAddress(
                                  Configuration.proxyAddr, Configuration.proxyPort);
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxySockAddr);
                this.socket = new ProxySocket(proxy);
                // Set up proxy authentication if necessary.
                if (Configuration.useProxyAuthentication()){
                    Authenticator.setDefault(new Authenticator(){
                      @Override
                      protected PasswordAuthentication getPasswordAuthentication(){
                          return new PasswordAuthentication(
                              Configuration.proxyUname,
                              Configuration.proxyPword.toCharArray()
                          );
                      }
                    });
                }
            }
            else {
                this.socket = new ProxySocket(Proxy.NO_PROXY);
            }
        }
        
        // Else using secure comms.
        else {
            
            // Make sure socket is disconnected and null before re/connecting.
            // It always should be, this is a precautionary procedure.
            if (this.sslSocket != null && this.sslSocket.isConnected())
                sslDisconnect();
            if (this.sslSocket != null)
                this.sslSocket = null;
            
            // Initialise proxy configuration.
            if (Configuration.useProxy){
                System.setProperty("socksProxyHost", Configuration.proxyAddr);
                System.setProperty("socksProxyPort", String.valueOf(Configuration.proxyPort));
                if (Configuration.useProxyAuthentication()){
                    System.setProperty("java.net.socks.username", Configuration.proxyUname);
                    System.setProperty("java.net.socks.password", String.valueOf(Configuration.proxyPword));
                }
            }
            
            // Initialise SSL configuration.
            System.setProperty("javax.net.ssl.trustStore", Configuration.trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", Configuration.trustStorePassword);
            System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
            
            sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        }

        try {
            
            // If using secure comms.
            if (Configuration.useSecureComms){
                // Connect to server.
                this.sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                                        address, Configuration.serverListeningPort);
            }
            
            // Else not using secure comms.
            else {
                // Init connection vars.
                InetAddress addr = InetAddress.getByName(address);
                int port = Configuration.serverListeningPort;
                SocketAddress sockaddr = new InetSocketAddress(addr, port);

                // Connect to server.
                this.socket.connect(sockaddr, Configuration.connectTimeout);
            }

            // Start the receive thread once connected.
            (new ReceiveThread(this)).start();
            
        }
        catch (UnknownHostException uhe){
            this.gui.setSystemText(String.format(
                    "%s is an unknown host, try again...", 
                    this.gui.serverAddress.getText()));
        }
        catch (Exception ex){
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            this.gui.setSystemText(ex.getMessage());
        }
    }

    /**
     * This method is used to disconnect the client from a server instance.  
     * This method supports non secure disconnects only.  If the 
     * client is already disconnected then this method does nothing.  
     */
    public synchronized void disconnect(){
        
        if (Configuration.useSecureComms){
            this.sslDisconnect();
            return;
        }
        
        try {
            if (this.socket != null){
                if (!this.socket.isClosed())
                    this.socket.close();
            }
        }
        catch (IOException ioe){
            // Do nothing on close() exception.
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ioe);
        }
        finally {
            this.socket = null;
        }
    }
    
    /**
     * This method is used to disconnect the client from a server instance.  
     * This method supports secure disconnects only.  If the 
     * client is already disconnected then this method does nothing.  
     */
    private synchronized void sslDisconnect(){
        
        try {
            if (this.sslSocket != null){
                if (!this.sslSocket.isClosed())
                    this.sslSocket.close();
            }
        }
        catch (IOException ioe){
            // Do nothing on close() exception.
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ioe);
        }
        finally {
            this.sslSocket = null;
        }
    }

    /**
     * This method sends text to the server of which this client is connected. 
     * This method converts the string to bytes and passes the bytes to the 
     * send(byte[]) method.  
     * @param text The text to send to the server and therefore other clients.
     */
    public synchronized void send(String text){
        byte[] data = text.getBytes();
        this.send(data);
    }

    /**
     * This method is responsible for sending data from the client to the 
     * connected server instance.  This method is uniform in that it transmits 
     * byte data rather than data specific types such as text or a file etc. 
     * If a send error occurs then the client is disconnected and the user is 
     * alerted via the main GUI dialog instance.  
     * @param data The byte data to be sent to the server.  
     */
    public synchronized void send(byte[] data){
        try {
            BufferedOutputStream output = null;
            if (!Configuration.useSecureComms)
                output = new BufferedOutputStream(
                        this.socket.getOutputStream(), Configuration.DATA_BUFFER_SIZE);
            else
                output = new BufferedOutputStream(
                        this.sslSocket.getOutputStream(), Configuration.DATA_BUFFER_SIZE);
            
            int bytesSent = 0;
            while (bytesSent < data.length){
                int bytesLeft = (data.length - bytesSent);
                int numBytesToSend = Configuration.DATA_BUFFER_SIZE;
                if (bytesLeft < Configuration.DATA_BUFFER_SIZE)
                    numBytesToSend = bytesLeft;
                
                output.write(data, bytesSent, numBytesToSend);
                output.flush();
                bytesSent += numBytesToSend;
                Utilities.delay(Configuration.TRANSMISSION_DELAY);
            }
        }

        // When an exception is thrown during a send the disconnect button is 
        // clicked automatically, the user is alerted to the fact
        // that they need to re/connect to a Yarn Server.  This should only be
        // necessary when the server has disconnected the client or some other
        // form of exception is thrown.
        catch (IOException ex) {
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            this.gui.disconnectDoClick();
            this.gui.setSystemText("You are not connected to a Yarn "
                    + "Server, please re/connect");
        }
    }
    
    /**
     * This method is responsible for the sending of a data share request and 
     * data to the server for further processing.  The request is send along 
     * with the data which is received by the server, the server then sends the 
     * data share request to the receiving client(s).  If accepted the share 
     * data is then sent to the client(s).  Once the this method executes this 
     * client is no longer involved in the data share process except by 
     * receiving updates as to whether or not the client(s) have accepted or 
     * rejected the data share.  Before anything is sent to the server however 
     * data share validation occurs.  This includes such checks as ensuring that 
     * the data size is within the legal limit etc.  
     * @param text The protocol command used for the data share.  
     */
    public void sendShareRequestAndData(String text){
        
        // Check there is a username with a space and message after it.
        String filepath = "";
        boolean result = Utilities.hasMessageAfterLastUsername(text);
        if (!result){
            this.gui.setSystemText("No message found after the username, try again...");
            return;
        }
        
        // Check there is a valid filename and extension.
        try {
            boolean isAFileShare = Utilities.isAFileShare(text);
            filepath = Utilities.validateFilePath(text, isAFileShare);
        } catch (Exception ex){
            this.gui.setSystemText(ex.getMessage());
            return;
        }
        
        // The illegal file extension validation is performed by the server only.
        
        // Ensure the file exists on the clients machine.
        File file = new File(filepath);
        if (!file.isFile()){
            this.gui.setSystemText("File error, check the file exists and try again...");
            return;
        }
        
        // Add the file size to the command.
        text += ("?\"" + file.length() + "\"");
        
        // Ensure the file isn't too large with the file share send command.
        if ((file.length() + text.length()) > Configuration.DATA_SHARE_SIZE_LIMIT){
            this.gui.setSystemText(String.format("File error, the file is too "
                    + "large. The maximum file size is %s Bytes", 
                    Configuration.DATA_SHARE_SIZE_LIMIT));
            return;
        }
        
        // Add the file bytes to the file share send command.
        byte[] data = new byte[(int)(file.length() + text.length())];
        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            //Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        
        System.arraycopy(text.getBytes(), 0, data, 0, text.getBytes().length);
        System.arraycopy(fileBytes, 0, data, text.getBytes().length, fileBytes.length);
        
        // Send the file share request command to the server (with the file data).
        this.send(data);
        
        String uname = text.substring(1, text.indexOf(' '));
        this.gui.setSystemText("File share request sent to " + uname);
    }
}