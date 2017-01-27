
package yarngui;

import javax.sound.sampled.*;
import java.io.*;
import java.net.URL;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Sound recorder class used to record and play voice recordings.  This class is 
 * used by the voice recorder dialog.  The only supported file format is '.WAV'. 
 * @author Michael Telford
 */
public class VoiceRecorder {
    
    private File recordWavFile;   // Set by setRecordWavFilePath method below.
    private long recordTimeLimit; // Set by VoiceRecorderDialog constructor.
    private TargetDataLine dataLine;
    private static AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
    
    /**
     * Constructor used to set the voice recording file path.
     * @param recordWavFilePath The absolute voice recording file path.  
     */
    public VoiceRecorder(String recordWavFilePath){
        this.setRecordWavFilePath(recordWavFilePath);
    }
    
    /**
     * Get method which returns the audio format used in the recording of 
     * voice shares.  
     * @return An AudioFormat instance.  
     */
    private AudioFormat getAudioFormat(){
        float sampleRate = 16000;
        int sampleSizeInBits = 8;
        int channels = 2;
        boolean signed = true;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(sampleRate, sampleSizeInBits,
                                             channels, signed, bigEndian);
        return format;
    }
    
    /**
     * Set method used to set the voice recording absolute file path including 
     * the file name and .WAV extension (only supported extension).
     * "temp_folder" can be used to locate the OS's temporary file directory. 
     * The temp folder location code is cross platform.  
     * @param filePathAndName The absolute file path for the voice recording. 
     */
    public void setRecordWavFilePath(String filePathAndName){
        // Init the filepath - special case for "temp_folder".
        if (filePathAndName.equals("temp_folder"))
            filePathAndName = System.getProperty("java.io.tmpdir") +
                    System.getProperty("file.separator") + "VoiceRecording.wav";
        this.recordWavFile = new File(filePathAndName);
        
        // Check the file path isn't a problem.
        try {
            this.recordWavFile.createNewFile();
            this.recordWavFile.deleteOnExit();
        } catch (IOException ex) {
            //Logger.getLogger(VoiceRecorder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Get method which returns the voice recording absolute file path.  
     * @return The voice recording absolute file path. 
     */
    public String getRecordWavFilePath(){
        return this.recordWavFile.getAbsolutePath();
    }
    
    /**
     * Get method which returns the current record time frame in milli seconds. 
     * @return The current record time frame in milli seconds.
     */
    public int getRecordTimeLimitInMilliSeconds(){
        return (int) this.recordTimeLimit;
    }
    
    /**
     * Get method which returns the current record time frame in seconds. 
     * @return The current record time frame in seconds.
     */
    public int getRecordTimeLimitInSeconds(){
        return (int) (this.recordTimeLimit / 1000);
    }
    
    /**
     * Set method which sets the current record time frame to the given time 
     * limit in seconds.  
     * @param timeLimitInSeconds the desired record time frame in seconds.  
     */
    public void setRecordTimeLimitInSeconds(int timeLimitInSeconds){
        this.recordTimeLimit = (timeLimitInSeconds * 1000);
    }
 
    /**
     * This method is used to capture the sound recording through an input 
     * device and write the data to a .WAV file.  The file path is contained in 
     * the recordWavFile variable.  
     */
    public void startRecording() throws Exception {
        AudioFormat format = this.getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        // Checks if system supports the data line.
        if (!AudioSystem.isLineSupported(info)){
            throw new Exception("Line not supported");
        }
        dataLine = (TargetDataLine) AudioSystem.getLine(info);
        dataLine.open(format);
        dataLine.start();

        AudioInputStream ais = new AudioInputStream(dataLine);
        AudioSystem.write(ais, fileType, recordWavFile);
    }
 
    /**
     * Stops the voice recording.
     */
    public void stopRecording(){
        dataLine.stop();
        dataLine.close();
    }
    
    /**
     * Plays the recorded .WAV file.  The full file path is contained in the 
     * recordWavFile variable.  
     */
    public void playSoundRecording() throws Exception {
        URL soundFilePath = this.recordWavFile.toURI().toURL();
        InputStream input = new FileInputStream(soundFilePath.getFile());
        AudioStream audioStream = new AudioStream(input); 
        AudioPlayer.player.start(audioStream);
    }
    
    /**
     * Get method used to retrieve the recording playback delay or recording 
     * time limit.  This integer value is calculated by analysing the recorded 
     * .WAV file and its total size.  
     * @return The length of the voice recording file.  
     * @throws Exception If an underlying audio format error occurs.  
     */
    public int getRecordingPlaybackDelay() throws Exception {
        AudioInputStream audioInputStream = 
                            AudioSystem.getAudioInputStream(this.recordWavFile);
        AudioFormat format = audioInputStream.getFormat();
        long frames = audioInputStream.getFrameLength();
        double durationInSeconds = (frames+0.0) / format.getFrameRate();
        return (int)(durationInSeconds * 1000);
    }
}