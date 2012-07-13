package fr.an.tests.sound.testfft;

import java.awt.Dimension;
import java.io.File;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import fr.an.tests.sound.testfft.synth.PHCoefEntry;
import fr.an.tests.sound.testfft.synth.PHCoefFragmentAnalysis;

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
//			"fl-la-gr.wav",
//			"fl-si-gr.wav",
//
//			"fl-do.wav",
//			"fl-re.wav",
//			"fl-mi.wav",
//			"fl-sol.wav",
				
//			"fl-la.wav",
			
//			"fl-si.wav"
		};

		boolean play = 
				false;
//			 true;
		
		
        JFrame frame = new JFrame();
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.getContentPane().add(tabbedPane);
        frame.setPreferredSize(new Dimension(1500, 1024));
		frame.pack();
        frame.setVisible(true);

		
		for (String fn : fileNames) {
			File file = new File(dir, fn);

			doAnalyseFile(file, tabbedPane, play);
			
		}
		
		{
			int synthFragmentsCount = 5;
			double currStartTime = 0.0;
			double totalDuration = 12;

			double fragmentDuration = totalDuration / synthFragmentsCount;   
			PHCoefFragmentAnalysis[] fragments = new PHCoefFragmentAnalysis[synthFragmentsCount];
			for (int i = 0; i < synthFragmentsCount; i++) {
				double currEndTime = currStartTime + fragmentDuration; 
				PHCoefFragmentAnalysis frag = new PHCoefFragmentAnalysis(currStartTime, currEndTime);
				fragments[i] = frag;
				
				int coefEntriesCount = 1;
				PHCoefEntry[] sortedCoefEntries = new PHCoefEntry[coefEntriesCount];
				for(int c = 0; c < coefEntriesCount; c++) {
					PHCoefEntry coefEntry = sortedCoefEntries[c] = new PHCoefEntry();
					double[] p = coefEntry.getP();
					
					double shiftStart = ((i%2) == 0)? +0.3 : -0.3;
					double shiftEnd = -2 * shiftStart;
					
					p[0] = 1.0 + shiftStart;
					p[1] = (1+c/2) * 220.0;
					p[2]= 0.0; // TODO... currStartTime modulo freq ??
					
					p[3] = shiftEnd;
					
					// TODO ARNAUD
					
				}
				frag.setSortedCoefEntries(sortedCoefEntries);
				
				currStartTime = currEndTime;
			}
			doAnalyseSynth("synth", fragments, tabbedPane, play);
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

	
	protected static void doAnalyseSynth(String synthName, 
			PHCoefFragmentAnalysis[] fragments,
			JTabbedPane tabbedPane, boolean play) {
		System.out.println("synthetizing " + synthName);
		
		SoundAnalysisModel model = new SoundAnalysisModel(synthName);
		model.setFrameRate(4000);
		int totalFrameLength = 2048 * 12; 
		model.setFrameLength(totalFrameLength);
		double[] synthData = new double[totalFrameLength];
		
		double totalDuration = 0.0;
		for (PHCoefFragmentAnalysis frag : fragments) {
			totalDuration += frag.getDuration();
		}
		double frameRate = totalFrameLength / totalDuration; 
		// double invFrameRate = 1.0 / frameRate;
		double currStartTime = 0.0;
		int currStartIndex = 0;
		
		for (PHCoefFragmentAnalysis frag : fragments) {
			double fragDuration = frag.getDuration();
			int fragLen = (int) (fragDuration * frameRate); // = frag.getDataLen();
			int currEndIndex = currStartIndex + fragLen;
			
			int harmonicCount = frag.getSortedCoefEntries().length;
			double fragStartTime = currStartTime;
			double fragEndTime = currStartTime + fragDuration;
			
			frag.getReconstructedMainHarmonics(harmonicCount, 
					fragStartTime, fragEndTime, 
					currStartIndex, currEndIndex, synthData, null);
			
			currStartTime += fragDuration;
			currStartIndex = currEndIndex;
		}

		model.setAudioDataAsDouble(synthData);
		
		model.analysisFFT();
		
		SoundAnalysisView view = new SoundAnalysisView(model);
		
        tabbedPane.add(synthName, view.getJComponent());
        
		// play back
		if (play) {
			model.playAudioBytes(model.getAudioDataAsDouble());
		}
	    
	}
	
	
	
	

}
