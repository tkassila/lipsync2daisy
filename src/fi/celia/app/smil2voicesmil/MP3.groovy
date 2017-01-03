package fi.celia.app.smil2voicesmil

// import java.io.*;
// import javazoom.jl.decoder.*;
import javax.sound.sampled.*;

//import java.io.*;

import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

import org.tritonus.share.sampled.file.TAudioFileFormat
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.mp3.MP3AudioHeader

/**
 * This class is capable to lengths of mp3 voice files by using two kinds of
 * mp3 java libraries. This class is used to produce more exact total end time
 * values for produced total end time of a voice file. This class is used whend
 * a user has been selected a gui element. By doing that she/he will calculate
 * differently total elapsed times for a run.
 * 
 * Tämä luokka palauttaa MP3-tiedoston media pituuden tai tiedoston
 * ajan millisekunteina. Käytetty kahta java .jar kirjastoa. Kumminkin
 * antamat ajat .mp3 tiedoston äänen pituudelle sekunteina poikkeavat
 * daisy3 (pipeline) validaattorin ilmoittamista totalelapsedtieme
 * ajoista.
 * 
 * @author Tuomas Kassila
 *
 */
class MP3 {
	
	/*
	 * Tämä methodi palauttaa MP3-tiedoston media pituuden
	 */
	public static int mediaLengthOfMp3File(String mp3fileName)
	{
		def f = new File(mp3fileName)
		mediaLengthOfMp3File(f)
	}
	
	/**
	 * Tämä methodi palauttaa MP3-tiedoston media pituuden
	 * 
	 * @param mp3file Kyseinen mp3 tiedosto filena
	 * @return pituus. Jos -1, niin tapahtunut virhe, exception
	 */
	/*
	public static int mediaLengthOfMp3File(File mp3file)
	{
		try
		{
			Bitstream m_bitstream = new Bitstream(
							  new FileInputStream(mp3file));
			Header m_header = m_bitstream.readFrame();

			int mediaLength = (int)mp3file.length();
			return mediaLength
		} catch(Exception e) {
			e.printStackTrace();
			return -1
		}
	}
	*/

	/**
	 * Tämä methodi palauttaa MP3-tiedoston pituuden millisekunteina.
	 * 
	 * @param mp3fileName luettava tiedosto
	 * @return millisekuntit. Jos -1, niin tapahtunut virhe, exception
	 */
	public static int millisecondsOfMp3File(String mp3fileName)
	throws FileNotFoundException, Exception
	{
		def f = new File(mp3fileName)
		if (!f.exists())
			throw new FileNotFoundException(f.getAbsolutePath());
		millisecondsOfMp3File(f)
	}
	
	public static int millisecondsOfMp3File(File fMp3) 
	{
		millisecondsOfMp3FileOfJaudiotagger(fMp3)
		//millisecondsOfMp3FileOfJl101(fMp3)
	}

	public static int millisecondsOfMp3FileOfJaudiotagger(File fMp3)
	{
		java.util.logging.Logger.getLogger("org.jaudiotagger").setLevel(java.util.logging.Level.OFF);
		AudioFile audioFile = AudioFileIO.read(fMp3);
		MP3AudioHeader audioheader = audioFile.getAudioHeader()
		println "Track length = " + audioheader.getPreciseTrackLength()
		// audioFile.getAudioHeader().getTrackLength()
		double dlen = audioheader.getPreciseTrackLength()
		String strDlen = (dlen * 1000).toString()
		int ind = strDlen.indexOf('.')
		if (ind > -1)
			strDlen = strDlen.substring(0, ind)
		int ret = Integer.parseInt(strDlen)
		ret
	}

	/**
	 * Tämä methodi palauttaa MP3-tiedoston pituuden millisekunteina.
	 *
	 * @param mp3fileName luettava tiedosto
	 * @return millisekuntit. Jos -1, niin tapahtunut virhe, exception
	 */
	public static int millisecondsOfMp3FileOfJl101(File mp3file)
	throws FileNotFoundException, Exception
	{
		try
		{
			int nTotalMS = 0;
			/*
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(mp3file);
			AudioFormat format = audioInputStream.getFormat();
			long audioFileLength = mp3file.ength();
			int frameSize = format.getFrameSize();
			float frameRate = format.getFrameRate();
			float durationInSeconds = (audioFileLength / (frameSize * frameRate));
			System.out.println("Length in ms: " + durationInSeconds);
			return durationInSeconds *1000;
			*/
			// C:\java\os\mp3spi1.9.5\MpegAudioSPI1.9.5
			AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(mp3file);
			if (fileFormat instanceof TAudioFileFormat) {
				Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
				String key = "duration";
				Long microseconds = (Long) properties.get(key);
				int mili = (int) (microseconds / 1000);
				/*
				long sec = (((long)mili / 1000L) % 60L);
				long min = (((long)mili / 1000L) / 60L);
				// System.out.println("time = " + min + ":" + sec);
				// System.out.println("time ms = " + mili);								
				 */
				return mili;
			} else {
				throw new UnsupportedAudioFileException();
			}
		} catch(Exception e) {
			e.printStackTrace();
			throw e
			// return -1
		}		
	}

	/**
	 * Tämä methodi palauttaa MP3-tiedoston pituuden millisekunteina.
	 * 
	 * @param mp3fileName luettava tiedosto
	 * @return millisekuntit. Jos -1, niin tapahtunut virhe, exception
	 */
	/*
	public static int millisecondsOfMp3File_old(File mp3file)
	{
		try
		{
			int nTotalMS = 0;
			Bitstream m_bitstream = new Bitstream(
				new FileInputStream(mp3file));
			Header m_header = m_bitstream.readFrame();

			int mediaLength = (int)mp3file.length();
			if (mediaLength != AudioSystem.NOT_SPECIFIED) {
				nTotalMS = Math.round(m_header.total_ms(mediaLength));
			}
			println("Length in ms: " + nTotalMS);			
			return nTotalMS
		} catch(Exception e) {
			e.printStackTrace();
			return -1
		}
	}
	*/

	public static void main(String [] args)
	{
		println "test function: main"
		MP3 mp3 = new MP3()
		def ms = MP3.millisecondsOfMp3File(args[0])
		println "millisecondsOfMp3File: " +ms
		println "s  : " +ms / 1000
		println "min: " +(ms / 1000)/ 60
		println "End."
	}
	
	public void println(String msg)
	{
		Lipsync2Daisy.println msg
	}
}
