/**
 * 
 */

import java.io.*;
import javax.sound.sampled.*;
import java.util.Map;
import org.tritonus.share.sampled.file.TAudioFileFormat;
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.jl.decoder.*;
//import javax.sound.sampled.*;

//import org.jaudiotagger.audio.AudioFileIO;
// import org.jaudiotagger.audio.AudioFile;
//import org.jaudiotagger.audio.mp3.MP3AudioHeader;


/**
 * @author tk
 *
 */
public class TestMP3Duration {

	/**
	 * 
	 */
	public TestMP3Duration() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{

		try {
			File file;
			long totalduration = 0;
		
        	for(int i = 1; i < 15; i++)
        	{
				file = new File(".\\20130825\\daisy3\\speechgen00" +(i < 10 ? "0" :"") +i +".mp3");
				System.out.println("MP3 file: " +file.getAbsolutePath());
				AudioFileFormat audioFileFormat = AudioSystem.getAudioFileFormat(file);
				if (audioFileFormat instanceof TAudioFileFormat)
				{
				    // Tritonus SPI compliant audio file format.
				    Map properties = ((TAudioFileFormat) audioFileFormat).properties();
				    // duration is in microseconds
				    Long microseconds = (Long) properties.get("duration");
			        int mili = (int) (microseconds / 1000);
			        int sec = (mili / 1000) % 60;
			        int min = (mili / 1000) / 60;
			        System.out.println("MP3 time = " + min + ":" + sec);				    
				    totalduration += microseconds;
			        mili = (int) (totalduration / 1000);
			        sec = (mili / 1000) % 60;
			        min = (mili / 1000) / 60;
				    System.out.println("1 MP3 total durcation: " +totalduration);
				    System.out.println("1 MP3 total time = " + min + ":" + sec);
				}				
        	}
        	
		}catch(Exception e){
			e.printStackTrace();
		}		
		
		/*
		// Currently, you can use the following hack with the JLayer decoder:
		public class TestMP3Duration
		{
		    public static void main(String args[])
		    {
		    */

		        try
		        {
			        	File f = new File(".\\20130825\\daisy3\\speechgen0014.mp3");
			    		Bitstream m_bitstream = new Bitstream(
			                              new FileInputStream(f));
			            Header m_header = m_bitstream.readFrame();

			            int mediaLength = (int)f.length();
			 
			            int nTotalMS = 0;
			            if (mediaLength != AudioSystem.NOT_SPECIFIED) {
			               nTotalMS = Math.round(m_header.total_ms(mediaLength));
			            }		        		

		            System.out.println("2 Length in ms: " + nTotalMS);
		            
			        f = new File(".\\20130825\\daisy3\\speechgen0014.mp3");
		    		m_bitstream = new Bitstream(
		                            new FileInputStream(f));
		            
		    		int bytes = 0;
		            m_bitstream.get_bits(bytes);
		            System.out.println("Length in bytes: " + bytes);
			        long bits = bytes * 8;
			        int krate = 64;
			        long brate = krate * 1024;
			        long seconds = bits / brate;
			        long minutes = seconds / 60;
			        seconds = seconds -(minutes * 60);
			        System.out.println("3 Length in ms: " + seconds);
			        System.out.println("3 Length in min: " + minutes);		        
			        // minutes +':' +seconds;  
			
		        } catch(Exception e) {
		            e.printStackTrace();
		        } 
		        
	    // }
		// }
	}
}