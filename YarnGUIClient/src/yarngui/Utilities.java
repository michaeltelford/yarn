
package yarngui;

/**
 * Utilities class for static methods used by many other classes.  
 * This class contains utilitarian methods which are useful code snippets 
 * containing common functionality.  Much of the functionality contained within 
 * these methods is used for String processing.  
 * Because all the methods are static this class is abstract and therefore 
 * does not contain a constructor.  
 * @author Michael Telford
 */
public abstract class Utilities {
    
    /**
     * This method provides a time delay used for various purposes.
     * @param millisecs The time in milliseconds which should be used as a 
     * delay.
     */
    public static void delay(int millisecs){
        try {
            Thread.sleep(millisecs);
        } 
        catch (InterruptedException ex) {
            // Do nothing after delay.
        }
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
     * Method which validates the file path before the data share is sent to 
     * the server.  Such validation checks include ensuring that the file exists 
     * on the client machine and contains an extension after the filename as is 
     * required by the server.  
     * @param command The protocol command for the data share.
     * @param isAFileShare True if a file is being shared, false if a voice 
     * recording is being shared.  
     * @return The absolute file path.  
     * @throws Exception If a command format error occurs.  
     */
    public static String validateFilePath(String command, 
                                          boolean isAFileShare) 
                                          throws Exception {
        // Remove the username.
        command = command.substring(command.indexOf(" "));
        
        /* Determine if the transfer is a voice or a file share.
        *  charAt(1) is used instead of 0 because of the space at the start of 
        *  the text and commands.
        */ 
        String dataType;
        if (isAFileShare)
            dataType = ReceiveThread.FILE_SHARE_SEND_CMD;
        else
            dataType = ReceiveThread.VOICE_SHARE_SEND_CMD;
        
        // Get the filepath. The " is escaped below by using \" inside the str.
        String filepath = command.substring(command.indexOf(dataType) 
                          + dataType.length(), command.lastIndexOf('"'));
        if (filepath.isEmpty())
            throw new Exception("Command error, check the filepath and try again...");
        
        // Get the filename.
        String filename = filepath.substring(filepath.lastIndexOf(
                                    System.getProperty("file.separator")) + 1);
        if (filename.isEmpty())
            throw new Exception("Command error, check the filename and try again...");
        
        // Check the filename contains an extension.
        if (!filename.contains(".")) 
            throw new Exception("Command error, check the file extension and try again...");
        String extension = filename.substring(filename.lastIndexOf('.') + 1);
        if (extension.isEmpty())
            throw new Exception("Command error, check the file extension and try again...");
        
        return filepath;
    }
    
    /**
     * Determine if the data share is a voice or a file share.  This is 
     * required to allow the server to adequately inform the receiving client(s) 
     * about what kind of share is being proposed by this client. 
     * @param cmd The data share protocol command.
     * @return True if a file share is in progress, false if it is a 
     * voice share.  
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
        if (cmd.trim().charAt(0) == ReceiveThread.FILE_SHARE_SEND_CMD.trim().charAt(0))
            return true;
        else if (cmd.trim().charAt(0) == ReceiveThread.VOICE_SHARE_SEND_CMD.trim().charAt(0))
            return false;
        else
            throw new Exception("Command protocol error, can't determine the data type");
    }
    
    /**
     * Removes the username(s) from the text parameter.
     * @param text The text containing one or more usernames.
     * @param numUnames The number of usernames to remove.
     * @return A sub-string of the text parameter with numUnames usernames 
     * having been removed.  
     */
    public static String removeUserNames(String text, int numUnames){
        text = text.trim();
        String sender = text.substring(0, text.indexOf(' '));
        for (int i = 0; i < numUnames; i++){
            text = text.substring(text.indexOf("@") + 1);
        }
        text = text.substring(text.indexOf(" ") + 1);
        text = text.trim();
        return sender + " : " + text;
    }
    
    /**
     * This method compares two string arrays in both length and content.  Each 
     * string within the arrays has an @ symbol prepended to the string value if 
     * required.  This method is used to compare the usernames contained in the 
     * received PM against the usernames contained in the conference dialog 
     * usernames text field.  
     * @param textUnames The first array containing usernames.
     * @param confUnames The second array containing usernames.
     * @return True if both arrays match in length and content, false otherwise. 
     */
    public static boolean doUserNamesMatch(String[] textUnames, String[] confUnames){
        try {
            if ((textUnames.length - 1) != confUnames.length)
                return false;
        }
        catch (NullPointerException npe){
            return false;
        }
        
        int matches = 0;
        for (int i = 0; i < confUnames.length; i++){
            String uname1 = confUnames[i].trim();
            if (!uname1.startsWith("@"))
                uname1 = "@" + uname1;
            for (int t = 0; t < textUnames.length; t++){
                String uname2 = textUnames[t].trim();
                if (!uname2.startsWith("@"))
                    uname2 = "@" + uname2;
                if (uname1.equals(uname2))
                    matches++;
            }
        }
        
        if (confUnames.length == matches)
            return true;
        else
            return false;
    }
    
    /**
     * This method is used to determine if a text transfer is addressed to 
     * everybody or specific individuals.  A private message begins with the @ 
     * symbol.  
     * @param text The text being sent to either everybody or specific clients. 
     * @return True if the text parameter begins with an @ symbol, false 
     * otherwise.  
     */
    public static boolean isAPrivateMessage(String text){
        try {
            String s = text.substring(text.indexOf(':') + 1);
            if (s.trim().charAt(0) == '@')
                return true;
            else
                return false;
        }
        catch (Exception ex){
            return false;
        }
    }
    
    /**
     * This method is used to retrieve the usernames at the start of a string. 
     * All usernames should begin with an @ symbol and are separated by a space. 
     * @param text The text containing usernames which are to be retrieved.
     * @return A string array containing any usernames from the start of the 
     * text.  
     */
    public static String[] getUsernamesFromText(String text){
        // Split up any usernames.
        String[] usernames = text.split("@");
        for (int i = 0; i < usernames.length; i++){
            usernames[i] = usernames[i].trim();
        }
        
        try {
            // Separate the last username from the actual text of the message.
            String lastUsername = usernames[usernames.length - 1];
            lastUsername = lastUsername.substring(0, lastUsername.indexOf(" "));
            usernames[usernames.length - 1] = lastUsername.trim();
            // Remove the ':' from the first username (the senders username).
            String firstUsername = usernames[0].trim();
            firstUsername = firstUsername.substring(0, firstUsername.indexOf(' '));
            usernames[0] = firstUsername.trim();
        }
        catch (Exception ex){} // Do nothing if there is no excess text.
        
        // Add the '@' symbol to the usernames.
        for (int i = 0; i < usernames.length; i++){
             if (!usernames[i].startsWith("@"))
                 usernames[i] = "@" + usernames[i];
        }

        return usernames;
    }
    
    /**
     * This method retrieves the data and protocol command length from a data 
     * share command.  
     * @param text The data share protocol command.  
     * @return An integer array containing firstly the data share size and 
     * secondly the data share protocol command size.  
     */
    public static int[] getDataAndCmdLength(String text){
        // Get the file size.
        String temp = text.substring(text.indexOf('?') + 2);
        int dataSize = Integer.parseInt(temp.substring(0, temp.indexOf("\"")));
        // Get the cmd length.
        int firstIndex = text.indexOf("?\"") + "?\"".length();
        int cmdLength = text.indexOf("\"", firstIndex + 1);
        cmdLength++;
        return new int[]{dataSize, cmdLength};
    }
}