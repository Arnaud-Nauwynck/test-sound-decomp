package fr.an.tests.sound.testfft;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class ReadFileMain {

	public static void main(String[] args) throws LineUnavailableException {
		File dir = new File("src/test/sounds/wav");
		
//		File file = new File(dir, "fl-do.wav");
		
		String[] fileNames = new String[] {
//			"myriam-singing-in-the-rain.wav",
			// missing... "fl-do-gr.wav",
			"fl-re-gr.wav",
			"fl-mi-gr.wav",
			"fl-fa-gr.wav",
			"fl-sol-gr.wav",
			"fl-la-gr.wav",
			"fl-si-gr.wav",

			"fl-do.wav",
			"fl-re.wav",
			"fl-mi.wav",
			"fl-sol.wav",
			"fl-la.wav",
			"fl-si.wav"
		};

		boolean play = 
//				false;
			 true;
		
		for (String fn : fileNames) {
			File file = new File(dir, fn);

			doAnalyseFile(file, play);
		}
		
	}

	protected static void doAnalyseFile(File file, boolean play) {
		System.out.println("Analysing file " + file);
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			throw new RuntimeException("Failed", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed", e);
		}
		
		AudioFormat audioFormat = audioInputStream.getFormat();
		System.out.println("audio format:" + audioFormat);
		
		int frameLength = (int) audioInputStream.getFrameLength();
		int audioBytesLen = frameLength * audioFormat.getFrameSize();
		float duration = frameLength/audioFormat.getFrameRate();
		System.out.println("frameLength:" + frameLength + ", duration (frameLength/framePerSec): " + duration + ", bytes (frameLength*FrameSize): " + audioBytesLen); 

		// read bytes from audioInputStream
		boolean useNioForFileRead = true;
		byte[] audioBytes = new byte[audioBytesLen];
		ByteBuffer audioBytesBuffer;
		if (useNioForFileRead) {
			audioBytesBuffer = ByteBuffer.wrap(audioBytes);
			ReadableByteChannel audioInputChannel = Channels.newChannel(audioInputStream);
			try {
				int readTotal = 0;
				int readCount;
				for(;;) {
					readCount = audioInputChannel.read(audioBytesBuffer);
					if (readCount == -1) {
						break;
					}
					readTotal += readCount;
					if (readTotal == audioBytesLen) {
						// no EOF in AudioStream channel???
						break;
					}
				}
				audioBytesBuffer.flip();
			} catch (IOException e) {
				throw new RuntimeException("Failed to read audioInputStream bytes", e);
			}
		} else {
			try {
				audioInputStream.read(audioBytes, 0, audioBytes.length);
			} catch (IOException e) {
				throw new RuntimeException("Failed to read audioInputStream bytes", e);
			}
			audioBytesBuffer = ByteBuffer.wrap(audioBytes);
		}
		
		try {
			audioInputStream.close();
		} catch(Exception ex) {
		}
		
		
		// convert to 16_LE 
		ShortBuffer sbuf = audioBytesBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
		short[] audioShorts = new short[frameLength];
		sbuf.get(audioShorts); // read short from bytes

		double[] doubleData = new double[frameLength];
		for (int i = 0; i < frameLength; i++) {
			doubleData[i] = ((double) audioShorts[i]) / 0x8000;
		}
		
		// sub-range for FFT (should use power of 2 ...)
		int fftSubLength = 1;
//		while(fftSubLength*2 < frameLength) {
//			fftSubLength *= 2;
//		}
		fftSubLength = 8000; // sample at 8000 Hz => FFT on 1 second interval 
		
		double[] fftData = new double[fftSubLength];
		System.arraycopy(doubleData, 0, fftData, 0, fftData.length);
		
//		RealDoubleFFT fftPack = new RealDoubleFFT(fftSubLength);
		DoubleFFT_1D fft = new DoubleFFT_1D(fftSubLength);
		FFTCoefPrinter fftCoefPrinter = new FFTCoefPrinter(fftSubLength); 
		System.out.println("doing FFT on sub-length:" + fftSubLength + " (=" + (fftSubLength/audioFormat.getFrameRate()) + " s)");
		
//		fftPack.ft(fftData);
		fft.realForward(fftData);
		
		fftCoefPrinter.setDataJTransform(fftData);
		fftCoefPrinter.printData();
		
		// play back
		if (play) {
			playAudioBytes(audioFormat, audioBytes);
		}
	    
	}

	protected static void playAudioBytes(AudioFormat audioFormat, byte[] audioBytes) {
		SourceDataLine sourceDataLine;
		try {
			sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException("", ex);
		}
	    try {
			sourceDataLine.open(audioFormat);
		} catch (LineUnavailableException ex) {
			throw new RuntimeException("", ex);
		}
	    
	    sourceDataLine.start();
	    sourceDataLine.write(audioBytes, 0, audioBytes.length);
	    sourceDataLine.drain();
	    sourceDataLine.stop();
	    sourceDataLine.close();
	}
}
