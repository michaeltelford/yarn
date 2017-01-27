
package yarngui;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Receive thread created on a successful connection to a server instance. 
 * This class is a sub class of Thread and overrides the run() method which 
 * creates a continuous receive loop (whilst the client remains connected).  
 * Supports both secure and non secure SSL socket connections by only 
 * interacting with the buffered input stream of either type of connection.  
 * A new receive thread is initialised and started when a successful connection 
 * has been established to a server.  The receive thread is stopped when a 
 * disconnect occurs.  
 * 
 * This class also contains the protocol commands used in the transference of 
 * data between clients and server applications.  
 * @author Michael Telford
 */
public class ReceiveThread extends Thread {
    
    // Protocol variables.
    public static final String CONNECTION_REQUEST      = "{###}";
    public static final String VOICE_SHARE_SEND_CMD    = " *\"";   //  *"
    public static final String FILE_SHARE_SEND_CMD     = " ^\"";   //  ^"
    public static final String DATA_SHARE_RECEIVE_CMD  = "^\"\"^"; // ^""^
    public static final String DATA_SHARE_ACCEPT_CMD   = "^-accept";
    public static final String DATA_SHARE_DECLINE_CMD  = "^-reject";
    public static final String VOICE_SHARE_IMMINENT_DATA_CMD = 
                    String.format("%s-*", ReceiveThread.DATA_SHARE_RECEIVE_CMD);
    public static final String FILE_SHARE_IMMINENT_DATA_CMD = 
                    String.format("%s-^", ReceiveThread.DATA_SHARE_RECEIVE_CMD);

    private BufferedInputStream input;
    private GUI gui;
    private Connection conn;
    private String fileName;
    private String senderUname;
    
    /**
     * Constructor which initialises the instance variables used in other 
     * methods of this class.  
     * @param connection The connection instance containing the connected socket 
     * and associated streams.  This connection instance also contains 
     * reference to the main GUI dialog instance.  
     */
    public ReceiveThread(Connection connection){
        conn = connection;
        input = connection.getBufferedInputStream();
        gui = connection.getYarnGUI();
    }

    /**
     * Overridden method from the Thread class.  Used to create a continuous 
     * receive loop for as long as the client remains connected to the server 
     * and a receive error doesn't occur.  This method receives data from the 
     * server and alerts the user.  The type of data being received can be text, 
     * file or a voice recording.  The receive mechanism works by first 
     * receiving a preview of the protocol command.  This preview is used to 
     * determine the type of incoming data and therefore how to process the data 
     * once received.  The user is alerted as to the content of the received 
     * data e.g. received text is printed to the conference text are of the main 
     * GUI dialog instance.  
     */
    @Override
    public void run(){
        
        // Set this thread's name for debugging purposes.
        this.setName("Receive Thread");
        
        // Continously tries to receive data once connected.
        // If a disconnect occurs (InputStream closes etc.) the thread stops.
        while (true){
            try {
                // Receive a preview of the incoming data and decide what to do with it.
                String text = this.receivePreview();
                
                // Do nothing for empty data.
                if (text.trim().isEmpty()){
                    this.skipAvailableData();
                    continue;
                }
                
                // Receive the connection request from the server.
                else if (text.equals(ReceiveThread.CONNECTION_REQUEST)){
                    this.skipAvailableData();
                    Connection conn = this.gui.getConnection();
                    conn.send(ReceiveThread.CONNECTION_REQUEST);
                }
                
                // Check for an incoming file share.
                else if (text.trim().startsWith(ReceiveThread.FILE_SHARE_IMMINENT_DATA_CMD)){
                    this.gui.setGUISendAbility(false);
                    this.gui.setSystemText(
                            String.format("Receiving file from %s... "
                            + "This may take a while if the file is large", 
                            this.senderUname));
                    
                    // Receive the file data.
                    int[] fileDetails = Utilities.getDataAndCmdLength(text);
                    byte[] fileData = this.receiveDataShare(fileDetails[0], fileDetails[1]);
                    
                    // Let the server know the file has been received.
                    Utilities.delay(Configuration.TRANSMISSION_DELAY);
                    this.gui.getConnection().send(ReceiveThread.CONNECTION_REQUEST);
                    
                    // Save the file and alert the user.
                    this.saveFile(fileData); // Also alerts the user.
                    this.gui.setGUISendAbility(true);
                }
                
                // Check for an incoming voice share.
                else if (text.trim().startsWith(ReceiveThread.VOICE_SHARE_IMMINENT_DATA_CMD)){
                    this.gui.setGUISendAbility(false);
                    this.gui.setSystemText(
                            String.format("Receiving voice recording from %s... "
                            + "This may take a while if the recording is large", 
                            this.senderUname));
                    
                    // Receive the voice file data.
                    int[] fileDetails = Utilities.getDataAndCmdLength(text);
                    byte[] fileData = this.receiveDataShare(fileDetails[0], fileDetails[1]);
                    
                    // Let the server know the file has been received.
                    Utilities.delay(Configuration.TRANSMISSION_DELAY);
                    this.gui.getConnection().send(ReceiveThread.CONNECTION_REQUEST);
                    
                    // Save the file and alert the user.
                    File recording = this.saveVoiceRecording(fileData); // Also alerts the user.
                    this.gui.setGUISendAbility(true);
                    
                    // Play recording.
                    new VoiceRecorderDialog(this.gui, recording);
                }
                
                // Check for a data share receive request and alert the user.
                else if (text.trim().startsWith(ReceiveThread.DATA_SHARE_RECEIVE_CMD)){
                    text = this.receive().replace(ReceiveThread.DATA_SHARE_RECEIVE_CMD, "");
                    this.setDataShareDetails(text);
                    boolean accepted = this.gui.showDataShareRequestDialog(text);
                    Connection conn = this.gui.getConnection();
                    if (accepted)
                        conn.send(ReceiveThread.DATA_SHARE_ACCEPT_CMD);
                    else
                        conn.send(ReceiveThread.DATA_SHARE_DECLINE_CMD);
                }
                
                // Else receive the full text data and display it via the GUI.
                else {
                    text = this.receive().trim();
                    // Send response and remove conn. request if necessary.
                    if (text.contains(ReceiveThread.CONNECTION_REQUEST)){
                        Connection conn = this.gui.getConnection();
                        conn.send(ReceiveThread.CONNECTION_REQUEST);
                        text = text.replace(ReceiveThread.CONNECTION_REQUEST, "");
                    }
                    // Conference code.
                    if (this.gui.getConferenceDialog().isVisible() 
                                                    && Utilities.isAPrivateMessage(text)){
                        String[] textUnames = Utilities.getUsernamesFromText(text);
                        String[] confUnames = this.gui.getConferenceDialog().getUsernames();
                        if (Utilities.doUserNamesMatch(textUnames, confUnames)){
                            text = Utilities.removeUserNames(text, confUnames.length);
                            this.gui.getConferenceDialog().setReceivedText(text);
                        }
                        else
                            this.gui.setReceivedText(text);
                    }
                    // Default GUI code for a text data receive.
                    else
                        this.gui.setReceivedText(text);
                }
            }
            
            catch (SocketException se){
                if (se.getMessage().equals("socket closed")){
                    // Stop this thread when the client disconnects from the server.
                }
                else {
                    //Logger.getLogger(ReceiveThread.class.getName()).log(Level.SEVERE, null, se);
                }
                break; // Thread stops.
            }
            
            // Exceptions get displayed for development purposes.
            catch (Exception ex){
                //Logger.getLogger(ReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
                break; // Thread stops.
            }
        }
    }
    
    /**
     * This method gets the sending clients username and the file name of the 
     * data share and assigns the values of which to the corresponding instance 
     * variables.  
     * @param text The protocol command containing the senders username and 
     * file name of the data share.  
     */
    private void setDataShareDetails(String text){
        this.senderUname = text.substring(0, text.indexOf(" "));
        this.fileName = text.substring(text.indexOf("'") + 1, text.lastIndexOf("'"));
    }
    
    /**
     * This method is used to skip all incoming data to this client from the 
     * server.  When there is no incoming data left the method returns allowing 
     * the next receive to be processed as normal.  
     */
    public void skipAvailableData(){
        int cycles = 2;
        try {
            this.conn.setSoTimeout(Configuration.TRANSMISSION_DELAY);
            for (int i = 0; i < cycles; i++){
                try {
                    this.input.skip(Configuration.DATA_BUFFER_SIZE);
                    i = 0;
                }
                catch (Exception ex){}
                Utilities.delay((int)(Configuration.TRANSMISSION_DELAY * 1.5));
            }
        } catch (Exception ex){
            //Logger.getLogger(ReceiveThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            this.conn.setSoTimeout(0);
        }
    }
    
    /**
     * This method is used to receive a data transmission up to the size of the 
     * DATA_BUFFER_SIZE variable.  This code has been developed to handle the 
     * irregular behaviour of SSL connections which send the first data byte as 
     * a separate transmission before sending the remainder in a second 
     * transmission.  This method also handles non SSL receive transmissions 
     * also.
     * @return The receive data in the form of a String.
     * @throws Exception If a read error occurs.
     */
    private synchronized String receive() throws Exception {
        int timeout = this.conn.getSoTimeout();
        byte[] temp = new byte[Configuration.DATA_BUFFER_SIZE];
        byte[] storage = new byte[Configuration.DATA_BUFFER_SIZE];
        
        // Code to deal with the one initial byte receive which occurs during
        // an SSL connection.  Also handles non SSL connections.
        int numRead = 0, offset = 0;
        do {
            if (numRead == 1)
                this.conn.setSoTimeout(100);
            try {
                numRead = this.input.read(temp, 0, temp.length);
                if ((offset + numRead) > storage.length)
                    numRead = (storage.length - offset);
                System.arraycopy(temp, 0, storage, offset, numRead);
                offset += numRead;
            } catch (SocketTimeoutException ste){
                numRead = 0; // Breaks out of the do while loop.
            }
            finally {
                this.conn.setSoTimeout(timeout);
            }
        }
        while (numRead == 1);
  
        // Convert to string and return.
        byte[] data = new byte[offset];
        System.arraycopy(storage, 0, data, 0, data.length);
        return new String(data);
    }
    
    /**
     * This method operates in the same manner as receive() except 
     * its max receive value is set by the PREVIEW_BUFFER_SIZE variable.  This 
     * method also marks and resets the position of data being received.  This 
     * enables a receive to occur after the preview of the same data. 
     * 
     * As a result this method is used to preview incoming data to view and 
     * process the data commands prepending the actual data.  These commands 
     * are then used to handle the data once received by the receive method. 
     * 
     * @return The received preview data in the form of a String. Usually 
     * represents the data command being used to transmit data.
     * @throws Exception If a read error occurs.
     */
    private synchronized String receivePreview() throws Exception {
        this.input.mark(Configuration.PREVIEW_BUFFER_SIZE);
        int timeout = this.conn.getSoTimeout();
        byte[] temp = new byte[Configuration.PREVIEW_BUFFER_SIZE];
        byte[] storage = new byte[Configuration.PREVIEW_BUFFER_SIZE];
        
        // Code to deal with the one initial byte receive which occurs during
        // an SSL connection.  Also handles non SSL connections.
        int numRead = 0, offset = 0;
        do {
            if (numRead == 1)
                this.conn.setSoTimeout(100);
            try {
                numRead = this.input.read(temp, 0, temp.length);
                if ((offset + numRead) > storage.length)
                    numRead = (storage.length - offset);
                System.arraycopy(temp, 0, storage, offset, numRead);
                offset += numRead;
            } catch (SocketTimeoutException ste){
                numRead = 0; // Breaks out of the do while loop.
            }
            finally {
                this.conn.setSoTimeout(timeout);
            }
        }
        while (numRead == 1);
        
        // Convert to string and return.
        byte[] data = new byte[offset];
        System.arraycopy(storage, 0, data, 0, data.length);
        
        this.input.reset();
        return new String(data);
    }
    
    /**
     * This method is responsible for receiving the file data from the sending 
     * client.  The file data is stored in the fileData variable and is accessed 
     * from other methods in this class.  This method is used for both file and 
     * voice data.  
     * @param fileSize The size of the file being shared.
     * @param cmdLength The size of the protocol command which prepends the file 
     * data.
     * @return The received byte data in an array.
     * @throws Exception If a receive error occurs.
     */
    public byte[] receiveDataShare(int fileSize, int cmdLength) throws Exception {
        int dataSize = fileSize + cmdLength;
        byte[] byteData = new byte[dataSize];
        int bytesReceived = 0;
        
        while (bytesReceived < dataSize){
            int bytesLeft = (dataSize - bytesReceived);
            int numBytesToReceive = Configuration.DATA_BUFFER_SIZE;
            if (bytesLeft < Configuration.DATA_BUFFER_SIZE)
                numBytesToReceive = bytesLeft;

            int received = input.read(byteData, bytesReceived, numBytesToReceive);
            if (received != numBytesToReceive)
                numBytesToReceive = received;
            bytesReceived += numBytesToReceive;
        }
        
        if (bytesReceived != dataSize)
            throw new Exception("File size doesn't match the number of bytes received");
        
        // Remove command data from the file data.
        byte[] fileData = new byte[fileSize];
        System.arraycopy(byteData, cmdLength, fileData, 0, fileData.length);
        return fileData;
    }
    
    /**
     * This method is used to save received file data.  This is done by creating 
     * a file matching that originally sent from the sending client and then 
     * setting the files content to that of the received file data.  The 
     * received data is passed in as a parameter.  This code below is fully 
     * cross platform in that both Windows and Linux based OS's are catered for 
     * when trying to locate a users downloads directory (to save the file to). 
     * If for some reason the downloads directory cannot be located then the 
     * current working directory (CWD) is used instead.  Either way the user is 
     * alerted as to the absolute file path of the saved file.  
     * @param fileData The file data to be used in the construction of the newly 
     * created (received) file.  
     */
    private synchronized void saveFile(byte[] fileData){
        String thisUser = System.getProperty("user.name");
        String os = System.getProperty("os.name").toLowerCase();
        String downloads, fullFilePath;
        
        if (os.startsWith("win")){
            downloads = String.format("C:\\Users\\%s\\Downloads", thisUser);
            fullFilePath = String.format("%s\\%s", downloads, this.fileName);
        }
        else if (os.equals("linux")){
            downloads = String.format("/home/%s/Downloads", thisUser);
            fullFilePath = String.format("%s/%s", downloads, fileName);
        }
        else { // Save file to the CWD.
            fullFilePath = this.fileName;
        }
        
        try {
            File file = null;
            FileOutputStream fos = null;
            try {
                file = new File(fullFilePath);
                fos = new FileOutputStream(file);
            } catch (Exception ex){
                this.gui.setSystemText(
                       "There was a problem locating your downloads directory");
                file = new File(this.fileName);
                fos = new FileOutputStream(file);
            }
            fos.write(fileData);
            fos.close();
            this.gui.setSystemText(String.format("File received from %s (%s)",
                                                       this.senderUname,
                                                       file.getAbsolutePath()));
        } 
        catch (Exception ex){
            this.gui.setSystemText(String.format("Could not save the file : %s",
                                                              ex.getMessage()));
        }
    }
    
    /**
     * This method is used to save a received voice recording.  This method 
     * works in the same way as the saveFile() method except all received voice 
     * recordings are of the .WAV file format.  All voice recordings are saved 
     * to the temporary files directory of the given OS where possible.  If for 
     * some reason the temporary files directory cannot be located then the 
     * current working directory is used.  Either way the user is alerted as to 
     * the absolute file path of the saved voice recording.  
     * @param fileData The file data used in the construction of the voice 
     * recording (.WAV file).  
     * @return The saved voice recording file instance or null if an error 
     * occurs.  
     */
    private synchronized File saveVoiceRecording(byte[] fileData){
        // Find the OS specific tmp files directory.
        String fullFilePath = System.getProperty("java.io.tmpdir") +
                              System.getProperty("file.separator") + 
                              this.fileName;
        
        try {
            File file = null;
            FileOutputStream fos = null;
            
            // Try saving the file to the tmp files directory.  
            try {
                file = new File(fullFilePath);
                fos = new FileOutputStream(file);
            } 
            
            // Otherwise save to the CWD.  
            catch (Exception ex){
                this.gui.setSystemText(
                 "There was a problem locating your temporary files directory"
                           + ", using the current working directory instead");
                file = new File(this.fileName);
                fos = new FileOutputStream(file);
            }
            
            // Write the received file data to the newly created file.  
            fos.write(fileData);
            fos.close();
            this.gui.setSystemText(
                    String.format("Voice recording received from %s (%s)",
                                                       this.senderUname,
                                                       file.getAbsolutePath()));
            return file;
        }
        
        // Alert the user if an error occurs during the save.  
        catch (Exception ex){
            this.gui.setSystemText(
                    String.format("Could not save the voice recording : %s",
                                                          ex.getMessage()));
            return null;
        }
    }
}