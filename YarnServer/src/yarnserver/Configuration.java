
package yarnserver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * This class is used to contain configuration details for other classes in the
 * form of static class variables.  The load and print configuration methods
 * are included in this class.  This class is abstract meaning that no class 
 * instance is created.  All public variables and methods are therefore static.
 * @author Michael Telford
 */
public abstract class Configuration {

    public  static final double  VERSION                        = 1.0;
    public  static final int     PREVIEW_BUFFER_SIZE            = 500;      // in bytes.
    public  static final int     DATA_BUFFER_SIZE               = 65535;    // in bytes.
    public  static final int     TRANSMISSION_DELAY             = 10;       // in milli seconds.
    public  static final int     DATA_SHARE_SIZE_LIMIT          = 5242880;  // 5MB.
    private static final int     CONNECTION_REQUEST_DELAY       = 15;       // in seconds.
    private static final double  CONNECTION_RESPONSE_TIMEOUT    = 2.5;      // in seconds.
    private static final int     DATA_SHARE_RESPONSE_TIMEOUT    = 300;      // in seconds.
    
    public  static String[]      illegalUsernames               = null;
    public  static String[]      illegalFileExtensions          = null;
    public  static String[]      legalStatuses                  = null;

    // Configuration details.
    public  static String        welcomeMessage;
    public  static String        serverShutdownCommand;
    public  static String        serverPassword;
    public  static int           usernameAndPasswordCharLimit;
    public  static int           connectionLimit;
    public  static int           serverListeningPort;
    
    // Default proxy configuration details.
    public  static boolean       useProxy                       = false;
    public  static String        proxyAddr                      = "";
    public  static int           proxyPort                      = 0;
    public  static String        proxyUname                     = "";
    public  static String        proxyPword                     = "";
    
    // Default secure communications details.
    public  static boolean       useSecureComms                 = false;
    public  static String        keyStore                       = "";
    public  static String        keyStorePassword               = "";

    /*************************************************************************/
    
    private static Document      doc                            = null;
    private static String        configFileName                 = "yarn.server.config.xml";
    private static String        hostname;
    private static String        ipAddr;

    /**
     * This method is responsible for reading the XML configuration file and its
     * corresponding values.  The values are assigned to their corresponding 
     * variables in this class.  Other classes access these variables where 
     * necessary.
     * 
     * If the XML configuration file cannot be found then a message is printed 
     * out to the CLI and the system exits.
     * @throws Exception If a XML read error occurs.
     */
    public static void loadConfigDetails() throws Exception {

        String configFilePath = String.format("%s%s%s",
                                System.getProperty("user.dir"),
                                System.getProperty("file.separator"), 
                                Configuration.configFileName);
        System.out.println(String.format("Using config file '%s'", configFilePath));

        try {
            // Get XML document.
            doc = getXMLDoc();
            
            // Read in node values from config file.
            Node node = doc.getElementsByTagName("welcome_message").item(0);
            Configuration.welcomeMessage = node.getTextContent();
            // Replace --version-- with version number where necessary.
            Configuration.welcomeMessage = Configuration.welcomeMessage.replaceAll(
                    "--version--", String.valueOf(Configuration.VERSION));
            
            node = doc.getElementsByTagName("username_and_password_char_limit").item(0);
            Configuration.usernameAndPasswordCharLimit = Integer.parseInt(node.getTextContent());

            node = doc.getElementsByTagName("server_password").item(0);
            Configuration.serverPassword = node.getTextContent();
			if (Configuration.serverPassword.length() > Configuration.usernameAndPasswordCharLimit){
                System.out.println("The server password length exceeded the character limit (" 
                                  + Configuration.usernameAndPasswordCharLimit + "), exiting...");
                System.exit(1);
            }
            
            node = doc.getElementsByTagName("server_shutdown_command").item(0);
            Configuration.serverShutdownCommand = node.getTextContent();
            
            node = doc.getElementsByTagName("connection_limit").item(0);
            Configuration.connectionLimit = Integer.parseInt(node.getTextContent());
            
            node = doc.getElementsByTagName("server_listening_port").item(0);
            Configuration.serverListeningPort = Integer.parseInt(node.getTextContent());
            
            // Proxy config values.
            node = doc.getElementsByTagName("use_proxy").item(0);
            Configuration.useProxy = Boolean.parseBoolean(node.getTextContent());
            
            node = doc.getElementsByTagName("proxy_address").item(0);
            Configuration.proxyAddr = node.getTextContent();
            
            node = doc.getElementsByTagName("proxy_port").item(0);
            if (!node.getTextContent().isEmpty())
                Configuration.proxyPort = Integer.parseInt(node.getTextContent());
            
            node = doc.getElementsByTagName("proxy_username").item(0);
            Configuration.proxyUname = node.getTextContent();
            
            node = doc.getElementsByTagName("proxy_password").item(0);
            Configuration.proxyPword = node.getTextContent();
            
            // SSL config values.
            node = doc.getElementsByTagName("use_ssl").item(0);
            Configuration.useSecureComms = Boolean.parseBoolean(node.getTextContent());
            
            node = doc.getElementsByTagName("key_store").item(0);
            Configuration.keyStore = node.getTextContent();
            
            node = doc.getElementsByTagName("key_store_password").item(0);
            Configuration.keyStorePassword = node.getTextContent();
            
            // Get local hostname and IP address.
            Configuration.hostname  = InetAddress.getLocalHost().getHostName();
            Configuration.ipAddr    = 
                 InetAddress.getByName(Configuration.hostname).getHostAddress();
        }
        catch (UnknownHostException e){
            Configuration.hostname  = "-1"; // Can't obtain hostname.
            Configuration.ipAddr    = "";
        }
        catch (Exception ex) {
            System.out.println("XML format error, exiting...");
            System.exit(0);
        }
    }

    /**
     * This method prints this classes configuration variables out to the CLI. 
     * This method should be called after loadConfigDetails().
     */
    public static void printConfigDetails(){

        // Print Config details.
        System.out.println();
        System.out.println("WELCOME_MSG: ");
        System.out.println(Configuration.welcomeMessage);
        System.out.print("USERNAME_AND_PASSWORD_CHAR_LIMIT: ");
        System.out.println(Configuration.usernameAndPasswordCharLimit);
        System.out.print("SERVER_PASSWORD: ");
        for (int i = 0; i < Configuration.serverPassword.length(); i++)
            System.out.print("*");
        System.out.println();
        System.out.println("SERVER_SHUTDOWN_COMMAND: ");
        System.out.println(Configuration.serverShutdownCommand);
        System.out.print("CONNECTION_LIMIT: ");
        System.out.println(Configuration.connectionLimit);
        System.out.print("SERVER_LISTENING_PORT: ");
        System.out.println(Configuration.serverListeningPort);
        
        // Print proxy details.
        System.out.print("USE_PROXY: ");
        System.out.println(Configuration.useProxy);
        if (Configuration.useProxy){
            System.out.print("PROXY_ADDRESS: ");
            System.out.println(Configuration.proxyAddr);
            System.out.print("PROXY_PORT: ");
            System.out.println(Configuration.proxyPort);
            System.out.print("PROXY_USERNAME: ");
            System.out.println(Configuration.proxyUname);
            System.out.print("PROXY_PASSWORD: ");
            for (int i = 0; i < Configuration.proxyPword.length(); i++)
                System.out.print("*");
            System.out.println();
        }
        
        // Print SSL details.
        System.out.print("USE_SSL: ");
        System.out.println(Configuration.useSecureComms);
        if (Configuration.useSecureComms){
            System.out.print("KEY_STORE: ");
            System.out.println(Configuration.keyStore);
            System.out.print("KEY_STORE_PASSWORD: ");
            for (int i = 0; i < Configuration.keyStorePassword.length(); i++)
                System.out.print("*");
            System.out.println();
        }
        System.out.println();
        
        // Print local hostname and IP address.
        if (Configuration.hostname.equalsIgnoreCase("-1"))
            System.out.println("Local hostname cannot be obtained...");
        else
            System.out.println(String.format(
                   "This servers local hostname (clients connect to) : %s [%s]", 
                   Configuration.hostname, Configuration.ipAddr));
        System.out.println();
    }
    
    /**
     * This method locates the XML configuration file and returns a Document 
     * object to manipulate the values it contains.
     * @return The configuration file XML document object.
     * @throws Exception If a file read error occurs.
     */
    private static Document getXMLDoc() throws Exception {
        DocumentBuilderFactory docFactory = 
                DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        return docBuilder.parse(Configuration.configFileName);
    }
    
    /**
     * This method returns whether the server password value has been set in the 
     * configuration file.
     * @return True if there is a server password value provided in the 
     * configuration file.  False otherwise.
     */
    public static boolean isTheServerPasswordSet(){
        if (Configuration.serverPassword.equals("")) 
            return false;
        else 
            return true;
    }
    
    /**
     * This method decides whether or not proxy authentication is required.
     * This is done by checking if a proxy username and password value has been 
     * provided in the XML configuration file.
     * @return True if a value is provided for both the proxy username and 
     * password configuration values.  False otherwise.
     */
    public static boolean useProxyAuthentication(){
        if ((proxyUname == null || proxyUname.isEmpty()) ||
             proxyPword == null || proxyPword.isEmpty())
            return false;
        else
            return true;
    }
    
    /**
     * Returns the CONNECTION_REQUEST_DELAY variable value in milli seconds.
     * @return The CONNECTION_REQUEST_DELAY variable value in milli seconds.
     */
    public static int getConnectionRequestDelayInMilliSeconds(){
        double d = (Configuration.CONNECTION_REQUEST_DELAY * 1000);
        return (int) d;
    }
    
    /**
     * Returns the CONNECTION_REQUEST_DELAY variable value in seconds.
     * @return The CONNECTION_REQUEST_DELAY variable value in seconds.
     */
    public static int getConnectionRequestDelayInSeconds(){
        return Configuration.CONNECTION_REQUEST_DELAY;
    }
    
    /**
     * Returns the CONNECTION_RESPONSE_TIMEOUT variable value in milliseconds.
     * @return The CONNECTION_RESPONSE_TIMEOUT variable value in milliseconds.
     */
    public static int getConnectionResponseTimeoutInMilliSeconds(){
        double d = (Configuration.CONNECTION_RESPONSE_TIMEOUT * 1000);
        return (int) d;
    }
    
    /**
     * Returns the DATA_SHARE_RESPONSE_TIMEOUT variable value in milliseconds.
     * @return The DATA_SHARE_RESPONSE_TIMEOUT variable value in milliseconds.
     */
    public static int getDataShareResponseTimeoutInMilliSeconds(){
        double d = (Configuration.DATA_SHARE_RESPONSE_TIMEOUT * 1000);
        return (int) d;
    }
}