
package yarncli;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.InputMismatchException;

/**
 * Main class used for providing the console interface and sending data.
 * Relies on the Network class for the sending of textual data and handling
 * connections to Yarn servers.
 * @author Michael Telford
 */
public class Main {

    private static final String HELP_MSG =
            "\nGive a server address argument to attempt a connection\n"
            + "Type -q to quit and exit once connected\n"
            + "Type -c to see how many clients are connected\n"
            + "Type -n to see the usernames of any connected clients\n"
            + "Type -h for help once connected or use as an argument\n\n";
    
    private static boolean isLoggedIn = false;
    private static String address = "";
    
    private static final InputStreamReader INPUT = new InputStreamReader(System.in);
    private static BufferedReader in = new BufferedReader(INPUT);
    private static Connection conn = null;

    // Used to set text in the CLI via other classes e.g. Connection class.
    public static void setReceivedText(String text){
        System.out.println("--> " + text);
        if (text.contains("Start typing to have a yarn"))
            Main.isLoggedIn = true;
    }

    // Used to locate and update variables via the config.txt file.
    private static void config() throws Exception {
        // Update config details for Connection class.
        try {
            Configuration.findFile();
        }
        catch (FileNotFoundException fnfe){
            try {
                Configuration.findFileGUI();
            }
            catch (FileNotFoundException fnfex){
                System.out.println("Configuration file not located");
                System.exit(0);
            }
        }
        try {
            Configuration.updateConfigDetails();
        }
        catch (InputMismatchException ime){
            System.out.println("Configuration file format is not correct...");
            System.exit(0);
        }
    }

    // Check user arguments at run time before connection.
    private static void userArgs(String[] args){
        // Display shell and print welcome message.
        System.out.println("\nWelcome to Yarn CLI Client " + 
                Configuration.VERSION + "\n" + "Type -h for help\n");
        // Check user arguments.
        if (args.length == 1){
            String arg = args[0];
            if (arg.equalsIgnoreCase("-h")){
                System.out.print(Main.HELP_MSG);
                System.exit(0);
            }
            else { // Else anything but '-h' is taken as a server address.
                Main.address = arg;
            }
        }
        else {
            System.out.println("Incorrect arguments given, type the server "
                    + "address or type '-h' for more help");
            System.exit(0);
        }
    }

    // Bind (optional), connect and start receive thread.
    private static void bindConnectReceive(){
        try {
            Main.conn = new Connection();
            //Main.conn.bind(); (Optional) // Port not available on Uni network.
            System.out.println("Attempting to connect to Yarn server " +
                    Main.address);
            Main.conn.connect(Main.address);
            Thread t = new Thread(Main.conn);
            t.start();
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

    /**
     * @param args Enter the address of the Yarn Server you wish to connect to,
     * or type -h to see the help menu.
     */
    public static void main(String[] args) throws Exception {

        // Update config data.
        Main.config();
        // Check user arguments.
        Main.userArgs(args);
        // Bind (optional), connect and start receive thread.
        Main.bindConnectReceive();
        // Delay for server connection response is in Connection run() method.

        // Allows to send text continously once connected.
        while (true){ // Send loop.
            try {
                // No cursor is used for sending text, only receiving.
                String text = in.readLine();

                if (text.equalsIgnoreCase("-h") && Main.isLoggedIn) // Help.
                    System.out.print(Main.HELP_MSG);

                else if (text.equalsIgnoreCase("-q")){ // Quit.
                    Main.conn.close();
                    System.out.println("Disconnected successfully...\nSee ya!");
                    break; // exits program.
                }
                
                else if (text.equalsIgnoreCase("")){ // Nothing Typed.
                    // Nothing happens when no text is entered.
                }

                else // Send text.
                    Main.conn.send(text);
            }

            // For disconnecting from a server.
            catch (SocketException se){
                // Nothing happens.
            }

            // All exceptions are printed and then the program re-enters the
            // send loop (while true) for defensive programming purposes.
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }

        } // Closes while true loop.
        System.exit(1);

    } // Closes Main method.
} // Closes Main class.