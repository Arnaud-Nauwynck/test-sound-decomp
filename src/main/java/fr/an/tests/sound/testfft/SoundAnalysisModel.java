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

public class SoundAnalysisModel {

	private String name;
	
	private AudioFormat audioFormat;
	private int frameLength;
	private short[] audioDataAsShorts;
	private double[] audioDataAsDouble;
	
	private FFTCoefAnalysis fftCoefAnalysis;
	
	// ------------------------------------------------------------------------

	public SoundAnalysisModel(String name) {
		this.name = name;
	}

	// ------------------------------------------------------------------------


	public String getName() {
		return name;
	}
	
	public int getFrameLength() {
		return frameLength;
	}
	
	public double[] getAudioDataAsDouble() {
		return audioDataAsDouble;
	}

	public void readData(File file) {
		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(file);
		} catch (UnsupportedAudioFileException e) {
			throw new RuntimeException("Failed", e);
		} catch (IOException e) {
			throw new RuntimeException("Failed", e);
		}
		
		this.audioFormat = audioInputStream.getFormat();
		System.out.println("audio format:" + audioFormat);
		
		this.frameLength = (int) audioInputStream.getFrameLength();
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
		audioDataAsShorts = new short[frameLength];
		sbuf.get(audioDataAsShorts); // read short from bytes

		
		// PRUNE zeros at beginning!!
		int firstIndexNonNull = 0;
		int lastIndexNonNull = frameLength;
		final short MIN_SIGNAL_VALUE = 10;
		for (int i = 0; i < frameLength; i++) {
			if (Math.abs(audioDataAsShorts[i]) > MIN_SIGNAL_VALUE) {
				firstIndexNonNull = i;
				if (firstIndexNonNull + 4000 < frameLength) {
					firstIndexNonNull += 4000; // prune begining until stable signal ???..
				}
				break;
			}
		}
		for (int i = frameLength-1; i >= firstIndexNonNull; i--) {
			if (Math.abs(audioDataAsShorts[i]) > MIN_SIGNAL_VALUE) {
				lastIndexNonNull = i;
				break;
			}
		}
		if (firstIndexNonNull != 0 ||  lastIndexNonNull != frameLength) {
			System.out.println("*** PRUNE zeros in begin/end signal => new range " + firstIndexNonNull + ", " + lastIndexNonNull);

			this.frameLength = lastIndexNonNull - firstIndexNonNull;
			short[] oldData = audioDataAsShorts;
			this.audioDataAsShorts = new short[frameLength];
			System.arraycopy(oldData, firstIndexNonNull, audioDataAsShorts, 0, frameLength);
		}
		
		audioDataAsDouble = new double[frameLength];
		for (int i = 0; i < frameLength; i++) {
			audioDataAsDouble[i] = ((double) audioDataAsShorts[i]) / 0x8000;
		}
	}

	public void analysisFFT() {

		// sub-range for FFT (should use power of 2 ...)
		int fftSubLength = 1;
//		while(fftSubLength*2 < frameLength) {
//		fftSubLength *= 2;
//	}
		// fftSubLength = 8000; // sample at 8000 Hz => FFT on 1 second interval
		fftSubLength = 1024;   // ~ 0.1 second interval
		this.fftCoefAnalysis = new FFTCoefAnalysis(fftSubLength);
		
		
		double[] fftData = new double[fftSubLength];
		System.arraycopy(audioDataAsDouble, 0, fftData, 0, fftData.length);
		
//		RealDoubleFFT fftPack = new RealDoubleFFT(fftSubLength);
		DoubleFFT_1D fft = new DoubleFFT_1D(fftSubLength);
		System.out.println("doing FFT on sub-length:" + fftSubLength + " (=" + (fftSubLength/audioFormat.getFrameRate()) + " s)");
		
//		fftPack.ft(fftData);
		fft.realForward(fftData);
		
		fftCoefAnalysis.setDataJTransform(fftData);
		fftCoefAnalysis.printData();
				
	}

	public void playAudioBytes(double[] data) {
		playAudioBytes(audioFormat, data);
	}

	public static byte[] convertToAudioBytesFormat(AudioFormat audioFormat, double[] data) {
		final int len = data.length; 
		byte[] res = new byte[len * 2];
		
		// TODO currently hard-coded ... convert short 16_LE -> bytes 
		ByteBuffer audioBytesBuffer = ByteBuffer.wrap(res);
		ShortBuffer sbuf = audioBytesBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();

		for (int i = 0; i < len; i++) {
			short shortData = (short) (data[i] * 0x8000);
			sbuf.put(i, shortData);	
		}
		return res;	
	}
	
	public static void playAudioBytes(AudioFormat audioFormat, double[] data) {
		byte[] audioBytes = convertToAudioBytesFormat(audioFormat, data);
		playAudioBytes(audioFormat, audioBytes);
	}
	
	public static void playAudioBytes(AudioFormat audioFormat, byte[] audioBytes) {
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

	public void getReconstructedMainHarmonics(int harmonicCount, double[] approxData, double[] residualData, FFTResiduInfo residuInfo) {
		fftCoefAnalysis.getReconstructedMainHarmonics(harmonicCount, approxData, residuInfo);
		final int len = approxData.length;
		// double
		for (int i = 0; i < len; i++) {
			residualData[i] = audioDataAsDouble[i] - approxData[i];
		}
	}
}
