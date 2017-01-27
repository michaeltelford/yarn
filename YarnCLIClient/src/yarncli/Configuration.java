
package yarncli;

import java.io.File;
import java.util.Scanner;
import javax.swing.JFileChooser;

/**
 * Used to store configuration class variables for use by other classes.  This
 * enables a single variable to be changed by a single change in this class,
 * eliminating the chance of forgetting to change one instance of the variable
 * in each using class.  The class variables consist of general configuration
 * details such as networking ports for the different Yarn services and max
 * character limit for receiving text.
 * @author Micky Telford
 */
public abstract class Configuration {

    public static final double VERSION = 1.2;

    // Configuration details.
    public static int maxTextChars;
    public static int localPort;
    public static int sendToPort; // Server listening port.
    public static int connectTimeout;
    public static int sendTimeout;
    public static int receiveTimeout;

    // Class variables.
    private static File file = null;
    private static Scanner in = null;

    // No constructor due to abstract class.

    public static void findFile() throws Exception {
        Configuration.file = new File("config.txt");
        Configuration.in = new Scanner(Configuration.file);
    }

    public static void findFileGUI() throws Exception {
        System.out.println("Locating config.txt file with GUI now...");
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION){
            Configuration.file = chooser.getSelectedFile();
        }
        Configuration.in = new Scanner(Configuration.file);
    }

    // Should only be called after the File has been located and scanned.
    public static void updateConfigDetails() throws Exception {

        // Get appropiate variable values.
        Configuration.in.next();
        Configuration.maxTextChars = Configuration.in.nextInt();
        Configuration.in.next();
        Configuration.in.next();
        Configuration.localPort = Configuration.in.nextInt();
        Configuration.in.next();
        Configuration.in.next();
        Configuration.sendToPort = Configuration.in.nextInt();
        Configuration.in.next();
        Configuration.in.next();
        Configuration.connectTimeout = Configuration.in.nextInt();
        Configuration.in.next();
        Configuration.in.next();
        Configuration.sendTimeout = Configuration.in.nextInt();
        Configuration.in.next();
        Configuration.in.next();
        Configuration.receiveTimeout = Configuration.in.nextInt();
        Configuration.in.next();

    }

    // For testing purposes only, call after updateConfigDetails().
    public static void printConfigDetails(){
        System.out.println(Configuration.maxTextChars);
        System.out.println(Configuration.localPort);
        System.out.println(Configuration.sendToPort);
        System.out.println(Configuration.connectTimeout);
        System.out.println(Configuration.sendTimeout);
        System.out.println(Configuration.receiveTimeout);
    }
}