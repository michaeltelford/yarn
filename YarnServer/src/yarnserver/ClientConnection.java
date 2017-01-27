
package yarnserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 * Used to provide an instance for each client connection to the server.
 * The overall amount of client connections and checking of connection status
 * should be handled by the calling class (Main).  An instance of this class should
 * be used once a client has connected successfully to the server.  The sending
 * and receiving of textual data once connected is handled by this class.  This
 * class can obviously interact with other clients through their instances of 
 * this class. 
 * @author Michael Telford
 */
public class ClientConnection extends Thread {

    private String               username               = null;
    private String               status                 = null;
    private boolean              isDataShareInProgress  = false;
    private Socket               socket                 = null;
    private BufferedInputStream  input                  = null;
    private BufferedOutputStream output                 = null;
    private Timer                connectionTimer        = null;

    /**
     * Constructor which takes a connected socket to create data streams from.
     * These are used for the subsequent transfer of data thereafter. The data
     * streams are buffered for efficient data transmissions. This constructor
     * also sets the socket timeout to zero causing receive methods to block.
     * @param socket - The connected socket instance.
     * @throws Exception - If there is an underlying socket error.
     */
    public ClientConnection(Socket socket) throws Exception {
        super();
        this.socket = socket;
        // Init data streams on connected socket.
        this.socket.setSoTimeout(0);
        this.input = new BufferedInputStream(
                this.socket.getInputStream(), Configuration.DATA_BUFFER_SIZE);
        this.output = new BufferedOutputStream(
                this.socket.getOutputStream(), Configuration.DATA_BUFFER_SIZE);
    }
    
    /**
     * Returns the buffered input stream for this client connection instance.
     * @return The buffered input stream instance.
     */
    public BufferedInputStream getBufferedInputStream(){
        return this.input;
    }
    
    /**
     * Returns the chosen client username.
     * @return - The chosen client username.
     */
    public String getUsername(){
        return this.username;
    }
    
    /**
     * Returns the client presence information status.
     * @return - The client presence information status.
     */
    public String getStatus(){
        return this.status;
    }

    /**
     * Returns the isDataShareInProgress boolean flag.
     * @return - The isDataShareInProgress boolean flag.
     */
    public boolean isDataShareInProgress(){
        return this.isDataShareInProgress;
    }
    
    /**
     * Sets the isDataShareInProgress boolean flag.
     * @param isFileShareInProgress true or false.
     */
    public void setDataShareProgress(boolean isFileShareInProgress){
        this.isDataShareInProgress = isFileShareInProgress;
    }

    /**
     * This method is called from the run() method for each client connection.
     * Its job is to send a connection request to the client when the connection 
     * timer has expired and a data share is not currently in progress. To remain 
     * connected the client must respond within a certain time frame. The client 
     * response is automatic if connected, the user is not alerted to its workings.
     * This method helps in proxy situations where a client disconnect does not 
     * disconnect the servers connection with the proxy therefore the server is 
     * not notified directly of the client disconnect.
     * @return true or false.
     */
    private boolean isStillConnected(){
        try {
            // If the connection timer has expired send a connection request.
            if (this.connectionTimer.hasTimerExpired() && !this.isDataShareInProgress){
                this.sendDataToThis(DataShare.CONNECTION_REQUEST);
                
                // Wait for the client response.
                String text = this.receivePreviewDataFromThis(
                    Configuration.getConnectionResponseTimeoutInMilliSeconds());
                if (text.equals(DataShare.CONNECTION_REQUEST))
                    this.skipAvailableData();
                
                // If connected then reset the client connection timer for the 
                // next cycle.  
                this.startTimerThread();
            }
            return true;
        }
        // Any exceptions thrown mean the client is no longer connected.
        catch (Exception ex){
            return false;
        }
    }
    
    /**
     * This method disconnects the client connection by nullifing the client 
     * connection slot in the array.  This therefore frees the slot for a 
     * future connecting client. This closes the clients side of the connection. 
     */
    private void disconnect(){
        for (int i = 0; i < Main.connections.length; i++){
            if (Main.connections[i] == this){
                Log.logClientDisconnected(this.socket,
                        Utilities.getNumAllConnectedClients(), this.username);
                Main.connections[i].interrupt();
                Main.connections[i] = null;
                break;
            }
        }
    }
    
    /**
     * This method alerts other users to the disconnect and then calls the 
     * disconnect method.
     * @throws Exception If there is an issue alerting other users to the 
     * client disconnect. 
     */
    private void disconnectOnException() throws Exception {
        // Alert others that the user has left the yarn.
        String oldUser = username + " has left the yarn";
        this.sendDataToAllOtherClients(oldUser);
        this.disconnect();
    }

    /**
     * This method calls disconnect for all connected clients.
     */
    private void disconnectAllClients(){
        for (int i = 0; i < Main.connections.length; i++){
            if (Main.connections[i] != null)
                Main.connections[i].disconnect();
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
    public String receiveDataFromThis() throws Exception {
        int timeout = this.socket.getSoTimeout();
        byte[] temp = new byte[Configuration.DATA_BUFFER_SIZE];
        byte[] storage = new byte[Configuration.DATA_BUFFER_SIZE];
        
        // Code to deal with the one initial byte receive which occurs during
        // an SSL connection.  Also handles non SSL connections.
        int numRead = 0, offset = 0;
        do {
            if (numRead == 1)
                this.socket.setSoTimeout(100);
            try {
                numRead = this.input.read(temp, 0, temp.length);
                if ((offset + numRead) > storage.length)
                    numRead = (storage.length - offset);
                System.arraycopy(temp, 0, storage, offset, numRead);
                offset += numRead;
                Utilities.delay(Configuration.TRANSMISSION_DELAY);
            } catch (SocketTimeoutException ste){
                numRead = 0; // Breaks out of the do while loop.
            }
            finally {
                this.socket.setSoTimeout(timeout);
            }
        }
        while (numRead == 1);
        
        // For when a timeout has already been set and it has expired.
        if (offset == 0){
            throw new SocketTimeoutException(
                                   "Didn't receive anything in the time frame");
        }
        
        // Convert to string and return.
        byte[] data = new byte[offset];
        System.arraycopy(storage, 0, data, 0, data.length);
        return new String(data);
    }
    
    /**
     * This method operates in the same manner as receiveDataFromThis() except 
     * its max receive value is set by the PREVIEW_BUFFER_SIZE variable.  This 
     * method also marks and resets the position of data being received.  This 
     * enables a receive to occur after the preview of the same data. 
     * 
     * As a result this method is used to preview incoming data to view and 
     * process the data commands prepending the actual data.  These commands 
     * are then used to handle the data once received by the 
     * receiveDataFromThis() method. 
     * 
     * @return The received preview data in the form of a String. Usually 
     * represents the data command being used to transmit data.
     * @throws Exception If a read error occurs.
     */
    public String receivePreviewDataFromThis() throws Exception {
        this.input.mark(Configuration.PREVIEW_BUFFER_SIZE);
        
        int timeout = this.socket.getSoTimeout();
        byte[] temp = new byte[Configuration.PREVIEW_BUFFER_SIZE];
        byte[] storage = new byte[Configuration.PREVIEW_BUFFER_SIZE];
        
        // Code to deal with the one initial byte receive which occurs during
        // an SSL connection.  Also handles non SSL connections.
        int numRead = 0, offset = 0;
        do {
            if (numRead == 1)
                this.socket.setSoTimeout(100);
            try {
                numRead = this.input.read(temp, 0, temp.length);
                if ((offset + numRead) > storage.length)
                    numRead = (storage.length - offset);
                System.arraycopy(temp, 0, storage, offset, numRead);
                offset += numRead;
                Utilities.delay(Configuration.TRANSMISSION_DELAY);
            } catch (SocketTimeoutException ste){
                numRead = 0; // Breaks out of the do while loop.
            }
            finally {
                this.socket.setSoTimeout(timeout);
            }
        }
        while (numRead == 1);
        
        // For when a timeout has already been set and it has expired.
        if (offset == 0){
            throw new SocketTimeoutException(
                                   "Didn't receive anything in the time frame");
        }
        
        // Convert to string and return.
        byte[] data = new byte[offset];
        System.arraycopy(storage, 0, data, 0, data.length);
        
        // Resets the stream position so the full data can be received by the 
        // receiveDataFromThis() method.
        this.input.reset();
        return new String(data);
    }
    
    /**
     * This sets the socket timeout from the given parameter timeoutInMilliSeconds 
     * and then calls the receivePreviewDataFromThis() method before resetting 
     * the socket timeout to zero for subsequent receives.  This creates a 
     * timed preview receive which throws a SocketTimeoutException if the time 
     * expires before any data is received.
     * @param timeoutInMilliSeconds The receive time limit.
     * @return The received data in the form of a String.
     * @throws SocketTimeoutException If data has not been received within the 
     * specified time limit.
     */
    public String receivePreviewDataFromThis(int timeoutInMilliSeconds) 
                                                              throws Exception {
        String text = "";
        this.socket.setSoTimeout(timeoutInMilliSeconds);
        
        try {
            text = this.receivePreviewDataFromThis();
        }
        catch (SocketTimeoutException ste){
            throw ste;
        }
        finally {
            this.socket.setSoTimeout(0);
        }
        
        return text;
    }
    
    /**
     * This method is used to skip all incoming data to this client connection. 
     * When there is no incoming data left the method returns allowing the next 
     * receive to be processed as normal.  
     */
    public void skipAvailableData(){
        int cycles = 2;
        try {
            this.socket.setSoTimeout(Configuration.TRANSMISSION_DELAY);
            for (int i = 0; i < cycles; i++){
                try {
                    this.input.skip(Configuration.DATA_BUFFER_SIZE);
                    i = 0;
                }
                catch (Exception ex){}
                Utilities.delay((int)(Configuration.TRANSMISSION_DELAY * 1.5));
            }
        } catch (Exception ex){
            //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                this.socket.setSoTimeout(0);
            } catch (SocketException ex){
                //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method is used to send data to the connected client.  The input 
     * parameter is the data to be sent in the form of bytes.  Therefore this 
     * method is used to send all forms of data whether it be text or file etc.
     * This should be the only method used to write data via the output steam.
     * Other send methods therefore call this method to actually transmit the 
     * data to the connected client.
     * @param data The data bytes to be sent.
     * @throws Exception If there is a send error.
     */
    public void sendDataToThis(byte[] data) throws Exception {
        try {    
            int bytesSent = 0;
            while (bytesSent < data.length){
                int bytesLeft = (data.length - bytesSent);
                int numBytesToSend = Configuration.DATA_BUFFER_SIZE;
                if (bytesLeft < Configuration.DATA_BUFFER_SIZE)
                    numBytesToSend = bytesLeft;

                this.output.write(data, bytesSent, numBytesToSend);
                this.output.flush();
                bytesSent += numBytesToSend;
                Utilities.delay(Configuration.TRANSMISSION_DELAY);
            }
        }
        // If a client has disconnected but the server is yet to
        // realise by not receiving a connection response from the client catch
        // the exception and print alert, otherwise throw any other exception.
        catch (SocketException se){
            if (!se.getMessage().equals("Connection closed by remote host"))
                throw se;
//            else
//                System.out.println(se.getMessage());
        }
    }
    
    /**
     * Takes a text (String) argument and converts it to byte data before 
     * calling the sendDataToThis(byte[]) method.
     * @param text The text to be sent.
     * @throws Exception If a send error occurs.
     */
    public void sendDataToThis(String text) throws Exception {
        this.sendDataToThis(text.getBytes());
    }
    
    /**
     * This method sends data to all connected clients which aren't this one 
     * (the sending client).  Therefore this method calls the sendDataToThis(byte[]) 
     * method to transmit the data to each corresponding client.
     * @param data The broken down byte data to be sent.
     * @throws Exception If a send error occurs.
     */
    private void sendDataToAllOtherClients(byte[] data) throws Exception {
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null && client.username != null && 
                client != this && !client.isDataShareInProgress())
            {
                client.sendDataToThis(data);
            }
        }
    }
    
    /**
     * This method takes text (String) data as an input and sends it to all 
     * other connected clients that aren't this one.  Therefore this method 
     * calls the sendDataToAllOtherClients(byte[]) method to transmit the data.
     * @param text The text data to be sent.
     * @throws Exception If a send error occurs.
     */
    private void sendDataToAllOtherClients(String text) throws Exception {
        this.sendDataToAllOtherClients(text.getBytes());
    }
    
    /**
     * This method sends data to all connected clients including this one. 
     * This method calls the sendDataToThis(byte[]) method to transmit the data.
     * @param data The byte data to be sent.
     * @throws Exception If a send error occurs.
     */
    private void sendDataToAllClients(byte[] data) throws Exception {
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null && client.username != null && !client.isDataShareInProgress())
            {
                client.sendDataToThis(data);
            }
        }
    }
    
    /**
     * This method takes a text (String) argument, converts the data into bytes 
     * and sends them to all connected clients including this one.  Therefore 
     * the sendDataToAllClients(byte[]) method is called which in turn calls 
     * the sendDataToThis(byte[]) method to transmit the data. 
     * @param text The text data to be sent.
     * @throws Exception If a send occurs.
     */
    private void sendDataToAllClients(String text) throws Exception {
        byte[] data = text.getBytes();
        this.sendDataToAllClients(data);
    }
    
    /**
     * Method used for the sending of text data to specific clients based upon 
     * their chosen usernames.  Validation occurs before the data is transmitted. 
     * Such validation includes checking there is a message after the list of 
     * usernames, removing any invalid usernames etc.
     * @param origDataString The data received from the sending client.
     * @param sendersUsername The username of the sending client.
     * @throws Exception If a send error occurs.
     */
    private void sendPrivateMessage(String origDataString, 
                                    String sendersUsername) throws Exception {
        
        // Assert there is a message after the last username.
        if (!Utilities.hasMessageAfterLastUsername(origDataString)){
            this.sendDataToThis("No message provided after the last username, "
                                                             + "try again...");
            return;
        }
        
        // Get the list of usernames to send the PM to.
        String[] clientUnames = Utilities.getReceivingClientUsernames(origDataString);
  
        // Remove any incorrect usernames and alert the sending client.
        for (String uname : clientUnames){
            ClientConnection client =
                                Utilities.getClientFromUsername(this, uname);
            // If user enters an incorrect username alert them, remove the 
            // bogus username from the dataText and continue.
            if (client == null){
                this.sendDataToThis("Your PM has not been sent to " + uname +
                                                    ", check the username (-n)...");
                origDataString = origDataString.replaceFirst("@" + uname + " ", "");
                continue;
            }
        }
        
        // Get the final list of usernames having removed any bogus ones.
        clientUnames = Utilities.getReceivingClientUsernames(origDataString.trim());
        
        // Cycle through the usernames and send the message to each client.
        for (String uname : clientUnames){
            ClientConnection client = Utilities.getClientFromUsername(this, uname);
            // Send PM to the client matching the username.
            if (client != null && !client.isDataShareInProgress()) // Never should be null.
                client.sendDataToThis(sendersUsername + origDataString);
        }
    }
    
    /**
     * This method begins the data share process between a sending client and 
     * one or more receiving clients.  Both file and voice shares are transmitted 
     * with this method because they reach the server as bytes and are therefore 
     * handled in the same way. Validation occurs before the data is transmitted. 
     * @param cmd The data share protocol command.  Used to process the share. 
     * @throws Exception If an error occurs e.g. the receiving client disconnects 
     * whilst receiving the data.
     */
    private void dataShare(String cmd) throws Exception {
        
        // Assert there is a filepath after the last username.
        if (!Utilities.hasMessageAfterLastUsername(cmd)){
            this.skipAvailableData();
            this.sendDataToThis("No filepath provided after the last username, "
                                                              + "try again...");
            return;
        }
        
        // Determine if the transfer is a file or voice share.
        final boolean isAFileShare = Utilities.isAFileShare(cmd);
        
        // Get the filename, file length and command length.
        final String fileName;
        final int fileLength, cmdLength;
        try {
            String[] details = Utilities.getFileNameAndCmdLengthFromText(
                                                            cmd, isAFileShare);
            fileName         = details[0];
            fileLength       = Integer.parseInt(details[1]);
            cmdLength        = Integer.parseInt(details[2]);
        }
        catch (Exception ex){
            this.skipAvailableData();
            String msg = "Your data share has not been sent, check the filepath, name and extension...";
            this.sendDataToThis(msg);
            return;
        }

        // Check for illegal file extensions.
        String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (Utilities.isValuePresent(Configuration.illegalFileExtensions, ext)){
            this.skipAvailableData();
            String msg = String.format(".%s is not a legal file extension, try again...", ext);
            this.sendDataToThis(msg);
            return;
        }
        
        // Get the username(s) of the receiving client(s).
        String[] usernames;
        try {
            usernames = Utilities.getReceivingClientUsernames(cmd);
        }
        catch (Exception ex){
            this.skipAvailableData();
            this.sendDataToThis("There was a problem with the provided "
                    + "username(s), try again...");
            return;
        }
        
        // Check the usernames are of valid connected clients.
        int numReceivingClients = 0;
        ClientConnection[] temp = new ClientConnection[usernames.length];
        for (int i = 0; i < usernames.length; i++){
            String uname = usernames[i];
            ClientConnection receivingClient = Utilities.getClientFromUsername(this, uname);
            if (receivingClient == null){
                String msg = "Your data share has not been sent to " + uname +
                             ", check the username...";
                this.sendDataToThis(msg);
            }
            else {
                temp[numReceivingClients] = receivingClient;
                numReceivingClients++;
            }
        }
        
        // Check that at least one client is to receive the data share.
        ClientConnection[] receivingClients;
        if (numReceivingClients == 0){
            this.skipAvailableData();
            this.sendDataToThis("Data share failed, check the username(s)");
            return;
        }
        else {
            receivingClients = new ClientConnection[numReceivingClients];
            System.arraycopy(temp, 0, receivingClients, 0, receivingClients.length);
        }
        
        // RECEIVING THE FILE FROM THE SENDING CLIENT.
        // Set up the fileshare instance with the necessary data.
        final DataShare dataShare = new DataShare(this, fileName);
        try { 
            // Isolate the sending client, receive the file and free the client.
            this.isDataShareInProgress = true;
            dataShare.receiveData(fileLength, cmdLength);
            this.isDataShareInProgress = false;
        }
        catch (Exception ex){
            this.disconnect();
            Log.logFailedDataShare(this.username, null, fileName, fileLength, isAFileShare);
            return;
        }
        
        // CYCLE THROUGH THE RECEIVING CLIENTS.
        for (final ClientConnection receivingClient : receivingClients){
            
            final String uname = receivingClient.getUsername();
            
            // Check that the desired client isn't already in a data share.
            if (receivingClient.isDataShareInProgress()){
                String msg = uname + " is already processing a data share, try again later...";
                this.sendDataToThis(msg);
                continue;
            }

            // SENDING THE FILE TO THE RECEIVING CLIENT.
            // Each receiving client has its own request thread to determine if 
            // the data share is accepted. Each client must respond within a 
            // given timeframe.  All this makes for an efficient share process.
            Thread receiveThread = new Thread(new Runnable(){
                public void run(){
                    
                    boolean accepted;
                    try {
                        // Isolate the receiving client, send fileshare request and appropiate
                        // response data then free the client.
                        receivingClient.isDataShareInProgress = true;
                        accepted = dataShare.isDataShareAccepted(receivingClient, isAFileShare);
                        dataShare.sendData(receivingClient, accepted, isAFileShare);

                        // Check the file has been received before releasing the fileshare flag.
                        if (accepted){
                            String response;
                            response = receivingClient.receivePreviewDataFromThis(
                                 Configuration.getDataShareResponseTimeoutInMilliSeconds());
                            if (!response.equals(DataShare.CONNECTION_REQUEST))
                                throw new Exception("Share data not sent to receiving client");
                            else 
                                receivingClient.skipAvailableData();
                        }
                        receivingClient.isDataShareInProgress = false;
                        startTimerThread();
                        receivingClient.startTimerThread();
                    }
                    catch (Exception ex){
                        receivingClient.disconnect();
                        try {
                            sendDataToThis("An error occured, the data share was cancelled");
                        } catch (Exception ex1){}
                        Log.logFailedDataShare(username, uname, fileName, fileLength, isAFileShare);
                        startTimerThread();
                        return;
                    }

                    // Log the accepted or rejected file share.
                    if (accepted){ 
                        Log.logAcceptedDataShare(getUsername(), 
                                                 uname, 
                                                 fileName,
                                                 fileLength,
                                                 isAFileShare);
                    }
                    else {
                        Log.logRejectedDataShare(getUsername(), 
                                                 uname,
                                                 fileName,
                                                 fileLength,
                                                 isAFileShare);
                    }
                } // End thread run method.
            }); // End thread init.
            
            receiveThread.setName("File Receive Thread : " + receivingClient.getUsername());
            receiveThread.start();
        } // End for each receiving client loop.
    }

    /**
     * Cycles through connected clients and sends their names & statuses to the 
     * requesting client. Connected means having entered a username and joined 
     * the chat.  Each status represents each clients presence information. 
     * @throws Exception If a send error occurs.
     */
    private void sendConnectedClientNames() throws Exception {
        if (this.isDataShareInProgress())
            return;
        
        int numClients = Utilities.getNumOtherConnectedClients(this);
        if (numClients <= 0){
            this.sendDataToThis("No other clients are connected");
            return;
        }
        
        String[] names = new String[numClients];
        int index = 0;
        
        // Record usernames of other connected clients.
        for (int i = 0; i < Main.connections.length; i++){
            ClientConnection client = Main.connections[i];
            if (client != null && client != this && client.username != null){
                names[index] = client.getUsername();
                index++;
            }
        }
        
        // NOTE: Don't use String.format() below because there is a format issue
        // when using [s%] for the status information.
        String userNamesString = String.format(("%s (you) [" + 
                                 this.getStatus() + "], "), this.getUsername());
        Arrays.sort(names); // Order the other usernames alphabetically.
	
        // Add other users to the response string.
        for (String uname : names){
            userNamesString += String.format(("%s [" + 
                     Utilities.getClientFromUsername(this, uname).getStatus() + 
                     "], "), uname);
	}
        
        // Remove the last , as it is not needed, then send to client.
        userNamesString = 
                 userNamesString.substring(0, userNamesString.lastIndexOf(','));
        this.sendDataToThis(userNamesString.trim());
    }
    
    /**
     * Counts the number of connected clients not including the calling client.
     * The number is then put into a text response and sent to the calling client.
     * @throws Exception If a send error occurs.
     */
    private void sendNumberOfConnectedClients() throws Exception {
        if (this.isDataShareInProgress())
            return;
        String text = "There is currently " +
                    (Utilities.getNumOtherConnectedClients(this)) +
                    " other connected client(s)";
        this.sendDataToThis(text);
    }
    
    /**
     * This method searches for connected clients with the provided usernames.
     * Each result is recorded and placed in a string response which is then 
     * sent to the sending client.
     * @param dataString Text containing the username(s) to search for.
     * @throws Exception If a send error occurs.
     */
    private void searchForConnectedClients(String dataString) throws Exception {
        if (this.isDataShareInProgress())
            return;
        
        String[] clientUnames = null;
        String resultsString = "";
        
        // Get the list of usernames to search for.
        dataString = dataString.substring(dataString.indexOf("-s ") + "-s ".length());
        clientUnames = Utilities.getReceivingClientUsernames(dataString.trim());
        if (clientUnames.length == 0){
            this.sendDataToThis("Username(s) not detected, use '-s @username'"
                    + " e.g. -s @micky @jimmy");
            return;
        }

        // Search for a client with each username and record the result.
        for (String uname : clientUnames){
            ClientConnection client = 
                    Utilities.getClientFromUsername(null, uname);
            if (client != null) 
                resultsString += ("@" + uname + " is connected\n");
            else
                resultsString += ("@" + uname + " is NOT connected\n");
        }
        
        // Send the results back to the requesting client after removing the 
        // last line break - '\n'.
        resultsString = resultsString.substring(0, resultsString.length() - 1);
        this.sendDataToThis(resultsString);
    }

    /**
     * This method requests that the newly connected client choose a username. 
     * The chosen username is validated to ensure it is both legal and unique 
     * (not already taken). If invalid this method alerts the user and then 
     * calls itself recursively to restart the process.  Once a username has 
     * been chosen the clients status (presence information) is set to the 
     * default value. 
     * @throws Exception If a send or receive error occurs.
     */
    private void setUsernameViaLogin() throws Exception {
        
        // Request username from user.
        String text = "Enter your username (usernames are case sensitive) :";
        this.sendDataToThis(text);

        // Receive username.
        String uname = this.receiveDataFromThis();
        if (uname.length() > Configuration.usernameAndPasswordCharLimit){
            uname = uname.substring(0, Configuration.usernameAndPasswordCharLimit);
            text = String.format("Your chosen username has been truncated "
                    + "to '%s' because it exceeded the maximum character "
                    + "limit (%d)", uname, Configuration.usernameAndPasswordCharLimit);
            this.sendDataToThis(text);
        }
        
        // Check whether username is taken or not allowed.
        if (Utilities.isUsernameOk(this, uname)){
            this.username = uname;
            this.status   = Configuration.legalStatuses[0]; // Default status.
        }
        else {
            text = "Username is taken or not allowed "
                         + "(no spaces allowed), try again...";
            this.sendDataToThis(text);
            Utilities.delay(100);
            this.setUsernameViaLogin();
        }
    }
    
    /**
     * If the server has been setup to require a password upon connection (by 
     * setting a password in the configuration file) then this method is called 
     * to request and validate the client password entry/entries. If an 
     * incorrect password is entered then this method alerts the user and calls 
     * this method recursively. 
     * 
     * This method transmits the password as plain text and therefore should 
     * only be used in conjunction with secure communications (SSL/TLS).
     * 
     * @throws Exception If a send or receive error occurs.
     */
    private void validatePasswordViaLogin() throws Exception {
        // Delays are used here to separate lines being sent.
        // Request the server password.
        Utilities.delay(100);
        String text = "Enter the yarn server password : ";
        this.sendDataToThis(text);
        
        // Receive password.
        String password = this.receiveDataFromThis();
        if (password.length() > Configuration.usernameAndPasswordCharLimit){
            password = password.substring(0, Configuration.usernameAndPasswordCharLimit);
        }
        
        // If password entry is incorrect re-call this method.
        Utilities.delay(100);
        if (!password.equals(Configuration.serverPassword)){
            Log.logClientFailedPasswordAttempt(this.socket, password);
            text = "Access denied, incorrect password provided, try again...";
            this.sendDataToThis(text);
            this.validatePasswordViaLogin();
        }
        // Else if password entry in correct, alert the user and continue.
        else 
            this.sendDataToThis("Access granted!");
        Utilities.delay(100);
    }
    
    /**
     * This method updates the presence information status for the calling client. 
     * Validation occurs to ensure the status is legal etc.  Feedback is given 
     * to the user if an invalid status is provided. 
     * @param status The desired status value.
     * @throws Exception If a send error occurs.
     */
    private void updatePresenceInformation(String status) throws Exception {
        if (this.isDataShareInProgress())
            return;
        
        // Ensure there is a status after the '-p' etc.
        try {
            status = status.trim();
            int index = status.indexOf(' ');
            if (index <= -1)
                throw new Exception("Empty presence status");
            status = status.substring(index + 1);
            if (status.trim().isEmpty())
                throw new Exception("Empty presence status");
        }
        catch (Exception ex){
            this.sendDataToThis("Presence status not detected, use '-p busy' etc.");
            return;
        }
        
        // If the status is valid.
        if (Utilities.isValuePresent(Configuration.legalStatuses, status)){
            // Alert the client as to status update.
            if (this.status.equals(status)){
                this.sendDataToThis("Your presence status is already '" + status + "'");
            }
            else {
                this.status = status;
                this.sendDataToThis("Your presence status has been updated to '" + status + "'");
            }
        }
        // Else if the status is invalid.
        else {
            // Send help information to the client.
            String availableStatuses = "Valid statuses include ";
            for (String s : Configuration.legalStatuses){
                availableStatuses += ("'" + s + "', ");
            }
            availableStatuses = availableStatuses.substring(0, availableStatuses.lastIndexOf(',')).trim();
            
            String msg = "'" + status + "' is not a legal presence status, try again...\n" +
            availableStatuses + "\nE.g. -p busy";
            this.sendDataToThis(msg);
        }
    }
    
    /**
     * This method simply nullifies the existing timer thread (if present) and 
     * starts a new timer thread instance.  The timer thread is used to 
     * periodically send client connection requests to clients.  
     */
    private void startTimerThread(){
        // Stop the timer instance if already initialised.
        if (this.connectionTimer != null && this.connectionTimer.isAlive() &&
                                        !this.connectionTimer.isInterrupted()){
            this.connectionTimer.interrupt();
        }
        
        // Create a new instance, name the timer thread and start the count down.
        this.connectionTimer = new Timer(
                    Configuration.getConnectionRequestDelayInSeconds());
        
        this.connectionTimer.setName(this.getName() + " - Connection Timer");
        this.connectionTimer.start();
    }

    /**
     * When a client connects a client connection thread is started which in 
     * turn executes this method.  Therefore this method provides all client 
     * interactions whether with itself or other clients. 
     * 
     * This method once started requests a password (if applicable) and username 
     * from the client before starting a connection timer thread and entering 
     * the continuous receive loop.
     * 
     * The receive loop waits (blocks) for a certain amount of time.  If data 
     * is received (as a preview) then the connection timer is reset and the 
     * data is processed through a if - else if tree.  If data is not received 
     * within the allocated time frame then the isStillConnected() method is 
     * called.  This method may or may not send a connection request to the 
     * client depending on if the connection timer has expired or not. 
     * 
     * Any exceptions thrown from within this method are handled depending on 
     * the type of exception thrown.  This can lead to a client disconnect or 
     * a message being sent to the user etc.  Certain exceptions result in the 
     * client being disconnected and this client connection instance being 
     * nullified. For example if the client disconnects whilst this method is 
     * in a receive block then the resulting exception will stop this thread 
     * exiting the while(true) loop.  This therefore prevents an infinite loop 
     * scenario from occurring.  
     */
    @Override
    public void run(){
        try {
            
            // Display welcome msg to user.
            this.sendDataToThis(Utilities.getWelcomeMessage());
            
            // Set the receive block to last a max of CONNECTION_REQUEST_DELAY
            // for the pasword and username receive reads.
            this.socket.setSoTimeout(
                       Configuration.getConnectionRequestDelayInMilliSeconds());
            
            // Request the yarn password for this server if necessary.
            if (Configuration.isTheServerPasswordSet())
                this.validatePasswordViaLogin();
            
            // Receive the clients username.
            this.setUsernameViaLogin(); // Sets this.user & this.status vars.
            this.sendDataToThis(("Your username is " + this.username + "\n\n" +
                    "Start typing to have a yarn..."));
            
            this.socket.setSoTimeout(0); // Causes a receive block.
            
            // Tell the user how many other clients are currently connected.
            Utilities.delay(100);
            this.sendNumberOfConnectedClients();
            
            // Log this new connection.
            Log.logClientConnected(this.socket, 
                    Utilities.getNumAllConnectedClients(), this.username);

            // Alert others that the user has entered.
            String newUser = this.username + " has entered for a yarn";
            this.sendDataToAllOtherClients(newUser);
            
            // Init and start the connection timer.
            this.startTimerThread();

            // Initialize variables for receiveData and echo loop.
            String previewString;
            String origDataString;
            String updatedDataString;

            while (true){ // Continuous receive loop.
                try {
                    // Wait for the file share to complete if necessary.
                    while (this.isDataShareInProgress){
                        Utilities.delay(Configuration.TRANSMISSION_DELAY);
                    }
                    
                    // Get preview data and decide what to do, options below.
                    try {
                        previewString = this.receivePreviewDataFromThis(
                                              Configuration.TRANSMISSION_DELAY);
                        this.startTimerThread();
                    }
                    catch (Exception ex){
                        if (!this.isStillConnected()) 
                                throw new SocketException(String.format(
                                           "Not connected: %s", this.username));
                        continue;
                    }

                    // Send data out to clients or self (see options below).
                    // The order within the if-else block below should be run
                    // from most specific to least specific condition.
                    
                    // If the data is empty e.g. "" or a connection request is 
                    // received when the server isn't expecting it do nothing.
                    if (previewString.isEmpty() || previewString.equals(DataShare.CONNECTION_REQUEST)){
                        this.skipAvailableData();
                        continue;
                    }

                    // Displays the current number of connected clients.
                    else if (previewString.equals("-c") || previewString.equals("-connections")){
                        this.skipAvailableData();
                        this.sendNumberOfConnectedClients();
                    }

                    // Lists the usernames of connected clients.
                    else if (previewString.equals("-n") || previewString.equals("-names")){
                        this.skipAvailableData();
                        this.sendConnectedClientNames();
                    }
                    
                    // -h command is handled internally by the client.

                    // Stops listening server and disconnects all clients.
                    else if (previewString.equals(Configuration.serverShutdownCommand)){
                        if (!Configuration.serverShutdownCommand.trim().isEmpty()){
                            if (this.username != null) 
                                Log.logServerStop(username);
                            this.sendDataToAllClients(
                                    "Admin has shut down this server, please re/connect");
                            System.out.println("SERVER HAS STOPPED (client admin request)");
                            System.exit(0);
                        }
                    }
                    
                    // Do nothing if a file share response cmd is received when
                    // it shouldn't, only the fileShare() method is concerned here.
                    else if (previewString.equals(DataShare.DATA_SHARE_ACCEPT_CMD)
                            || previewString.equals(DataShare.DATA_SHARE_DECLINE_CMD)){
                        this.skipAvailableData();
                    }
          
                    // Share command should contain the username and filepath.
                    else if ((previewString.charAt(0) == '@' && 
                              previewString.contains(DataShare.FILE_SHARE_SEND_CMD)) ||
                             (previewString.charAt(0) == '@' && 
                              previewString.contains(DataShare.VOICE_SHARE_SEND_CMD))){
                        this.dataShare(previewString);
                    }
                    
                    // Sends (PM) data to client(s) via the username(s).
                    // E.g. "@bill Howdy bill? wdc?"
                    else if (previewString.charAt(0) == '@'){
                        // Receive full data and fit to exact sized array.
                        origDataString = this.receiveDataFromThis();
                        this.sendPrivateMessage(origDataString, this.username + " : ");
                    }
                    
                    // Searches for connected clients with the given usernames.
                    else if (previewString.startsWith("-s") || previewString.startsWith("-search")){
                        origDataString = this.receiveDataFromThis();
                        this.searchForConnectedClients(origDataString);
                    }
                    
                    // Updates the clients presence info (status).
                    else if (previewString.startsWith("-p") || previewString.startsWith("-presence")){
                        origDataString = this.receiveDataFromThis();
                        this.updatePresenceInformation(origDataString);
                    }

                    // Sends data to all other connected clients.
                    else {
                        // Check if there are other clients connected.
                        if (Utilities.getNumOtherConnectedClients(this) == 0){
                            this.skipAvailableData();
                            if (!this.isDataShareInProgress())
                                this.sendDataToThis("No other clients are connected");
                        }
                        else {
                            // Receive full data and send it to all other clients.
                            origDataString = this.receiveDataFromThis();
                            updatedDataString = this.username + " : " + origDataString;
                            this.sendDataToAllOtherClients(updatedDataString);
                        }
                    }
                }
                // For disconnecting when on a receiveData block on localhost.
                catch (SocketException se){
                    //System.out.println(se.getMessage()); // Print disconnects.
                    this.disconnectOnException();
                    break; // Breaks out of receiveData and echo loop.
                }

                // For disconnecting when on a receiveData block over LAN.
                catch (NegativeArraySizeException nase){
                    //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, nase);
                    this.disconnectOnException();
                    break; // Breaks out of receiveData and echo loop.
                }

                // Exceptions during loop are printed and then the loop
                // commences again for each client connection.
                catch (Exception ex){
                    //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        // For disconnecting when on a receiveData block on localhost.
        catch (SocketException se){
            //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, se);
            this.disconnect();
        }
        // For disconnecting when on a receiveData block over LAN.
        catch (NegativeArraySizeException nase){
            //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, nase);
            this.disconnect();
        }
        // For disconnecting a client if they haven't logged in on time.
        catch (SocketTimeoutException ste){
            //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ste);
            try {
                this.sendDataToThis(
                    "Your login timed out, please re-connect and try again...");
            } catch (Exception ex){}
            this.disconnect();
        }
        catch (Exception ex){
            //Logger.getLogger(ClientConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}