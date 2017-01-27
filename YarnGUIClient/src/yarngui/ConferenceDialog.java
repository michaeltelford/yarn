
package yarngui;

import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;

/**
 * This class is responsible for providing a sub class of JFrame which is used 
 * as a conference dialog.  This dialog is launched from the main GUI dialog 
 * window when the user clicks the conference button.
 * 
 * This window provides a means of providing the user with a graphical interface 
 * capable of sending and receiving private messages with another user or users. 
 * By using the conference dialog a user cannot send a message to everybody by 
 * mistake.  This dialog therefore acts as a safety measure as well as providing 
 * convenience for private messaging functionality. 
 * @author Michael Telford
 */
public class ConferenceDialog extends javax.swing.JFrame {
    
    public static final String WELCOME_TEXT =
              "Enter the client usernames above and begin typing to start a "
            + "conference with them.  Usernames should be separated by a space.\n\n"
            + "Any text sent from this window will only go to the above chosen "
            + "users.\n\n"
            + "If somebody else adds a new client to the conference the received "
            + "conference text will appear in the main communicator window "
            + "until you update this window with the new username in the text "
            + "field above.  Therefore it is good practice to let the other "
            + "conference users know before you add another user so that "
            + "they can update their conference windows also.\n\n";
    public static final String HELP_MSG =
              "Enter the usernames of the clients in the conference using the text "
            + "field above.\n"
            + "Then write to the conference using the text field and the "
            + "send button below.\n\n";
    
    private String lastSentText = "";
    private Connection connection;

    /**
     * The constructor for the conference dialog instance.  This method is used 
     * to initialise but not display the dialog components, set its position 
     * and initialise instance variables.  The visibility is controlled by the 
     * calling class with the setVisible(boolean) method. 
     * @param gui The parent dialog.  This is needed to correctly position the 
     * conference dialog window. 
     */
    public ConferenceDialog(GUI gui){
        initComponents();
        JComponent comp = (JComponent) this.getContentPane();
        comp.setBorder(new EmptyBorder(1,1,3,1));
        this.setLocation(100, 100);
        this.conference.setText(ConferenceDialog.WELCOME_TEXT);
        this.scrollConferenceToTop();
        this.connection = gui.getConnection();
    }
    
    /**
     * This method displays the sent text in the main text pane. All sent text 
     * is prepended with --> and is appended with a new line break.  The line 
     * break sets the next sent text to be printed on a new line.  This method 
     * is obviously called when the user sends text to the server from the 
     * conference dialog.  
     * @param text The sent text which is to be displayed in the main text 
     * pane to the user.
     */
    private synchronized void setSentText(String text){
        text = text.trim();
        this.conference.append("--> " + text + "\n");
        this.scrollConferenceToBottom();
    }
    
    /**
     * This method works the same as setSentText(String) except system text is 
     * not prepended with --> in the same way.  This method is used when the 
     * system wants to alert the user about something. 
     * @param text The system text to be displayed to the user.
     */
    public final synchronized void setSystemText(String text){
        // Append the text and scroll to the bottom of the conference window.
        // NOTE: Don't trim the text here as it'll remove any \n in the text.
        this.conference.append(text + "\n");
        this.scrollConferenceToBottom();
    }
    
    /**
     * This method works the same as setSentText(String) except its for 
     * displaying text to the user which has been received from other clients 
     * through the server.  The only difference between this method and 
     * setSystemText is that an alert noise is also played when received text is 
     * displayed to the user.  This alert functionality can be turned off in the 
     * configuration if desired by the user.  
     * @param text The received text which is to be displayed to the user. 
     */
    public final synchronized void setReceivedText(String text){
        Utilities.delay(100);
        // Append the text and scroll to the bottom of the conference window.
        this.conference.append(text + "\n");
        this.scrollConferenceToBottom();
        // If the GUI is NOT in focus when a msg is received then play a sound.
        if (!this.isInFocus() && Configuration.soundAlert)
            GUI.playAlertSound();
    }
    
    /**
     * This method disables the send button for when a data share is taking 
     * place.  This disables the users ability to send text along with the 
     * data share until the server has received the data. 
     * @param isEnabled True for enabled functionality, false for disabled 
     * functionality of the send button within this dialog. 
     */
    public void setGUISendAbility(boolean isEnabled){
        this.send.setEnabled(isEnabled);
    }
    
    /**
     * This method scrolls the main text pane of this dialog to the top.  This 
     * is used for when the dialog is launched so that the user can view the 
     * welcome text from the start. 
     */
    private void scrollConferenceToTop(){
        this.conference.select(this.conference.getHeight() - 10000000, 0);
    }
    
    /**
     * This method scrolls the main text pane of this dialog to the bottom. 
     * This method is called after text has been appended to the text pane 
     * allowing for better viewing by the user. 
     */
    private void scrollConferenceToBottom(){
        this.conference.select(this.conference.getHeight() + 10000000, 0);
    }
    
    /**
     * This method is used to retrieve the given usernames from the username 
     * text field.  This information is obviously required when sending text to 
     * other clients.  This method prepends @ to the username if necessary.
     * @return An array containing the entered usernames, each prepended with 
     * @ as is used in the protocol commands.
     */
    public String[] getUsernames(){
        String list = this.usernames.getText().trim();
        if (list.isEmpty())
            return null;
        String[] unames = list.split(" ");
        for (int i = 0; i < unames.length; i++){
            unames[i] = unames[i].trim();
            if (!unames[i].startsWith("@"))
                unames[i] = "@" + unames[i];
        }
        return unames;
    }
    
    /**
     * This method is responsible for prepending the text being sent to the 
     * server with the required usernames of the receiving clients.  This method 
     * is obviously called before the text is sent. 
     * @param unames A string containing all the desired receiving client 
     * usernames.
     * @param message The message being sent to the receiving clients. 
     * @return A string containing the username(s) and the message being sent. 
     */
    private String prefixUsernamesToMessage(String[] unames, String message){
        // Should never return null, just a precaution.
        if (unames == null || unames.length == 0 || message.isEmpty())
            return null;
        String processedMessage = "";
        // All usernames should already have an '@' at the start.
        for (String uname : unames){
            processedMessage += (uname + " ");
        }
        processedMessage += message;
        return processedMessage;
    }
    
    /**
     * This method returns whether or not this dialog in currently in focus.
     * @return True if in focus, false otherwise. 
     */
    public boolean isInFocus(){
        if (this.hasFocus()
         || this.getContentPane().hasFocus()
         || this.conference.hasFocus()
         || this.message.hasFocus()
         || this.send.hasFocus()
         || this.clear.hasFocus()
         || this.usernames.hasFocus()
         || this.usernamesLabel.hasFocus()
         || this.conferenceLabel.hasFocus()
         || this.jScrollPane1.hasFocus()
         || this.jScrollPane2.hasFocus())
            return true;
        else
            return false;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        usernamesLabel = new javax.swing.JLabel();
        usernames = new javax.swing.JTextField();
        conferenceLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        conference = new javax.swing.JTextArea();
        send = new javax.swing.JButton();
        clear = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        message = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Yarn Conference");
        setMinimumSize(new java.awt.Dimension(437, 300));

        usernamesLabel.setText("Usernames");

        usernames.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        usernames.setToolTipText("Conference users");
        usernames.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                usernamesKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                usernamesKeyTyped(evt);
            }
        });

        conferenceLabel.setText("Conference");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        conference.setEditable(false);
        conference.setColumns(20);
        conference.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        conference.setLineWrap(true);
        conference.setRows(5);
        conference.setTabSize(4);
        conference.setToolTipText("Conference text area");
        conference.setWrapStyleWord(true);
        jScrollPane1.setViewportView(conference);

        send.setText("Send");
        send.setToolTipText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        clear.setText("Clear");
        clear.setToolTipText("Clear");
        clear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearActionPerformed(evt);
            }
        });

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        message.setColumns(20);
        message.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        message.setRows(2);
        message.setTabSize(4);
        message.setToolTipText("Send text area");
        message.setWrapStyleWord(true);
        message.setMinimumSize(new java.awt.Dimension(204, 34));
        message.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                messageKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                messageKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(message);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(usernames)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(send, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(308, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usernamesLabel)
                    .addComponent(conferenceLabel))
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(usernamesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernames, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(conferenceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(send, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(clear, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action method for the clear button.  This method clears the text from 
     * the smaller text pane where the user types a message to be sent. 
     * @param evt The dialog event.
     */
    private void clearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearActionPerformed
        if (!this.message.getText().isEmpty())
            this.message.setText("");
        this.message.requestFocus();
    }//GEN-LAST:event_clearActionPerformed

    /**
     * Action method for the send button.  This method takes the text from the 
     * small text pane and the usernames from the username field, combines their 
     * contents and sends it to the server.  There are some cases where the text 
     * isn't sent, -help for example displays the help message rather than 
     * sending text to the server. 
     * @param evt The dialog event.
     */
    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed
        String messageText = this.message.getText().trim();
        if (messageText.isEmpty()){ 
            return;
        }
        else {
            this.lastSentText = messageText;
        }
        
        if (messageText.equals("-h") || messageText.equals("-help")){
            this.setSystemText(ConferenceDialog.HELP_MSG);
        }
        else if ((messageText.startsWith("@") && messageText.contains(ReceiveThread.FILE_SHARE_SEND_CMD)) ||
                 (messageText.startsWith("@") && messageText.contains(ReceiveThread.VOICE_SHARE_SEND_CMD))){
            this.setSystemText("Cannot perform a data share from this window, "
                             + "use the main communicator window.");
        }
        else if (messageText.startsWith("@")){
            this.setSystemText("Usernames should be provided in the text field "
                    + "above, not in the message text going to the conference.");
        }
        else {
            String[] unames = this.getUsernames();
            // If usernames have been provided.
            if (unames != null){
                this.message.setText("");
                this.setSentText(messageText);
                messageText = this.prefixUsernamesToMessage(unames, messageText);
                this.connection.send(messageText);
                this.message.requestFocus();
            }
            // If no usernames have been provided.
            else {
                this.setSystemText("You must provide at least one username");
                this.usernames.requestFocus();
            }
        }
    }//GEN-LAST:event_sendActionPerformed

    /**
     * Action method for when a key is pressed whilst the smaller text pane has 
     * focus.  Different keys being pressed result in different actions.  For 
     * example the enter key sends whatever text is typed in the small text 
     * pane. The escape key results in the clear button being pressed. The up 
     * arrow button results in the last sent text being pasted into the smaller 
     * text pane.
     * @param evt The dialog event.
     */
    private void messageKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageKeyPressed
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER && Configuration.enterSend)
            this.send.doClick();
        else if (keyCode == KeyEvent.VK_ESCAPE)
            this.clear.doClick();
        else if (keyCode == KeyEvent.VK_UP){
            this.message.setText(this.lastSentText);
        }
    }//GEN-LAST:event_messageKeyPressed

    /**
     * Action method for when a key has been released whilst the small text pane 
     * has focus.  The enter whey when release clears the text field after the 
     * text has been sent to the server.  
     * @param evt The dialog event.
     */
    private void messageKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_messageKeyReleased
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ENTER && Configuration.enterSend 
                && !this.message.getText().isEmpty())
            this.message.setText("");
    }//GEN-LAST:event_messageKeyReleased

    /**
     * Not used.
     * @param evt 
     */
    private void usernamesKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernamesKeyTyped
        // Not used.
    }//GEN-LAST:event_usernamesKeyTyped

    /**
     * Action method for when the escape key is released whilst the usernames 
     * text field has focus.  The result is that the username text is removed 
     * clearing the text field completely.
     * @param evt The dialog event.
     */
    private void usernamesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernamesKeyReleased
        int keyCode = evt.getKeyCode();
        if (keyCode == KeyEvent.VK_ESCAPE)
            this.usernames.setText("");
    }//GEN-LAST:event_usernamesKeyReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clear;
    private javax.swing.JTextArea conference;
    private javax.swing.JLabel conferenceLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea message;
    private javax.swing.JButton send;
    private javax.swing.JTextField usernames;
    private javax.swing.JLabel usernamesLabel;
    // End of variables declaration//GEN-END:variables
}