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
import fr.an.tests.sound.testfft.sfft.FFTCoefEntry;
import fr.an.tests.sound.testfft.sfft.FFTCoefFragmentAnalysis;
import fr.an.tests.sound.testfft.synth.PHCoefFragmentAnalysis;

public class SoundAnalysisModel {

	private String name;
	
	private AudioFormat audioFormat;
	
	private int frameLength;
	/** frames per seconds ... ex: 4000 Hz*/
	private double frameRate;
	private double[] audioDataAsDouble;
	
	
	private int fragmentLen = // 1024;   // at 4000 Hz => sample for 1/4 second = 250 ms
			2048;  // at 4000 Hz => sample for 1/2 second = 500 ms
	private FFTCoefFragmentAnalysis[] fftCoefAnalysisFragments;
	private PHCoefFragmentAnalysis[] phCoefAnalysisFragments;
	
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

	public void setFrameLength(int p) {
		this.frameLength = p;
	}

	public double getFrameRate() {
		return frameRate;
	}

	public void setFrameRate(int p) {
		this.frameRate = p;
	}

	public double getFrameDuration() {
		return frameLength / frameRate;
	}


	public int getFragmentLen() {
		return fragmentLen;
	}

	public void setFragmentLen(int p) {
		this.fragmentLen = p;
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
		this.frameRate = audioFormat.getFrameRate();
		int audioBytesLen = frameLength * audioFormat.getFrameSize();
		double durationTime = frameLength/audioFormat.getFrameRate();
		System.out.println("frameLength:" + frameLength + ", duration (frameLength/framePerSec): " + durationTime + ", bytes (frameLength*FrameSize): " + audioBytesLen); 

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
		short[] audioDataAsShorts = new short[frameLength];
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
			audioDataAsShorts = new short[frameLength];
			System.arraycopy(oldData, firstIndexNonNull, audioDataAsShorts, 0, frameLength);
		}
		
		double[] tmpAudioData = new double[frameLength];
		for (int i = 0; i < frameLength; i++) {
			tmpAudioData[i] = ((double) audioDataAsShorts[i]) / 0x8000;
		}
		setAudioDataAsDouble(tmpAudioData);
	}
	
	public void setAudioDataAsDouble(double[] audioDataAsDouble) {
		this.audioDataAsDouble = audioDataAsDouble;
	}

	public void analysisFFT() {

		int fragmentsCount = audioDataAsDouble.length / fragmentLen - 1;
		this.fftCoefAnalysisFragments = new FFTCoefFragmentAnalysis[fragmentsCount];
		this.phCoefAnalysisFragments = new PHCoefFragmentAnalysis[fragmentsCount];
		
		DoubleFFT_1D fft = new DoubleFFT_1D(fragmentLen);

		
		int currIndex = 0;
		double currStartTime = 0.0; 
		double invFrameRate = 1.0 / frameRate;
		for (int i = 0; i < fragmentsCount; i++,currIndex += fragmentLen) {
			double[] fragmentData = new double[fragmentLen];
			System.arraycopy(audioDataAsDouble, currIndex, fragmentData, 0, fragmentLen);

			double[] fftData = new double[fragmentLen];
			System.arraycopy(audioDataAsDouble, currIndex, fftData, 0, fragmentLen);
			fft.realForward(fftData);
			fftCoefAnalysisFragments[i] = new FFTCoefFragmentAnalysis(fragmentLen);
			fftCoefAnalysisFragments[i].setFFTData(fftData);
			// fftCoefAnalysis.printData();

			double currFragmentTime = fragmentData.length * invFrameRate;
			double currEndTime = currStartTime + currFragmentTime;
			phCoefAnalysisFragments[i] = new PHCoefFragmentAnalysis(currStartTime, currEndTime);
			phCoefAnalysisFragments[i].setData(fragmentData);
			
			currStartTime += currFragmentTime;
		}
	}

	public void playAudioBytes(double[] data) {
		if (audioFormat != null) {
			playAudioBytes(audioFormat, data);
		}
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

	public void getReconstructedMainHarmonics(int harmonicCount, 
				double[] approxData, double[] residualData, ResiduInfo residuInfo) {
		int fragmentsCount = fftCoefAnalysisFragments.length;

		int approxDataLen = approxData.length;
		
		int currIndex = 0;
		for (int i = 0; i < fragmentsCount; i++,currIndex += fragmentLen) {
			FFTCoefFragmentAnalysis fftFrag = fftCoefAnalysisFragments[i];
		
			int endIndex = Math.min(currIndex + fragmentLen, approxDataLen);
			ResiduInfo residuInfoFrag = new ResiduInfo();
			fftFrag.getReconstructedMainHarmonics(harmonicCount, 
					currIndex, endIndex, approxData, residuInfoFrag);
			
			residuInfo.addFragment(residuInfoFrag);
		}
		
		final int len = approxData.length;
		// double
		for (int i = 0; i < len; i++) {
			residualData[i] = audioDataAsDouble[i] - approxData[i];
		}
	}

	public void getReconstructedMainHarmonicsPH(int harmonicCount, 
			double startTime, double endTime,
			double[] approxData, double[] residualData, ResiduInfo residuInfo) {
		int fragmentsCount = fftCoefAnalysisFragments.length;
	
		int approxDataLen = approxData.length;
		
		int fragIndex = 0;
		int currIndex = 0;
		double currStartTime = startTime;
		for (fragIndex = 0; fragIndex < fragmentsCount; fragIndex++) {
			PHCoefFragmentAnalysis frag = phCoefAnalysisFragments[fragIndex];
			if (frag.getStartTime() > startTime) {
				currStartTime += frag.getDuration(); 
				continue;
			}
		}
		for (; fragIndex < fragmentsCount; fragIndex++) {
			PHCoefFragmentAnalysis frag = phCoefAnalysisFragments[fragIndex];
		
			double currFragStartTime = frag.getStartTime();
			double currFragEndTime = frag.getEndTime();
			if (currStartTime > endTime) {
				break;
			}
			int endIndex = Math.min(currIndex + fragmentLen, approxDataLen);
			ResiduInfo residuInfoFrag = new ResiduInfo();
			frag.getReconstructedMainHarmonics(harmonicCount, 
					currFragStartTime, currFragEndTime,
					currIndex, endIndex, approxData, residuInfoFrag);
			
			residuInfo.addFragment(residuInfoFrag);
			
			currIndex += frag.getDataLen();
			currStartTime += frag.getDuration(); 
		}
		
		final int len = approxData.length;
		// double
		for (int i = 0; i < len; i++) {
			residualData[i] = audioDataAsDouble[i] - approxData[i];
		}
	}
		
	public double[] getFFTData() {
		final int fftLen = fragmentLen / 2;
		double[] res = new double[fftLen];
		final int fragmentsCount = fftCoefAnalysisFragments.length;
		for (int f = 0; f < fragmentsCount; f++) {
			FFTCoefFragmentAnalysis fftFrag = fftCoefAnalysisFragments[f];
			FFTCoefEntry[] fragFFTCoefs = fftFrag.getCoefEntries();
			for (int i = 0; i < fftLen; i++) {
				res[i] += fragFFTCoefs[i].getNorm(); // sum norm is not exact (neither summing fragments)... but ok for display
			}
		}
		double invFragCount = 1.0 / fragmentsCount; 
		for (int i = 0; i < fftLen; i++) {
			res[i] *= invFragCount;
		}
		return res;
	}

	public FFTCoefFragmentAnalysis[] getFFTCoefAnalysisFragments() {
		return fftCoefAnalysisFragments;
	}
}
