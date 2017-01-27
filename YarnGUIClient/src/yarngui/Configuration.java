
package yarngui;

import java.io.File;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

/**
 * This class is responsible for maintaining the configuration data used in
 * the networking and general usage of the system.  The configuration data
 * resides in the configuration file associated with this application.  The file 
 * is located and used on startup if possible, if not then default configuration 
 * values are used instead.  Once located the file is scanned and the necessary 
 * data is stored as class variables which are used by other classes as needed.
 * @author Michael Telford
 */
public abstract class Configuration {
    
    // ************************************************************************
    
    /**
     * Welcome message displayed when GUI is shown (Not in config file).
     */
    public static final String WELCOME_MSG =
            "Welcome to Yarn Messenger " + Configuration.VERSION
            + "\nThe cross platform communicator solution!";
    /**
     * Help message displayed to the user when they type -h or -help.
     */
    public static final String HELP_MSG =
            "\nUse the Connect button and address bar to join a Yarn Server\n"
            
            + "\nOnce connected you can use the following commands for help:"
            + "\nType -connections (-c) to display how many clients are connected to the server"
            + "\nType -names (-n) to display the names and statuses of connected users\n"
            
            + "\nType -presence (-p) to update your presence information status :"
            + "\nE.g. -p busy"
            + "\nOnly valid statuses can be used, type '-p statuses' to see the "
            + "full status list\n"
            
            + "\nType -search (-s) to search for a connected client via their username :"
            + "\n-s @username1 @username2"
            + "\nE.g. -s @michael @julie\n"
            
            + "\nTo send a private message to a particular user type :"
            + "\n@username message"
            + "\nE.g. @michael hello michael how are you?\n"
            
            + "\nAlternatively you can use the conference window to safely "
            + "communicate with other clients, simply click the button below. "
            + "Additional help information is available within the "
            + "conference dialog window.\n"
            
            + "\nTo send a data share (file or voice) to a particular user you "
            + "can either use the data share button below or type the command "
            + "manually. The data share commands can be seen having sent a data "
            + "share using the dialog."
            
            + "\n"; // This line break should always be the last character.

    public static final double  VERSION                 = 1.0;
    public static final int     PREVIEW_BUFFER_SIZE     = 500;      // in bytes.
    public static final int     DATA_BUFFER_SIZE        = 65535;    // in bytes.
    public static final int     TRANSMISSION_DELAY      = 10;       // in milli seconds.
    public static final int     DATA_SHARE_SIZE_LIMIT   = 5242880;  // 5MB.

    // Default network configuration details.
    public static boolean       enterSend               = true;
    public static boolean       soundAlert              = true;
    public static int           localPort               = 19895; // Not currently used.
    public static int           serverListeningPort     = 19896;
    public static int           connectTimeout          = 2500;
    
    // Default proxy configuration details.
    public static boolean       useProxy                = false;
    public static String        proxyAddr               = "";
    public static int           proxyPort               = 0;
    public static String        proxyUname              = "";
    public static String        proxyPword              = "";
    
    // Default secure communications (SSL) configuration details.
    public static boolean       useSecureComms          = false;
    public static String        trustStore              = "";
    public static String        trustStorePassword      = "";
    
    // ************************************************************************
    
    // Instance vars (used for the code below).
    private static final String configFilepath = "yarn.client.config.xml";
    private static Document doc = null;
    
    /**
     * This method is responsible for locating and reading the configuration 
     * file.  The values within the file are assigned to public static variables 
     * contained in this class.  These variables are then accessed by other 
     * classes as needed.  If the configuration file cannot be located or a 
     * format error occurs then default configuration values are used instead. 
     * The user is alerted in such cases. 
     */
    public static void readConfigValues(){
        try {
            // Get XML document.
            doc = getXMLDoc();
            
            // Read in node values from config file.
            Node node = doc.getElementsByTagName("enter_send").item(0);
            String temp = node.getTextContent();
            if (temp != null && !temp.isEmpty()){
                if (temp.equalsIgnoreCase("false"))
                    Configuration.enterSend = false;
                else
                    Configuration.enterSend = true;
            }
            
            node = doc.getElementsByTagName("sound_alert").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty()){
                if (temp.equalsIgnoreCase("false"))
                    Configuration.soundAlert = false;
                else
                    Configuration.soundAlert = true;
            }

//            node = doc.getElementsByTagName("local_port").item(0);
//            temp = node.getTextContent();
//            if (temp != null && !temp.isEmpty())
//                Configuration.localPort = Integer.parseInt(temp);

            node = doc.getElementsByTagName("server_listening_port").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.serverListeningPort = Integer.parseInt(temp);

            node = doc.getElementsByTagName("connect_timeout").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.connectTimeout = Integer.parseInt(temp);
            
            // Proxy config values.
            node = doc.getElementsByTagName("use_proxy").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.useProxy = Boolean.parseBoolean(temp);
            
            node = doc.getElementsByTagName("proxy_address").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.proxyAddr = temp;
            
            node = doc.getElementsByTagName("proxy_port").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.proxyPort = Integer.parseInt(temp);
            
            node = doc.getElementsByTagName("proxy_username").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.proxyUname = temp;
            
            node = doc.getElementsByTagName("proxy_password").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.proxyPword = temp;
            
            // SSL config values.
            node = doc.getElementsByTagName("use_ssl").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.useSecureComms = Boolean.parseBoolean(temp);
            
            node = doc.getElementsByTagName("trust_store").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.trustStore = temp;
            
            node = doc.getElementsByTagName("trust_store_password").item(0);
            temp = node.getTextContent();
            if (temp != null && !temp.isEmpty())
                Configuration.trustStorePassword = temp;
        } 
        catch (Exception ex){
            GUI.showUsingDefaultConfigValuesDialog();
        }
    }
    
    /**
     * This method is responsible for writing the configuration variable values 
     * to the XML configuration file when the user applies preference changes. 
     * By writing the changes to the configuration file the applied changes take 
     * effect when the application is restarted at a later time. 
     * @return True if the values have been successfully written to the 
     * configuration file, false otherwise. 
     */
    public static boolean writeConfigValues(){
        try {
            // Get XML document.
            if (doc == null)
                doc = getXMLDoc();
            
            // Get nodes.
            Node config = doc.getElementsByTagName("config").item(0);
            NodeList list = config.getChildNodes();

            // Set config file values.
            for (int i = 0; i < list.getLength(); i++){
                Node node = list.item(i);
                
                // Main network config.
                if ("enter_send".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.enterSend));
                if ("sound_alert".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.soundAlert));
//                if ("local_port".equals(node.getNodeName()))
//                    node.setTextContent(String.valueOf(Configuration.localPort));
                if ("server_listening_port".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.serverListeningPort));
                if ("connect_timeout".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.connectTimeout));
                
                // Proxy config.
                if ("use_proxy".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.useProxy));
                if ("proxy_address".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.proxyAddr));
                if ("proxy_port".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.proxyPort));
                if ("proxy_username".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.proxyUname));
                if ("proxy_password".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.proxyPword));
                
                // SSL config.
                if ("use_ssl".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.useSecureComms));
                if ("trust_store".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.trustStore));
                if ("trust_store_password".equals(node.getNodeName()))
                    node.setTextContent(String.valueOf(Configuration.trustStorePassword));
            }
            
            // Write new values to file.
            TransformerFactory transformerFactory = 
                    TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(configFilepath));
            transformer.transform(source, result);
            
            return true;
        } 
        catch (Exception ex){
            return false;
        }
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
        return docBuilder.parse(configFilepath);
    }
    
    /**
     * This method decides whether or not proxy authentication is required.
     * This is done by checking if a proxy username and password value has been 
     * provided in the XML configuration file.
     * @return True if a value is provided for both the proxy username and 
     * password configuration values.  False otherwise.
     */
    public static boolean useProxyAuthentication(){
        if (!Configuration.useProxy)
            return false;
        if ((proxyUname == null || proxyUname.isEmpty()) ||
             proxyPword == null || proxyPword.isEmpty())
            return false;
        else
            return true;
    }
}