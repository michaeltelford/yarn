
package yarnserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Log class for the logging of text to a file in the application directory.  
 * This class is abstract, therefore all public methods are static.  
 * @author Michael Telford
 */
public abstract class Log {
    
    private static String logFileName = "yarn.server.log.txt";
    private static boolean isLogInit  = false;
    private static PrintWriter output = null;
    
    // No constructor used due to class being abstract.
    
    /**
     * This method initialises the log file and must be called before any text 
     * log methods are called.  If the log file cannot be found then a message 
     * is printed to the CLI.  The server application does not require a log 
     * file to operate.  
     */
    public static void initLog(){
        String logFilePath = String.format("%s%s%s",
                    System.getProperty("user.dir"), 
                    System.getProperty("file.separator"), logFileName);
        try {
            FileWriter fstream = new FileWriter(logFileName, true);
            BufferedWriter buffer = new BufferedWriter(fstream);
            output = new PrintWriter(buffer);
            isLogInit = true;
            System.out.println(String.format("Using log file '%s'\n",
                    logFilePath));
        }
        catch (IOException ioe){
            String exMsg = "Log file '" + logFilePath + "' could not be "
                    + "opened, server will run regardless...\n";
            System.out.println(exMsg);
        }
    }
    
    /**
     * Log text detailing the server startup.
     * This method returns if the log file has not been initialised.  
     */
    public static void logServerStart(){
        if (!isLogInit)
            return;
        output.println();
        output.println("-----------------------------------------------------");
        logLineOfText("server started");
        output.println();
        output.flush();
    }
    
    /**
     * Log text detailing the server being stopped.
     * This method returns if the log file has not been initialised.  
     */
    public static void logServerStop(String uname){
        if (!isLogInit)
            return;
        output.println();
        logLineOfText(
                "server stopped by @" + uname + " using shutdown command '" +
                Configuration.serverShutdownCommand + "'");
        output.close();
    }
    
    /**
     * Log text detailing a client connection.
     * This method returns if the log file has not been initialised. 
     * @param client The connected socket.  
     * @param numConnected Number of currently connected clients.  
     * @param uname The chosen client username.  
     */
    public static void logClientConnected(Socket client, 
                                          int numConnected, 
                                          String uname){
        if (!isLogInit)
            return;
        String address = client.getInetAddress().getHostAddress();
        String port = String.valueOf(client.getPort());
        logLineOfText(
                "client connected @" + uname + " [" + address + ":" + port 
                + "] (" + String.valueOf(numConnected) 
                + " client(s) currently connected)");
    }
    
    /**
     * Log text detailing a client connection.
     * This method returns if the log file has not been initialised. 
     * @param client The connected socket.  
     * @param numConnected Number of currently connected clients.  
     * @param uname The chosen client username.  
     */
    public static void logClientDisconnected(Socket client, 
                                             int numConnected, 
                                             String uname){
        if (!isLogInit)
            return;
        String address = client.getInetAddress().getHostAddress();
        String port = String.valueOf(client.getPort());
        int num = numConnected - 1;
        if (num < 0) num = 0;
        logLineOfText(
                "client disconnected @" + uname + " [" + address + ":" + port 
                + "] (" + String.valueOf(num) 
                + " client(s) currently connected)");
    }
    
    /**
     * Log text detailing a failed password attempt.  
     * @param client The connected socket.  
     * @param password The incorrect password entry.  
     */
    public static void logClientFailedPasswordAttempt(Socket client, 
                                                      String password){
        if (!isLogInit)
            return;
        String address = client.getInetAddress().getHostAddress();
        String port = String.valueOf(client.getPort());
        logLineOfText(
                "client failed to login with '" + password + "' [" 
                + address + ":" + port + "]");
    }
    
    /**
     * Log text detailing the accepted data share.  
     * @param sender The senders username.  
     * @param receiver The receivers username.  
     * @param filename The file name of the share.
     * @param fileSize  The amount of file bytes being transmitted.  
     * @param isAFileShare True if a file is being shared, false if a voice 
     * recording is being shared.  
     */
    public static void logAcceptedDataShare(String  sender, 
                                            String  receiver, 
                                            String  filename,
                                            int     fileSize,
                                            boolean isAFileShare){
        if (!isLogInit)
            return;
        String dataType = "file";
        if (!isAFileShare)
            dataType = "voice";
        logLineOfText(String.format(
                "%s share request [@%s -> @%s] (%s (%d bytes) : accepted)",
                               dataType, sender, receiver, filename, fileSize));
    }
    
    /**
     * Log text detailing the rejected data share.  
     * @param sender The senders username.  
     * @param receiver The receivers username.  
     * @param filename The file name of the share.
     * @param fileSize  The amount of file bytes being transmitted.  
     * @param isAFileShare True if a file is being shared, false if a voice 
     * recording is being shared.  
     */
    public static void logRejectedDataShare(String  sender, 
                                            String  receiver, 
                                            String  filename,
                                            int     fileSize,
                                            boolean isAFileShare){
        if (!isLogInit)
            return;
        String dataType = "file";
        if (!isAFileShare)
            dataType = "voice";
        logLineOfText(String.format(
                "%s share request [@%s -> @%s] (%s (%d bytes) : rejected)",
                               dataType, sender, receiver, filename, fileSize));
    }
    
    /**
     * Log text detailing the failed data share.  
     * @param sender The senders username.  
     * @param receiver The receivers username.  
     * @param filename The file name of the share.
     * @param fileSize  The amount of file bytes being transmitted.  
     * @param isAFileShare True if a file is being shared, false if a voice 
     * recording is being shared.  
     */
    public static void logFailedDataShare(String  sender, 
                                          String  receiver, 
                                          String  filename,
                                          int     fileSize,
                                          boolean isAFileShare){
        if (!isLogInit)
            return;
        String message;
        if (receiver == null)
            message = "sender to server transmission failure";
        else
            message = "server to receiver transmission failure";
        String dataType = "file";
        if (!isAFileShare)
            dataType = "voice";
        logLineOfText(String.format(
                "%s share request [@%s -> @%s] (%s (%d bytes) : %s)",
                               dataType, sender, receiver, filename, fileSize, message));
    }
    
    /**
     * Generic method which writes a line of text to the log file.
     * This method is called by all the log methods in this class.  
     * @param line The line of text to be logged.
     */
    private static void logLineOfText(String line){
        if (!isLogInit)
            return;
        try {
            output.print(getCurrentDateAndTime() + " : ");
            output.println(line);
            output.flush();
        }
        catch (Exception ex){
            //System.out.println(ex.toString());
        }
    }
    
    /**
     * This method returns a date and time which is used as a prefix of all 
     * logged data.  This method is called by the logLineOfText(String) method.  
     * @return A text based timestamp containing the current date and time.  
     */
    private static String getCurrentDateAndTime(){
        return new Date().toString();
    }
}