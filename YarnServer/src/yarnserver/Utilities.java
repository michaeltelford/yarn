
package yarnserver;

/**
 * Utilities class for static methods generally used by ClientConnection.
 * This class contains utilitarian methods which are useful code snippets 
 * containing common functionality.  Much of the functionality contained within 
 * these methods is used for String and ClientConnection object processing.  
 * Because all the methods are static this class is abstract and therefore 
 * does not contain a constructor.  
 * @author Michael Telford
 */
public abstract class Utilities {
    
    /**
     * Get method which returns the welcome message sent to all newly 
     * connected clients.  A new line is added to the start and end of the 
     * welcome message for formatting purposes.  
     * @return A String containing the formatted welcome message.  
     */
    public static String getWelcomeMessage(){
        return ("\n" + Configuration.welcomeMessage + "\n");
    }
    
    /**
     * This method is used to retrieve the client usernames from command text. 
     * The usernames retrieved are of the receiving clients which are being 
     * sent data.  This method is called to obtain a list of usernames to which 
     * a PM is being sent.  The usernames are then used to retrieve the correct 
     * client connection instances.  
     * @param cmd The protocol command containing the receiving client 
     * usernames.
     * @return A list of username strings.  One for each client.
     */
    public static String[] getReceivingClientUsernames(String cmd){
        String[] usernames;
        String[] tempUnames = new String[cmd.split(" ").length];
        int numUsernames = 0;
        while (true){
            cmd = cmd.trim();
            if (cmd.startsWith("@")){
                int index = cmd.indexOf(' ');
                if (index != -1){
                    if (Utilities.addValueIfNotPresent(tempUnames, numUsernames, cmd.substring(1, index)))
                        numUsernames++;
                    cmd = cmd.substring(index);
                }
                else {
                    if (Utilities.addValueIfNotPresent(tempUnames, numUsernames, cmd.substring(1)))
                        numUsernames++;
                    break;
                }
            }
            else
                break;
        }
        usernames = new String[numUsernames];
        System.arraycopy(tempUnames, 0, usernames, 0, usernames.length);
        return usernames;
    }
    
    /**
     * This method checks if a given string value is contained in the given 
     * array.  If not then the value is added to the array in the given 
     * position.  This method then returns true if the value has been added and
     * false otherwise.  
     * @param array The string array.
     * @param pos The array position to add the string value to if not already 
     * present.
     * @param value The value which already exists or will be added to the 
     * array.  
     * @return True if the value has been added.  False otherwise.  
     */
    public static boolean addValueIfNotPresent(String[] array, 
                                               int pos, 
                                               String value){
        boolean match = Utilities.isValuePresent(array, value);
        if (!match){
            array[pos] = value;
            return true;
        }
        return false;
    }
    
    /**
     * This method checks if the given string value exists in the given string 
     * array.  True is returned if the value does exist in the array. 
     * Otherwise false is returned.
     * @param array The string array.
     * @param value The value which either does or doesn't exist in the array.
     * @return True is the value exists in the array.  False otherwise.  
     */
    public static boolean isValuePresent(String[] array, String value){
        for (int i = 0; i < array.length; i++){
            try {
                String s = array[i];
                if (s.equals(value))
                    return true;
            }
            catch (NullPointerException npe){
                continue;
            }
        }
        return false;
    }
    
    /**
     * This method retrieves the file name from the protocol command as well as 
     * the file size and the full command length.  Such details are required in 
     * order to properly process a data share when received from the sending 
     * client. 
     * @param text The text based protocol command. 
     * @param isAFileShare True if a file is being transferred, false if a 
     * voice is being transferred.  Although the data is the same, the server 
     * still needs to know the intended data share. 
     * @return A string array containing three String values containing the 
     * file name, file length and command length. 
     * @throws Exception If an error occurs whilst processing the protocol 
     * command. 
     */
    public static String[] getFileNameAndCmdLengthFromText(String text,
                                                           boolean isAFileShare) 
                                                           throws Exception {
        String origText = text;
        
        // Remove the username.
        text = text.substring(text.indexOf(" "));
        
        // Determine if a file or voice transfer is taking place.
        String dataType;
        if (isAFileShare)
            dataType = DataShare.FILE_SHARE_SEND_CMD;
        else
            dataType = DataShare.VOICE_SHARE_SEND_CMD;
        
        // Get the filepath.
        int firstIndex = text.indexOf("\"");
        int secondIndex = text.indexOf("\"", firstIndex + 1);
        String filepath = text.substring(text.indexOf(dataType) + dataType.length(), secondIndex);
        if (filepath.isEmpty())
            throw new Exception("Command error, check the filepath and try again...");
        
        // Get the file length.
        String temp = text.substring(text.indexOf('?') + 2);
        String fileLength = temp.substring(0, temp.indexOf("\""));
        
        /*
        * Determine the file separater, we can't use the system property here
        * because the server might be running on Windows while the sending 
        * client is running on Linux.  Therefore we use String methods.
        */
        String sep = "\\"; // Black slash (Windows).
        if (filepath.lastIndexOf(sep) == -1){
            sep = "/"; // Forward slash (Linux).
            if (filepath.lastIndexOf(sep) == -1)
                throw new Exception("Command error, check the file separater and try again...");
        }
        
        // Get the filename.
        String filename = filepath.substring(filepath.lastIndexOf(sep) + 1);
        if (filename.isEmpty())
            throw new Exception("Command error, check the filename and try again...");
        
        // Check the filename contains an extension.
        if (!filename.contains(".")) 
            throw new Exception("Command error, check the file extension and try again...");
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        if (extension.isEmpty())
            throw new Exception("Command error, check the file extension and try again...");
        
        // Set the cmdLength.
        firstIndex = origText.indexOf("?\"") + "?\"".length();
        int cmdLength = origText.indexOf("\"", firstIndex + 1);
        cmdLength++;
        return new String[] {filename, fileLength, String.valueOf(cmdLength)};
    }
    
    /**
     * Determine if the data share is a voice or a file transfer.  This is 
     * required to adequately inform the receiving client(s) about what kind of 
     * share is being proposed by the sending client. 
     * @param cmd The protocol command.
     * @return True if a file share is in progress, false if it is a voice share.
     * @throws Exception thrown if there is a protocol command format issue.
     */
    public static boolean isAFileShare(String cmd) throws Exception {
        // Remove usernames in the command.
        while (true){
            if (cmd.startsWith("@")){
                cmd = cmd.substring(cmd.indexOf("@") + 1);
                cmd = cmd.substring(cmd.indexOf(" ") + 1);
            }
            else
                break;
        }
        
        // Determine protocol.
        if (cmd.trim().charAt(0) == DataShare.FILE_SHARE_SEND_CMD.trim().charAt(0))
            return true;
        else if (cmd.trim().charAt(0) == DataShare.VOICE_SHARE_SEND_CMD.trim().charAt(0))
            return false;
        else
            throw new Exception("Command protocol error, can't determine the data type");
    }
    
    /**
     * This method is responsible for the initialisation of illegal usernames. 
     * This method is called by the Main class during the server startup. 
     * The illegal usernames contain a list of text based words which each 
     * connecting user cannot select as a username upon connection. 
     * Most of the illegal usernames are made up of protocol and server 
     * commands. 
     */
    public static void initIllegalUnames(){
        Configuration.illegalUsernames     = new String[24];
        Configuration.illegalUsernames[0]  = "-1";
        Configuration.illegalUsernames[1]  = "-c";
        Configuration.illegalUsernames[2]  = "-n";
        Configuration.illegalUsernames[3]  = "-h";
        Configuration.illegalUsernames[4]  = "-s";
        Configuration.illegalUsernames[5]  = Configuration.serverShutdownCommand;
        Configuration.illegalUsernames[6]  = Configuration.welcomeMessage;
        Configuration.illegalUsernames[7]  = "@";
        Configuration.illegalUsernames[8]  = " ";
        Configuration.illegalUsernames[9]  = DataShare.DATA_SHARE_ACCEPT_CMD;
        Configuration.illegalUsernames[10] = DataShare.DATA_SHARE_DECLINE_CMD;
        Configuration.illegalUsernames[11] = DataShare.FILE_SHARE_IMMINENT_DATA_CMD;
        Configuration.illegalUsernames[12] = DataShare.VOICE_SHARE_IMMINENT_DATA_CMD;
        Configuration.illegalUsernames[13] = DataShare.DATA_SHARE_RECEIVE_CMD;
        Configuration.illegalUsernames[14] = DataShare.FILE_SHARE_SEND_CMD;
        Configuration.illegalUsernames[15] = DataShare.CONNECTION_REQUEST;
        Configuration.illegalUsernames[16] = DataShare.VOICE_SHARE_SEND_CMD;
        Configuration.illegalUsernames[17] = "-connections";
        Configuration.illegalUsernames[18] = "-names";
        Configuration.illegalUsernames[19] = "-search";
        Configuration.illegalUsernames[20] = "-help";
        Configuration.illegalUsernames[21] = "";
        Configuration.illegalUsernames[22] = "-p";
        Configuration.illegalUsernames[23] = "-presence";
    }
    
    /**
     * This method is responsible for the initialisation of legal statuses 
     * which a user can choose from when updating their presence information. 
     * This method is called by the Main class during the server startup.
     * NOTE: The first legal status should always be the desired default status 
     * because the first status is applied to newly connected clients. A 
     * sensible default status for newly connected clients is 'online'. 
     */
    public static void initLegalStatuses(){
        /*
         * NOTE: Configuration.legalStatuses[0] should always be the desired 
         * default status for a connecting client, because this is the applied 
         * status when a ClientConnection thread is initialised and started.
         */
        Configuration.legalStatuses     = new String[3];
        Configuration.legalStatuses[0]  = "online";
        Configuration.legalStatuses[1]  = "busy";
        Configuration.legalStatuses[2]  = "away";
    }
    
    /**
     * This method is responsible for the initialisation of illegal file 
     * extensions which are used to passively validate file shares. In other 
     * words the illegal file extensions cannot be shared using this system. 
     * This method is called by the Main class during the server startup. 
     */
    public static void initIllegalFileExtentions(){
        Configuration.illegalFileExtensions     = new String[1];
        Configuration.illegalFileExtensions[0]  = "exe";
    }
    
    /**
     * This method is used to retrieve the corresponding client connection 
     * instance from the given username. The 'thisClient' parameter provides a 
     * means of including the calling client connection in the search.  If this 
     * parameter is null then all currently connected clients are searched 
     * through, otherwise if a client connection is provided then that client 
     * is excluded from the search.  If no matching client is found based on 
     * the given username then null is returned. This method is called in many 
     * places and is common functionality when working with client connections. 
     * @param thisClient Excludes the given client from the search if provided. 
     * Otherwise null can be provided to search through all connected clients. 
     * @param uname The username to match against chosen client usernames. The 
     * matching client is returned.
     * @return A client connection instance with a username matching that of 
     * the given username parameter. 
     * @throws Exception If an error occurs when cycling through and processing 
     * client connections. 
     */
    public static ClientConnection getClientFromUsername(
                                                   ClientConnection thisClient,
                                                   String uname)
                                                   throws Exception {
        // Returns null if no client has matching username.
        // thisClient can be null if you want the calling clients username to be
        // included in the search, otherwise it is excluded.
        if (uname == null || uname.isEmpty())
            return null;
        if (thisClient != null){
            for (int i = 0; i < Main.connections.length; i++){
                ClientConnection client = Main.connections[i];
                if (client != null 
                 && client.getUsername() != null
                 && client.getUsername().equals(uname) 
                 && client != thisClient){
                    return client;
                }
            }
        }
        else {
            for (int i = 0; i < Main.connections.length; i++){
                ClientConnection client = Main.connections[i];
                if (client != null 
                 && client.getUsername() != null
                 && client.getUsername().equals(uname)){
                    return client;
                }
            }
        }
        return null;
    }
    
    /**
     * This method processes the given text parameter and checks if there is a 
     * message after the last username.  This method is called when processing 
     * private messages (PM's). 
     * @param text The text parameter which either contains a message after a 
     * series of usernames or doesn't. 
     * @return True if there is a message after the last username, false 
     * otherwise. 
     */
    public static boolean hasMessageAfterLastUsername(String text){
        // Should never be the case, just precautionary.
        if (text.trim().isEmpty())
            return false;
        
        // Get the number of unames at the start of the text.
        String[] subStrings = text.trim().split(" ");
        int numUsernames = 0;
        for (String s : subStrings){
            if (s.trim().startsWith("@"))
                numUsernames++;
        }
        
        // Assert that there is or isn't a message after the last uname.
        if (subStrings.length > numUsernames)
            return true;
        else
            return false;
    }
    
    /**
     * This method is responsible for validating the provided username.  To be 
     * legal a username must not match any of the illegal usernames and must be 
     * unique (not already chosen by another user). 
     * @param thisClient The calling client connection instance which is 
     * provided so that all other clients and usernames are retrieved and 
     * validated against. This method is called when a client connects to a 
     * server and provides a desired username. 
     * @param uname The desired client username which is to be validated.
     * @return True if the desired username is not illegal and is not already 
     * taken by another user. 
     */
    public static boolean isUsernameOk(ClientConnection thisClient,
                                       String uname){
        // Check that the uname isn't empty.
        if (uname == null || uname.isEmpty())
            return false;
        
        // Check that uname is legal, decided by the initIllegalUnames() method.
        if (Utilities.isValuePresent(Configuration.illegalUsernames, uname))
            return false;
        
        // Should never be the case, just precautionary.
        if (uname.trim().isEmpty())
            return false;
        
        // Check that uname is not already taken by another user.
        String otherUsername;
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null && client != thisClient){
                otherUsername = client.getUsername();
                if (otherUsername == null)
                    continue;
                else if (otherUsername.equals(uname))
                    return false;
            }
        }
        return true;
    }
    
    /**
     * This method returns the number of connected clients.  Connected is 
     * defined as having established a connection with the server and entered 
     * a legal and unique username. 
     * @return The number of currently connected clients.
     */
    public static int getNumAllConnectedClients(){
        int connected = 0;
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null && client.getUsername() != null){
                connected++;
            }
        }
        return connected;
    }

    /**
     * This method returns the number of other connected clients.  Connected is 
     * defined as having established a connection with the server and entered 
     * a legal and unique username. The result of this method is the same as 
     * 'int count = (getNumAllConnectedClients() - 1);'.
     * @return The number of other currently connected clients.  This count 
     * excludes the calling client connection. 
     */
    public static int getNumOtherConnectedClients(ClientConnection thisClient){
        int connected = 0;
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null 
             && client != thisClient 
             && client.getUsername() != null){
                connected++;
            }
        }
        return connected;
    }
    
    /**
     * This method provides a time delay used for various purposes.
     * @param time The time in milliseconds which should be used as a delay.
     */
    public static void delay(int time){
        try {
            Thread.sleep(time);
        }
        catch (InterruptedException ie){
            // Do nothing.
        }
    }
}