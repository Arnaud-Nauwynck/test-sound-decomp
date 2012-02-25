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
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.event.DatasetChangeEvent;
import org.jfree.data.event.DatasetChangeListener;
import org.jfree.data.xy.DefaultXYDataset;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class ReadFileMain {

	public static class AnalysisEntry {
		
	}
	
	public static void main(String[] args) throws LineUnavailableException {
		File dir = new File("src/test/sounds/wav");
		
//		File file = new File(dir, "fl-do.wav");
		
		String[] fileNames = new String[] {
//			"myriam-singing-in-the-rain.wav",
			// missing... "fl-do-gr.wav",
//			"fl-re-gr.wav",
//			"fl-mi-gr.wav",
//			"fl-fa-gr.wav",
//			"fl-sol-gr.wav",
			"fl-la-gr.wav",
//			"fl-si-gr.wav",

//			"fl-do.wav",
//			"fl-re.wav",
//			"fl-mi.wav",
//			"fl-sol.wav",
//			"fl-la.wav",
//			"fl-si.wav"
		};

		boolean play = 
//				false;
			 true;
		
		
        JFrame frame = new JFrame();
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane);
		frame.pack();
        frame.setVisible(true);

		
		for (String fn : fileNames) {
			File file = new File(dir, fn);

			doAnalyseFile(file, tabbedPane, play);
			
		}
		
	}

	protected static void doAnalyseFile(File file, JTabbedPane tabbedPane, boolean play) {
		System.out.println("Analysing file " + file);
		
		SoundAnalysisModel model = new SoundAnalysisModel(file.getName());
		
		model.readData(file);
		

		model.analysisFFT();
		
		SoundAnalysisView view = new SoundAnalysisView(model);
		
        tabbedPane.add(file.getName(), view.getJComponent());
        
		// play back
		if (play) {
			model.playAudioBytes(model.getAudioDataAsDouble());
		}
	    
	}

	
	
	

}
