
package yarnserver;

import java.io.BufferedInputStream;
import java.net.SocketTimeoutException;

/**
 * This class is responsible for the negotiation and transfer of file and voice 
 * data between clients.  
 * 
 * This class also contains static final variables which 
 * are used as the protocol commands when transmitting data.  
 * @author Michael Telford
 */
public class DataShare {
    
    // Protocol variables.
    public static final String CONNECTION_REQUEST      = "{###}";
    public static final String VOICE_SHARE_SEND_CMD    = " *\"";   //  *"
    public static final String FILE_SHARE_SEND_CMD     = " ^\"";   //  ^"
    public static final String DATA_SHARE_RECEIVE_CMD  = "^\"\"^"; // ^""^
    public static final String DATA_SHARE_ACCEPT_CMD   = "^-accept";
    public static final String DATA_SHARE_DECLINE_CMD  = "^-reject";
    public static final String VOICE_SHARE_IMMINENT_DATA_CMD = 
                    String.format("%s-*", DataShare.DATA_SHARE_RECEIVE_CMD);
    public static final String FILE_SHARE_IMMINENT_DATA_CMD = 
                    String.format("%s-^", DataShare.DATA_SHARE_RECEIVE_CMD);
    
    private ClientConnection sendingClient;
    private String fileName;
    private byte[] fileData;
    private int fileSize;
    
    /**
     * The constructor sets the sendingClient and fileName instance variables. 
     * @param sendingClient The client sending the file to other files.
     * @param fileName The name of file being transmitted.  Voice transfers are 
     * sent as files.  
     */
    public DataShare(ClientConnection sendingClient, String fileName){
        this.sendingClient = sendingClient;
        this.fileName = fileName;
    }
    
    /**
     * This method is responsible for receiving the file data from the sending 
     * client.  The file data is stored in the fileData variable and is accessed 
     * from other methods in this class.  This method is used for both file and 
     * voice data.  
     * @param fileSize The size of the file being shared.
     * @param cmdLength The size of the protocol command which prepends the file 
     * data.
     * @return The number of data bytes received.
     * @throws Exception If a receive error occurs.
     */
    public int receiveData(int fileSize, int cmdLength) throws Exception {        
        BufferedInputStream input = this.sendingClient.getBufferedInputStream();
        this.fileSize = fileSize;
        fileSize += cmdLength;
        byte[] byteData = new byte[fileSize];
        
        int bytesReceived = 0;
        while (bytesReceived < fileSize){
            int bytesLeft = (fileSize - bytesReceived);
            int numBytesToReceive = Configuration.DATA_BUFFER_SIZE;
            if (bytesLeft < Configuration.DATA_BUFFER_SIZE)
                numBytesToReceive = bytesLeft;

            int received = input.read(byteData, bytesReceived, numBytesToReceive);
            if (received != numBytesToReceive)
                numBytesToReceive = received;
            bytesReceived += numBytesToReceive;
        }
        
        if (bytesReceived != fileSize)
            throw new Exception("File size doesn't match the number of bytes received");
        this.fileData = byteData;
        return bytesReceived;
    }
    
    /**
     * This method is responsible for contacting the receiving client to 
     * determine whether or not the user wants to accept the data share.  The 
     * user is provided data share details to help base their decision on.  
     * Once the data share request is sent to the receiving client there is a 
     * given amount of time for the client to respond before this method 
     * automatically takes the response to be 'reject' stopping the data share.
     * @param receivingClient A receiving client of the data share.
     * @param isAFileShare True if a file is being transmitted, false if a 
     * voice recording is being transmitted.  
     * @return True if the receiving client wishes to receive the file, 
     * otherwise false is returned.  
     * @throws Exception If a send or receive error occurs.  
     */
    public boolean isDataShareAccepted(ClientConnection receivingClient, 
                                       boolean isAFileShare) throws Exception {
        // Determine the data type - file or voice.
        String dataType = "file";
        if (!isAFileShare)
            dataType = "voice recording";
        
        // Ask the receiving client do they wish to receive the data share.
        String msg = String.format("%s%s wants to send you the %s '%s' "
                     + "(%s bytes)\nDo you wish to accept this %s transfer?"
                     + "\nYou have 10 seconds to respond",
                     DataShare.DATA_SHARE_RECEIVE_CMD, 
                     this.sendingClient.getUsername(),
                     dataType,
                     this.fileName, 
                     this.fileSize,
                     dataType);
        receivingClient.sendDataToThis(msg);

        // Receive the response.
        String response;
        try {
            int timeoutInSeconds = 20;
            response = receivingClient.receivePreviewDataFromThis(timeoutInSeconds * 1000);
            receivingClient.skipAvailableData();
        }
        catch (SocketTimeoutException ste){
            return false;
        }
        
        // Return the client response.
        if (response.trim().equals(DataShare.DATA_SHARE_ACCEPT_CMD))
            return true;
        else
            return false;
    }
    
    /**
     * This method is responsible for sending data to a receiving client.  The 
     * data being sent depends upon whether or not the data share has been 
     * accepted.  If accepted the file data is sent, if rejected both the 
     * sending and receiving clients involved are notified of the rejection.  
     * @param receivingClient A receiving client of the data share.
     * @param isDataShareAccepted True if the receiving client has accepted the 
     * data share, false otherwise.  
     * @param isAFileShare True if the data share contains file data, false if 
     * the data share contains voice recording data.  
     * @throws Exception If a send or receive error occurs.  
     */
    public void sendData(ClientConnection receivingClient, 
                         boolean isDataShareAccepted, 
                         boolean isAFileShare) throws Exception {
        // If yes then send the file to the receiving client.
        if (isDataShareAccepted){
            
            // Determine the data type being transfered.
            String dataType = "file";
            String protocol = DataShare.FILE_SHARE_IMMINENT_DATA_CMD;
            if (!isAFileShare){
                dataType = "voice recording";
                protocol = DataShare.VOICE_SHARE_IMMINENT_DATA_CMD;
            }
            
            sendingClient.sendDataToThis(String.format("Sending file to %s... "
                                + "This may take a while if the file is large", 
                                receivingClient.getUsername()));
            
            // Add the immenient file share command.
            byte[] cmdData = protocol.getBytes();
            byte[] cmdAndFileData = new byte[cmdData.length + this.fileData.length];
            System.arraycopy(cmdData, 0, cmdAndFileData, 0, cmdData.length);
            System.arraycopy(this.fileData, 0, cmdAndFileData, 
                             cmdData.length, this.fileData.length);
            
            // Send the cmd and file data.
            receivingClient.sendDataToThis(cmdAndFileData);
            sendingClient.sendDataToThis(String.format(
                                    "The %s '%s' was successfully sent to %s",
                                    dataType,
                                    this.fileName, 
                                    receivingClient.getUsername()));
        }
        // If no then alert the sending client.
        else {
            sendingClient.sendDataToThis(String.format(
                                         "%s rejected the data share", 
                                         receivingClient.getUsername()));
            receivingClient.sendDataToThis("You have rejected the data share");
        }
    }
}