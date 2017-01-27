
package yarnserver;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

/**
 * Main class for the TCP Server CLI application.  Listens on a given port and
 * spawns a new ClientConnection object for each connecting client.  That
 * instance is then started on its own thread which is used for sending and
 * receiving textual data and for the checking of its connection status.
 * @author Micky Telford
 */
public class Main {
    
    /**
     * The client connection array is used to store client connections once 
     * accepted.  This variable is public and statically accessible from this 
     * class.  Other classes access this variable when interacting with client 
     * connections.  
     */
    public static ClientConnection[] connections = null;

    /**
     * Main method which is responsible for listening for and accepting incoming 
     * client connections in a continuous loop.  This class also starts the 
     * server application, initialises the configuration data, log file and 
     * system variables such as the client connection and server socket for 
     * either secure or insecure communications.  
     * No arguments are needed or used.  
     * @param args the command line arguments which are not needed or used.  
     */
    public static void main(String[] args) throws Exception {

        try {
            
            System.out.println("\nWelcome to Yarn Server " + Configuration.VERSION);
            
            // Init configuration data.
            System.out.println("Initialising system variables...");
            Configuration.loadConfigDetails();
            Configuration.printConfigDetails();
            
            // Init necessary arrays.
            Utilities.initIllegalUnames();
            Utilities.initIllegalFileExtentions();
            Utilities.initLegalStatuses();

            // Init log data.
            System.out.println("Initializing server log...");
            Log.initLog();
            Log.logServerStart();
            
            Main.connections = new ClientConnection[Configuration.connectionLimit];
            ProxyServerSocket server = null;
            SSLServerSocket sslServerSocket = null;
            
            // If NOT using secure comms.
            if (!Configuration.useSecureComms){
                
                // Init proxy for use with accepted sockets.
                Proxy proxy;
                if (Configuration.useProxy){
                    InetSocketAddress proxySockAddr = new InetSocketAddress(
                        Configuration.proxyAddr, Configuration.proxyPort);
                    proxy = new Proxy(Proxy.Type.SOCKS, proxySockAddr);
                }
                else
                    proxy = Proxy.NO_PROXY;

                // Set up the proxy to use authentication if necessary.
                if (Configuration.useProxy && Configuration.useProxyAuthentication()){
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

                // Init variables for connection loop.
                server = new ProxyServerSocket(
                                            Configuration.serverListeningPort,
                                            Configuration.connectionLimit,
                                            proxy);
            }
            
            // Else if using secure comms.
            else {
                
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
                System.setProperty("javax.net.ssl.keyStore", Configuration.keyStore);
                System.setProperty("javax.net.ssl.keyStorePassword", Configuration.keyStorePassword);
                System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");

                SSLServerSocketFactory sslServerSocketFactory =
                        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                sslServerSocket =
                        (SSLServerSocket) sslServerSocketFactory.createServerSocket(
                        Configuration.serverListeningPort, Configuration.connectionLimit);
            }

            System.out.println("SERVER IS RUNNING...");

            // Enters continuous client connection loop.
            while (true){
                try {
                    // Find a null instance in array.
                    for (int i = 0; i < Main.connections.length; i++){
                        if (Main.connections[i] == null){
                            
                            // Accept connection on request.
                            Socket socket = null;
                            SSLSocket sslSocket = null;
                            
                            // If NOT using secure comms.
                            if (!Configuration.useSecureComms){
                                socket = server.accept(); // Blocks.
                                Main.connections[i] = new ClientConnection(socket);
                            }
                            
                            // Else if using secure comms.
                            else {
                                sslSocket = (SSLSocket) sslServerSocket.accept(); // Blocks.
                                Main.connections[i] = new ClientConnection(sslSocket);
                            }
                            
                            // Init and start connection on its own thread.
                            Main.connections[i].setName("Client Connection Thread " + i);
                            Main.connections[i].setDaemon(true);
                            Main.connections[i].start();
                            
                            // Re-enters client connection loop.
                            break;
                        }
                    } // Re-enters loop for next client connection.
                }
                catch (Exception ex){
                    // All exceptions within loop result in a line being printed
                    // on CLI and then continuation of the connection loop.
                    //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        catch (Exception ex){ // Initialization exceptions.
            throw ex;
        }
    }
}