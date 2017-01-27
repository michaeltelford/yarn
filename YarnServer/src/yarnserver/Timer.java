
package yarnserver;

/**
 * Timer class which can be used for any form of timing scenario.  This class 
 * is used as a connection timer for the server.  When the timer expires the 
 * server contacts the client with a connection request.  A reply is required 
 * in order for the client to remain connected.  A timer instance is associated 
 * with each client connection thread.  
 * 
 * This class extends Thread in order to 'start' the timer once a timeout has 
 * been set.
 * @author Michael Telford
 */
public class Timer extends Thread {
    
    private int     timeoutInSeconds;
    private boolean hasTimeoutExpired = false;
    
    /**
     * Class constructor which takes a timeout (in seconds) parameter.  This 
     * timeout tells the class instance how long the timer should run for.  The 
     * hasTimeoutExpired instance variable is used to determine whether or not 
     * the timer has expired or not.  
     * @param timeoutInSeconds The timer duration in seconds.  
     */
    public Timer(int timeoutInSeconds){
        super();
        this.timeoutInSeconds = (timeoutInSeconds * 1000);
    }
    
    /**
     * This method returns the hasTimerExpired boolean instance variable.  This 
     * boolean flag is used to determine whether or not the timer has stopped. 
     * For example this method is called by each client connection and if true 
     * is returned then the server sends a connection request to the corresponding 
     * client.  
     * @return True if the timer has stopped/expired, otherwise false.  
     */
    public boolean hasTimerExpired(){
        return this.hasTimeoutExpired;
    }
    
    /**
     * This method is responsible for starting the timer thread.  The duration 
     * depends upon the timeoutInSeconds variable value passed to the class 
     * constructor.  
     * 
     * This method simply sets the hasTimeoutExpired variable to false, waits 
     * for the given duration of timeoutInSeconds and then sets the 
     * hasTimeoutExpired variable to true.  It is up to the calling class to 
     * check the value of the hasTimeoutExpired variable.
     */
    @Override
    public void run(){
        try {
            this.hasTimeoutExpired = false;
            Thread.sleep(this.timeoutInSeconds);
            this.hasTimeoutExpired = true;
        } 
        catch (InterruptedException ex){
            this.hasTimeoutExpired = true;
        }
    }
}