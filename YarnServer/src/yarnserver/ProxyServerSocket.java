
package yarnserver;

import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is a sub class of ServerSocket.  As a result this class can be 
 * used to create server socket instance that contains proxy connectivity 
 * support.  If no proxy is provided then the created instance is almost a 
 * complete duplicate of ServerSocket.
 * @author Michael Telford
 */
public class ProxyServerSocket extends ServerSocket {
    
    /**
     * The proxy instance to use by accepting client connections through.
     * The proxy server must obviously be started and accessible.
     */
    private Proxy proxy = null;
    
    /**
     * This classes constructor takes typical ServerSocket constructor parameters 
     * before passing them to the ServerSocket parent class using super().  
     * The proxy parameter is used in the accept() method to accept client 
     * connections through.  
     * @param lisentingPort The port which the server is listening for client 
     * connections on.  The clients must connect to this port.  
     * @param maxConnections The maximum number of simultaneous client 
     * connections for this server.  
     * @param aProxy The proxy instance containing the proxy server information 
     * such as the proxy address and port.  All accepted client connections will 
     * be done so through this proxy instance.  The clients must also contain 
     * a similar proxy instance in their connect code.  
     * @throws Exception If an exception is thrown from the parent ServerSocket 
     * class.
     */
    public ProxyServerSocket(int lisentingPort, 
                             int maxConnections, 
                             Proxy aProxy) throws Exception {
        super(lisentingPort, maxConnections);
        proxy = aProxy;
    }
    
    /**
     * The accept method is overridden from the ServerSocket parent class.  
     * It works the same except the initSocket() method is called before the 
     * actual client connection is accepted.  This allows the proxy configuration 
     * to be used in the accepted connection.  
     * @return An accepted socket instance which is configured to use a proxy 
     * when transmitting data between client and server.  Or if an error occurs 
     * null is returned.  
     */
    @Override
    public Socket accept(){
        try {
            Socket socket = initSocket();
            implAccept(socket);
            return socket;
        } 
        catch (Exception ex){
            //Logger.getLogger(ProxyServerSocket.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * This method is responsible for initialising a socket instance which is 
     * configured to use a proxy as a middle man when transmitting data.  
     * Other socket initialisation takes place such 
     * @return
     * @throws Exception 
     */
    private Socket initSocket() throws Exception {
        Socket socket = new Socket(proxy);
        socket.setKeepAlive(true);
        socket.setSendBufferSize(Configuration.DATA_BUFFER_SIZE);
        socket.setReceiveBufferSize(Configuration.DATA_BUFFER_SIZE);
        return socket;
    }
}