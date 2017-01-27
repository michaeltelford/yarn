
package yarngui;

import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class provides a sub class of socket to enable proxy support.  Although 
 * proxy support is possible without creating a socket sub class, adopting this 
 * approach enables additional socket configuration to be initialised.  
 * @author Michael Telford
 */
public class ProxySocket extends Socket {
    
    /**
     * Constructor used to initialise the super class (socket) with the proxy 
     * parameter provided.  Once initialised the proxy socket has other desired 
     * network settings applied.  
     * @param proxy The proxy instance to use for this client.  Can be 
     * Proxy.NO_PROXY if the use of a proxy is not desired.  
     */
    public ProxySocket(Proxy proxy){
        super(proxy);
        initYarnSocket();
    }
    
    /**
     * Method which is called from the constructor and is used to further 
     * configure the proxy socket before allowing use of the returned instance. 
     * One of the most important configurations is the setting of the send and 
     * receive buffer sizes.  
     */
    private void initYarnSocket(){
        try {
            // Bind code.
            this.setSoLinger(false, 1);
            this.setReuseAddress(true);
            // Connection code.
            this.setKeepAlive(true);
            this.setSendBufferSize(Configuration.DATA_BUFFER_SIZE);
            this.setReceiveBufferSize(Configuration.DATA_BUFFER_SIZE);
        } 
        catch (SocketException ex) {
            //Logger.getLogger(ProxySocket.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}