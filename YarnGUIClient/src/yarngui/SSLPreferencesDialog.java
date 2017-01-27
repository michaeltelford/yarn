
package yarngui;

import java.awt.Point;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * This class extends JFrame to provide an SSL preferences dialog.  This SSL 
 * dialog is used in conjunction with the 'standard' preferences dialog.  This 
 * dialog obviously only contains configuration preferences applied to the 
 * client SSL (secure communication) connection setup.  
 * @author Michael Telford
 */
public class SSLPreferencesDialog extends javax.swing.JFrame {

    private boolean haveChangesBeenMade = false;
    private boolean uSsl;
    private String  trustStoreFilePath;
    private String  trustStorePword;
    
    /**
     * The constructor initialises the dialog components, sets the SSL 
     * configuration values, sets the dialog position and then displays it to 
     * the user.  The parent component parameter is used to position this dialog 
     * on top of its parent.  This provides good user graphical interaction.  
     * @param parentComp The parent component used in the positioning of this 
     * component.  
     */
    public SSLPreferencesDialog(PreferencesDialog parentComp){
        initComponents();
        this.uSsl = Configuration.useSecureComms;
        // This dialog is setup by default to NOT use SSL so change if necessary.
        if (this.uSsl)
            this.setGuiUseSsl(true);
        this.setDialogPosition(parentComp); // NOTE: GUI is not the parent.
        this.setPreferenceValues();
        this.setVisible(true);
    }
    
    /**
     * Returns whether or not SSL preference changes have been made by the 
     * user.  
     * @return True if changes have been made, false otherwise.  
     */
    public boolean haveChangesBeenMade(){
        return this.haveChangesBeenMade;
    }
    
    /**
     * Returns whether or not the user wishes to use secure SSL communications.
     * @return True if secure communications should be used, false otherwise.  
     */
    public boolean getUseSsl(){
        return this.uSsl;
    }
    
    /**
     * Returns a string containing the trust store file path.
     * @return A string containing the trust store file path.
     */
    public String getTrustStoreFilePath(){
        return this.trustStoreFilePath;
    }
    
    /**
     * Returns a string containing the trust store password.
     * @return A string containing the trust store password.
     */
    public String getTrustStorePassword(){
        return this.trustStorePword;
    }
    
    /**
     * This method sets the dialog position using the parent component to 
     * position itself on top of the parent.  
     * @param parentComp The parent component used to position this dialog on 
     * top of.  
     */
    private void setDialogPosition(PreferencesDialog parentComp){
        Point pos = parentComp.getLocation();
        int px = (int)(pos.getX());
        int py = (int)(pos.getY());
        int x = (px + (parentComp.getWidth() / 2) - (this.getWidth() / 2));
        int y = 
              (py + (parentComp.getHeight() / 2) - (this.getHeight() / 2));
        Point newPos = new Point(x, y);
        this.setLocation(newPos);
    }
    
    /**
     * Graphically sets this dialog to either use or not use secure 
     * communications by enabling or disabling certain components such as the 
     * password and file selection button.  
     * @param enabled True if the dialog should be configured to use secure 
     * communications, false otherwise.  
     */
    private void setGuiUseSsl(boolean enabled){
        this.trustStoreFileSelect.setEnabled(enabled);
        this.trustStorePassword.setEnabled(enabled);
        this.trustStoreFileName.setEnabled(enabled);
        this.sslTrustStoreLabel.setEnabled(enabled);
        this.sslTrustStorePasswordLabel.setEnabled(enabled);
    }
    
    /**
     * This method sets the dialog SSL preference values to that of the SSL 
     * configuration values.  This method is called before the SSL preferences 
     * dialog is displayed.  
     */
    private void setPreferenceValues(){
        this.useSsl.setSelected(Configuration.useSecureComms);
        this.trustStoreFileName.setText(this.getTrustStoreFileName());
        this.trustStorePassword.setText(Configuration.trustStorePassword);
    }
    
    /**
     * This method is used to return the file name only, from the user selected 
     * file path of the SSL trust store.  The code used is cross platform.  
     * @return The user selected file name of the SSL trust store.  
     */
    private String getTrustStoreFileName(){
        String filePath = Configuration.trustStore.trim();
        String origFilePath = filePath;
        
        try {
            /*
            * Determine the file separater, we can't use the system property here
            * because the config file path might have been set on Windows while 
            * this client is running on Linux.  Therefore we use String methods.
            */
            String sep = "\\"; // Black slash (Windows).
            if (filePath.lastIndexOf(sep) == -1){
                sep = "/"; // Forward slash (Linux).
                if (filePath.lastIndexOf(sep) == -1)
                    throw new IndexOutOfBoundsException("Bad file path");
            }
            
            // Get the filename.
            String fileName = filePath.substring(filePath.lastIndexOf(sep) + 1);
            if (fileName.isEmpty())
                throw new IndexOutOfBoundsException("Bad file path");
            return fileName;
        }
        catch (IndexOutOfBoundsException ioobe){
            return origFilePath; // Return the full file path if an error occurs.
        }
    }
    
    /**
     * This method is used to 'save' the user SSL preferences.  This is done 
     * by assigning the chosen values to instance variables.  The 
     * PreferencesDialog can then write the instance variable values to the 
     * configuration file after the SSL preferences dialog has been disposed.  
     */
    private void savePreferences(){
        boolean b0 = false;
        String s1 = "";
        String s2 = "";
        
        try {
            b0 = this.useSsl.isSelected();
            if (b0){
                s1 = this.trustStoreFilePath;
                s2 = new String(this.trustStorePassword.getPassword()); 
            }
        }
        catch (Exception ex){
            this.dispose();
            this.showErrorDialog();
            return;
        }
        
        /* The instance vars below are accessed by the PreferencesDialog after 
         * this dialog has been disposed.  The values are then written to 
         * the config file by the PreferencesDialog instance.  
         */
        uSsl = b0;
        if (b0){
            trustStoreFilePath = s1;
            trustStorePword = s2;
        }
        
        this.haveChangesBeenMade = true;
        this.dispose();
    }
    
    /**
     * This method displays an error dialog to the user.  The error dialog 
     * alerts the user as to a format error with their entered SSL preference 
     * values.  
     */
    private void showErrorDialog(){
        String text = "Format error, your changes have not been saved";
        JOptionPane.showMessageDialog(this, text,
                "Preferences Error", JOptionPane.OK_OPTION);
    }

    /**
     * Generated code from Netbeans Swing, used to provide the component layout 
     * of this dialog.  
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sslPreferencesLabel = new javax.swing.JLabel();
        useSslLabel = new javax.swing.JLabel();
        useSsl = new javax.swing.JCheckBox();
        sslTrustStoreLabel = new javax.swing.JLabel();
        trustStoreFileName = new javax.swing.JTextField();
        trustStoreFileSelect = new javax.swing.JButton();
        sslTrustStorePasswordLabel = new javax.swing.JLabel();
        trustStorePassword = new javax.swing.JPasswordField();
        apply = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("SSL");
        setAlwaysOnTop(true);
        setResizable(false);

        sslPreferencesLabel.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        sslPreferencesLabel.setText("SSL Preferences");

        useSslLabel.setText("Use SSL :");

        useSsl.setToolTipText("Use SSL?");
        useSsl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useSslActionPerformed(evt);
            }
        });

        sslTrustStoreLabel.setText("SSL Trust Store : ");

        trustStoreFileName.setEditable(false);
        trustStoreFileName.setToolTipText("SSL Trust Store File");

        trustStoreFileSelect.setText("...");
        trustStoreFileSelect.setToolTipText("Select Trust Store File");
        trustStoreFileSelect.setEnabled(false);
        trustStoreFileSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustStoreFileSelectActionPerformed(evt);
            }
        });

        sslTrustStorePasswordLabel.setText("SSL Trust Store Password : ");

        trustStorePassword.setToolTipText("Trust Store Password");
        trustStorePassword.setEnabled(false);

        apply.setText("Apply");
        apply.setToolTipText("Apply Changes");
        apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyActionPerformed(evt);
            }
        });

        cancel.setText("Cancel");
        cancel.setToolTipText("Cancel Changes");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(apply, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                        .addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sslPreferencesLabel)
                            .addComponent(sslTrustStoreLabel)
                            .addComponent(sslTrustStorePasswordLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(trustStorePassword, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                    .addComponent(trustStoreFileName, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(useSslLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(trustStoreFileSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(useSsl))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sslPreferencesLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useSsl)
                    .addComponent(useSslLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sslTrustStoreLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(trustStoreFileSelect, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(trustStoreFileName))
                .addGap(9, 9, 9)
                .addComponent(sslTrustStorePasswordLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(trustStorePassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(apply)
                    .addComponent(cancel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action event method for the cancel button.  Used to dispose this dialog 
     * and have no further effect on the configuration values.  
     * @param evt The dialog event.  
     */
    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        this.dispose();
    }//GEN-LAST:event_cancelActionPerformed

    /**
     * Action event method for the apply button.  Used to save the users SSL 
     * preferences by calling the savePreferences() method.  
     * @param evt The dialog event.  
     */
    private void applyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyActionPerformed
        this.savePreferences();
    }//GEN-LAST:event_applyActionPerformed

    /**
     * Action event method for the trust store file select button.  Displays a 
     * file select dialog to the user to locate the SSL trust store.  The file 
     * select dialog is a JFileChooser object.  
     * @param evt 
     */
    private void trustStoreFileSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustStoreFileSelectActionPerformed
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showDialog(this, "Select File");
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            this.trustStoreFilePath = file.getAbsolutePath();
            this.trustStoreFileName.setText(file.getName());
        }
    }//GEN-LAST:event_trustStoreFileSelectActionPerformed

    /**
     * Action event method for the use SSL check box.  Graphically configures 
     * the SSL preferences dialog to either use or not use secure 
     * communications.  
     * @param evt The dialog event.  
     */
    private void useSslActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useSslActionPerformed
        if (this.useSsl.isSelected())
            this.setGuiUseSsl(true);
        else {
            this.setGuiUseSsl(false);
        }
    }//GEN-LAST:event_useSslActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apply;
    private javax.swing.JButton cancel;
    private javax.swing.JLabel sslPreferencesLabel;
    private javax.swing.JLabel sslTrustStoreLabel;
    private javax.swing.JLabel sslTrustStorePasswordLabel;
    private javax.swing.JTextField trustStoreFileName;
    private javax.swing.JButton trustStoreFileSelect;
    private javax.swing.JPasswordField trustStorePassword;
    private javax.swing.JCheckBox useSsl;
    private javax.swing.JLabel useSslLabel;
    // End of variables declaration//GEN-END:variables
}