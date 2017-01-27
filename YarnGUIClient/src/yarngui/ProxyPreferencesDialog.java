
package yarngui;

import java.awt.Point;
import javax.swing.JOptionPane;

/**
 * This class extends JFrame to provide a proxy preferences dialog.  This proxy 
 * dialog is used in conjunction with the 'standard' preferences dialog.  This 
 * dialog obviously only contains configuration preferences applied to the 
 * client proxy connection setup.  
 * @author Michael Telford
 */
public class ProxyPreferencesDialog extends javax.swing.JFrame {
    
    private boolean haveChangesBeenMade = false;
    private boolean uProxy = Configuration.useProxy;
    private String  pAddr;
    private int     pPort  = 0;
    private String  pUname;
    private String  pPword;
    
    /**
     * The constructor initialises the dialog components, sets the proxy 
     * configuration values, sets the dialog position and then displays it to 
     * the user.  The parent component parameter is used to position this dialog 
     * on top of its parent.  This provides good user graphical interaction.  
     * @param parentComp The parent component used in the positioning of this 
     * component.  
     */
    public ProxyPreferencesDialog(PreferencesDialog parentComp){
        initComponents();
        if (Configuration.useProxy)
            this.setGuiUseProxy(true);
        if (Configuration.useProxyAuthentication())
            this.setGuiUseAuthentication(true);
        this.setDialogPosition(parentComp); // NOTE: GUI is not the parent.
        this.setPreferenceValues();
        this.setVisible(true);
    }
    
    /**
     * Returns whether or not proxy preference changes have been made by the 
     * user.  
     * @return True if changes have been made, false otherwise.  
     */
    public boolean haveChangesBeenMade(){
        return haveChangesBeenMade;
    }
    
    /**
     * Returns whether or not the user wishes to use a proxy.
     * @return True if a proxy should be used, false otherwise.  
     */
    public boolean getUseProxy(){
        return uProxy;
    }
    
    /**
     * Returns a string containing the proxy address.
     * @return A string containing the proxy address.
     */
    public String getProxyAddress(){
        return pAddr;
    }
    
    /**
     * Returns an integer containing the proxy port.
     * @return An integer containing the proxy port.
     */
    public int getProxyPort(){
        return pPort;
    }
    
    /**
     * Returns a string containing the proxy username.
     * @return A string containing the proxy username.
     */
    public String getProxyUsername(){
        return pUname;
    }
    
    /**
     * Returns a string containing the proxy password.
     * @return A string containing the proxy password.
     */
    public String getProxyPassword(){
        return pPword;
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
        int y = (py + (parentComp.getHeight() / 2) - (this.getHeight() / 2));
        Point newPos = new Point(x, y);
        this.setLocation(newPos);
    }
    
    /**
     * Graphically sets this dialog to either use or not use a proxy by 
     * enabling or disabling certain components such as the proxy address and 
     * port fields.  
     * @param enabled True if the dialog should be configured to use a proxy, 
     * false otherwise.  
     */
    private void setGuiUseProxy(boolean enabled){
        this.proxyAddress.setEnabled(enabled);
        this.proxyPort.setEnabled(enabled);
        this.proxyAddressLabel.setEnabled(enabled);
        this.proxyPortLabel.setEnabled(enabled);
    }
    
    /**
     * Graphically sets this dialog to either use or not use proxy 
     * authentication by enabling or disabling certain components such as the 
     * proxy username and password fields.  
     * @param enabled True if the dialog should be configured to use proxy 
     * configuration, false otherwise.  
     */
    private void setGuiUseAuthentication(boolean enabled){
        this.proxyUsername.setEnabled(enabled);
        this.proxyPassword.setEnabled(enabled);
        this.proxyUsernameLabel.setEnabled(enabled);
        this.proxyPasswordLabel.setEnabled(enabled);
    }
    
    /**
     * This method is used to display a proxy port format error to the user 
     * using the JOptionPane showMessageDialog() method.  
     */
    private void showErrorDialog(){
        String text = "Check that you've correctly entered a port number";
        JOptionPane.showMessageDialog(this, text,
                "Preferences Error", JOptionPane.OK_OPTION);
    }
    
    /**
     * This method sets the dialog proxy preference values to that of the proxy 
     * configuration values.  This method is called before the proxy preferences 
     * dialog is displayed.  
     */
    private void setPreferenceValues(){
        this.useProxy.setSelected(Configuration.useProxy);
        this.proxyAddress.setText(Configuration.proxyAddr);
        this.proxyPort.setText(String.valueOf(Configuration.proxyPort));
        this.useAuthentication.setSelected(Configuration.useProxyAuthentication());
        this.proxyUsername.setText(Configuration.proxyUname);
        this.proxyPassword.setText(Configuration.proxyPword);
    }
    
    /**
     * This method is used to 'save' the user proxy preferences.  This is done 
     * by assigning the chosen values to instance variables.  The 
     * PreferencesDialog can then write the instance variable values to the 
     * configuration file after the proxy preferences dialog has been disposed.  
     */
    private void savePreferences(){
        boolean b0 = false;
        String s1 = "";
        int i1 = 0;
        boolean b1 = false;
        String s2 = "";
        String s3 = "";
        
        try {
            b0 = this.useProxy.isSelected();
            if (b0){
                s1 = this.proxyAddress.getText();
                i1 = Integer.parseInt(this.proxyPort.getText());
            }
            b1 = this.useAuthentication.isSelected();
            if (b1){
                s2 = this.proxyUsername.getText();
                s3 = new String(this.proxyPassword.getPassword());
            }
        }
        catch (NumberFormatException nfe){
            this.dispose();
            this.showErrorDialog();
            return;
        }
        
        /* The instance vars below are accessed by the PreferencesDialog after 
         * this dialog has been disposed.  The values are then written to 
         * the config file by the PreferencesDialog instance.  
         */
        uProxy = b0;
        if (b0){
            pAddr = s1;
            pPort = i1;
        }
        if (b1){
            pUname = s2;
            pPword = s3;
        }
        
        this.haveChangesBeenMade = true;
        this.dispose();
    }

    /**
     * Generated code from Netbeans Swing, used to provide the component layout 
     * of this dialog.  
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        useProxyLabel = new javax.swing.JLabel();
        useProxy = new javax.swing.JCheckBox();
        proxyAddressLabel = new javax.swing.JLabel();
        proxyAddress = new javax.swing.JTextField();
        proxyPortLabel = new javax.swing.JLabel();
        proxyPort = new javax.swing.JTextField();
        useAuthenticationLabel = new javax.swing.JLabel();
        useAuthentication = new javax.swing.JCheckBox();
        proxyUsernameLabel = new javax.swing.JLabel();
        proxyUsername = new javax.swing.JTextField();
        proxyPasswordLabel = new javax.swing.JLabel();
        cancel = new javax.swing.JButton();
        apply = new javax.swing.JButton();
        proxyPassword = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Proxy");
        setAlwaysOnTop(true);
        setResizable(false);

        label.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        label.setText("Proxy Preferences");

        useProxyLabel.setText("Use Proxy :");

        useProxy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useProxyActionPerformed(evt);
            }
        });

        proxyAddressLabel.setText("Proxy Address :");
        proxyAddressLabel.setEnabled(false);

        proxyAddress.setEnabled(false);

        proxyPortLabel.setText("Proxy Port :");
        proxyPortLabel.setEnabled(false);

        proxyPort.setEnabled(false);

        useAuthenticationLabel.setText("Use Authentication :");

        useAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useAuthenticationActionPerformed(evt);
            }
        });

        proxyUsernameLabel.setText("Proxy Username :");
        proxyUsernameLabel.setEnabled(false);

        proxyUsername.setEnabled(false);

        proxyPasswordLabel.setText("Proxy Password :");
        proxyPasswordLabel.setEnabled(false);

        cancel.setText("Cancel");
        cancel.setToolTipText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        apply.setText("Apply");
        apply.setToolTipText("Apply");
        apply.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyActionPerformed(evt);
            }
        });

        proxyPassword.setEnabled(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useProxyLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(useProxy))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(useAuthenticationLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(useAuthentication))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(proxyPortLabel)
                            .addComponent(proxyAddressLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 70, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(proxyPort, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(proxyAddress)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(proxyPasswordLabel)
                            .addComponent(proxyUsernameLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(proxyUsername, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(proxyPassword)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(apply, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cancel, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(useProxyLabel)
                    .addComponent(useProxy))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyAddressLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyPortLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(useAuthentication)
                    .addComponent(useAuthenticationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proxyUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(proxyUsernameLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proxyPasswordLabel)
                    .addComponent(proxyPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancel)
                    .addComponent(apply))
                .addContainerGap(16, Short.MAX_VALUE))
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
     * Action event method for the apply button.  Used to save the users proxy 
     * preferences by calling the savePreferences() method.  
     * @param evt The dialog event.  
     */
    private void applyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyActionPerformed
        this.savePreferences();
    }//GEN-LAST:event_applyActionPerformed

    /**
     * Action event method for the use proxy check box.  Graphically configures 
     * the dialog to either use or not use a proxy depending on whether the box 
     * is checked or not.  In turn certain dialog components are enabled or 
     * disabled.  
     * @param evt The dialog event.  
     */
    private void useProxyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useProxyActionPerformed
        if (this.useProxy.isSelected())
            this.setGuiUseProxy(true);
        else {
            this.setGuiUseProxy(false);
            this.useAuthentication.setSelected(false);
            this.setGuiUseAuthentication(false);
        }
    }//GEN-LAST:event_useProxyActionPerformed

    /**
     * Action event method for the use proxy authentication check box.  
     * Graphically configures the dialog to either use or not use proxy 
     * authentication depending on whether the box is checked or not.  In turn 
     * certain dialog components are enabled or disabled.  
     * @param evt The dialog event.  
     */
    private void useAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useAuthenticationActionPerformed
        if (this.useAuthentication.isSelected()){
            this.setGuiUseAuthentication(true);
            this.useProxy.setSelected(true);
            this.setGuiUseProxy(true);
        }
        else 
            this.setGuiUseAuthentication(false);
    }//GEN-LAST:event_useAuthenticationActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton apply;
    private javax.swing.JButton cancel;
    private javax.swing.JLabel label;
    private javax.swing.JTextField proxyAddress;
    private javax.swing.JLabel proxyAddressLabel;
    private javax.swing.JPasswordField proxyPassword;
    private javax.swing.JLabel proxyPasswordLabel;
    private javax.swing.JTextField proxyPort;
    private javax.swing.JLabel proxyPortLabel;
    private javax.swing.JTextField proxyUsername;
    private javax.swing.JLabel proxyUsernameLabel;
    private javax.swing.JCheckBox useAuthentication;
    private javax.swing.JLabel useAuthenticationLabel;
    private javax.swing.JCheckBox useProxy;
    private javax.swing.JLabel useProxyLabel;
    // End of variables declaration//GEN-END:variables
}