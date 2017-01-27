
package yarngui;

import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class extends JFrame to provide a data share dialog.  This data share 
 * dialog is launched from the main GUI dialog instance.  The data share dialog 
 * is used to build a data share request which is sent through the server to one 
 * or many clients.  
 * @author Michael Telford
 */
public class ShareDialog extends javax.swing.JFrame {
    
    private GUI gui;
    private VoiceRecorderDialog voiceRecorderDialog;
    private int recordingCount = 0;
    private String  fullFileSharePath;
    private String  fullVoiceSharePath;

    /**
     * The constructor initialises the dialog components, sets the dialog 
     * position and then displays it to the user.  The parent component 
     * parameter is used to position this dialog on top of its parent.  This 
     * provides good user graphical interaction.  
     * @param parentComp The parent component used in the positioning of this 
     * component.  
     */
    public ShareDialog(GUI gui){
        initComponents();
        this.gui = gui;
        this.setGuiToFileShare();
        gui.setDialogPosition(this);
        this.setVisible(true);
    }
    
    /**
     * Sets the count of voice recordings since the opening of the 
     * share dialog.  
     * @param count The number of voice recordings since the opening of the 
     * share dialog.  
     */
    public void setRecordingCount(int count){
        this.recordingCount = count;
    }
    
    /**
     * Returns the count of voice recordings since the opening of the share 
     * dialog.
     * @return The count of voice recordings since the opening of the share 
     * dialog.
     */
    public int getRecordCount(){
        return this.recordingCount;
    }
    
    /**
     * This method returns a string array containing the entered usernames from 
     * the usernames text field.  
     * @return A string array containing the username(s) to which the data share 
     * is being sent.  
     * @throws Exception If a username format error occurs.  
     */
    private String[] getUsernames() throws Exception {
        String list = this.usernamesTextField.getText().trim();
        if (list.isEmpty())
            throw new Exception("No username(s) provided");
        String[] unames = list.split(" ");
        for (int i = 0; i < unames.length; i++){
            unames[i] = unames[i].trim();
            if (!unames[i].startsWith("@"))
                unames[i] = "@" + unames[i];
        }
        return unames;
    }
    
    /**
     * This method returns the user selected data share file path.
     * @return The data share file path.  
     * @throws Exception If a file or voice recording hasn't been selected.  
     */
    private String getFilePath() throws Exception {
        if (this.fileShareRadioButton.isSelected()){
            if (this.fullFileSharePath == null || this.fullFileSharePath.isEmpty())
                throw new Exception("No file has been selected");
            return this.fullFileSharePath;
        }
        else if (this.voiceShareRadioButton.isSelected()){
            if (this.fullVoiceSharePath == null || this.fullVoiceSharePath.isEmpty())
                throw new Exception("No voice recording has been selected");
            return this.fullVoiceSharePath;
        }
        else
            throw new Exception("Error occurred while determining a selected radio button");
    }
    
    /**
     * This method returns the protocol command having concatenated the list of 
     * usernames and data share file path.  
     * @param usernames The usernames of the receiving clients.  
     * @param path The file or voice recording path.  
     * @return The data share protocol command.  
     */
    private String getShareCommand(String[] usernames, String path){
        String dataType = ReceiveThread.FILE_SHARE_SEND_CMD;
        if (this.voiceShareRadioButton.isSelected())
            dataType = ReceiveThread.VOICE_SHARE_SEND_CMD;
        String usernamesString = "";
        for (String uname : usernames){
            usernamesString += (uname + " ");
        }
        // NOTE: dataType has a space, double quotes and then the actual data character.
        return String.format("%s%s%s\"", usernamesString.trim(), dataType, path);
    }
    
    /**
     * This method sets the fullVoiceSharePath variable which contains the 
     * absolute file path of the voice recording file.  
     */
    public void setVoiceFilePath(String fullFilePath){
        this.fullVoiceSharePath = fullFilePath;
    }
    
    /**
     * Sets the voice text field with a value.
     * @param text the text value to be set.
     */
    public void setVoiceTextField(String text){
        this.voiceTextField.setText(text);
    }
    
    /**
     * Sets this dialog to transfer a file as opposed to a voice recording.  
     * This enables the file share components and disables the voice recording 
     * components.  
     */
    private void setGuiToFileShare(){
        this.fileChooserButton.setEnabled(true);
        this.voiceRecorderButton.setEnabled(false);
        this.fileTextField.setEnabled(true);
        this.voiceTextField.setEnabled(false);
    }
    
    /**
     * Sets this dialog to transfer a voice recording as opposed to a file.  
     * This enables the voice share components and disables the file share 
     * components.  
     */
    private void setGuiToVoiceShare(){
        this.fileChooserButton.setEnabled(false);
        this.voiceRecorderButton.setEnabled(true);
        this.fileTextField.setEnabled(false);
        this.voiceTextField.setEnabled(true);
    }
    
    /**
     * Closes the voice recorder dialog if it is displayed, otherwise this 
     * method does nothing.  
     */
    private void closeVoiceRecorderDialog(){
        try {
            if (this.voiceRecorderDialog != null || this.voiceRecorderDialog.isVisible())
                this.voiceRecorderDialog.dispose();
        }
        catch (NullPointerException npe){}
    }
    
    /**
     * Overridden method from the JFrame class.  An overridden method was 
     * required to close the voice recorder dialog (if necessary) before the 
     * data share dialog gets closed.  
     */
    @Override
    public void dispose(){
        this.closeVoiceRecorderDialog();
        super.dispose();
    }

    /**
     * Generated code from Netbeans Swing, used to provide the component layout 
     * of this dialog.  
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        radioButtonGroup = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        usernamesTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        fileTextField = new javax.swing.JTextField();
        fileChooserButton = new javax.swing.JButton();
        shareButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        fileShareRadioButton = new javax.swing.JRadioButton();
        voiceShareRadioButton = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        voiceTextField = new javax.swing.JTextField();
        voiceRecorderButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Data Share");
        setAlwaysOnTop(true);
        setResizable(false);

        jLabel1.setText("Client Username(s) : ");

        usernamesTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernamesTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("File :");

        fileTextField.setEditable(false);

        fileChooserButton.setText("...");
        fileChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserButtonActionPerformed(evt);
            }
        });

        shareButton.setText("Share");
        shareButton.setToolTipText("Share");
        shareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shareButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        radioButtonGroup.add(fileShareRadioButton);
        fileShareRadioButton.setSelected(true);
        fileShareRadioButton.setText("File Share");
        fileShareRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileShareRadioButtonActionPerformed(evt);
            }
        });

        radioButtonGroup.add(voiceShareRadioButton);
        voiceShareRadioButton.setText("Voice Share");
        voiceShareRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voiceShareRadioButtonActionPerformed(evt);
            }
        });

        jLabel3.setText("Voice :");

        voiceTextField.setEditable(false);

        voiceRecorderButton.setText("...");
        voiceRecorderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voiceRecorderButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(shareButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(fileShareRadioButton, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(voiceShareRadioButton)))
                        .addGap(0, 41, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(usernamesTextField, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fileTextField)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(voiceTextField))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(voiceRecorderButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fileChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(usernamesTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileShareRadioButton)
                    .addComponent(voiceShareRadioButton))
                .addGap(5, 5, 5)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fileChooserButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(voiceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(voiceRecorderButton, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(shareButton)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Not used.
     * @param evt 
     */
    private void usernamesTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernamesTextFieldActionPerformed
        // Not used.
    }//GEN-LAST:event_usernamesTextFieldActionPerformed

    /**
     * Action event method for the cancel button.  Used to dispose this dialog. 
     * @param evt The dialog event.  
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Action event method for the share button.  Used to retrieve the entered 
     * usernames and share file path (file or voice), produce the corresponding 
     * protocol command and send it via the main GUI dialog which in turn 
     * initialises the data share process.  This dialog is then disposed of.  
     * If an error occurs an error dialog is displayed to the user.  
     * @param evt The dialog event.  
     */
    private void shareButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shareButtonActionPerformed
        // Check the username and file have been provided.
        try {
            // Get the username and fileshare.
            String[] usernames = this.getUsernames();
            String path = this.getFilePath();
            
            // Formulate the file share command and send it to the server.
            String cmd = this.getShareCommand(usernames, path);
            this.gui.enterAndSendText(cmd);
            this.dispose();
        }
        catch (Exception ex){
            JOptionPane.showMessageDialog(this, ex.getMessage(), 
                                          "Error", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_shareButtonActionPerformed

    /**
     * Action event method for the file share chooser button.  Displays a 
     * file select dialog to the user to locate the desired file to be shared. 
     * The file select dialog is a JFileChooser object.  
     * @param evt The dialog event.  
     */
    private void fileChooserButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserButtonActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showDialog(this, "Select File");
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            this.fullFileSharePath = file.getAbsolutePath();
            this.fileTextField.setText(file.getName());
        }
    }//GEN-LAST:event_fileChooserButtonActionPerformed

    /**
     * Action event method used to initialise (if necessary) and display the 
     * voice recorder dialog used to record a voice recording.  
     * @param evt The dialog event.  
     */
    private void voiceRecorderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voiceRecorderButtonActionPerformed
        if (this.voiceRecorderDialog == null || !this.voiceRecorderDialog.isVisible())
            this.voiceRecorderDialog = new VoiceRecorderDialog(this);
        else
            this.voiceRecorderDialog.requestFocus();
    }//GEN-LAST:event_voiceRecorderButtonActionPerformed

    /**
     * Action event method for the file share radio button.  Closes the voice 
     * recorder dialog (if necessary) and configures the dialog for a file 
     * share as opposed to a voice share.  
     * @param evt The dialog event.  
     */
    private void fileShareRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileShareRadioButtonActionPerformed
        this.closeVoiceRecorderDialog();
        this.setGuiToFileShare();
    }//GEN-LAST:event_fileShareRadioButtonActionPerformed

    /**
     * Action event method for the file share radio button.  Configures the 
     * dialog for a voice share as opposed to a file share.  
     * @param evt The dialog event.  
     */
    private void voiceShareRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voiceShareRadioButtonActionPerformed
        this.setGuiToVoiceShare();
    }//GEN-LAST:event_voiceShareRadioButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton fileChooserButton;
    private javax.swing.JRadioButton fileShareRadioButton;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.ButtonGroup radioButtonGroup;
    private javax.swing.JButton shareButton;
    private javax.swing.JTextField usernamesTextField;
    private javax.swing.JButton voiceRecorderButton;
    private javax.swing.JRadioButton voiceShareRadioButton;
    private javax.swing.JTextField voiceTextField;
    // End of variables declaration//GEN-END:variables
}