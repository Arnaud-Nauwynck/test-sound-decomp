package fr.an.tests.sound.testfft;

import java.awt.Dimension;
import java.io.File;

import javax.sound.sampled.LineUnavailableException;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import fr.an.tests.sound.testfft.sfft.FFT;
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
			"fl-fa-gr.wav",
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

		boolean useSynthPH = true;
		boolean useSynth = true;

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

		int frameRate = 8000;  // Hz
		int fragmentLen = 2048; // 2048/8000~=250 ms   4096/8000~=500ms  8192/80002=1sec
		int totalFrameLength = 4 * fragmentLen; // (int) (1.5 * 8000); // = 1.5 sec 
		int synthFragmentsCount = totalFrameLength / fragmentLen;
		
		// double totalDuration = totalFrameLength / frameRate;
		double fragmentDuration = fragmentLen / frameRate;
		double dt = 1.0 / frameRate;


		FFT fft = new FFT(fragmentLen, frameRate);

		
		if (useSynth) {
			SoundAnalysisModel model = new SoundAnalysisModel("synth");
			model.setFrameRate(frameRate);
			
			SoundFragmentAnalysis[] fragments = new SoundFragmentAnalysis[synthFragmentsCount];
			int currStartIndex = 0;
			double currStartTime = 0.0;
			for (int i = 0; i < synthFragmentsCount; i++) {
				SoundFragmentAnalysis frag = new SoundFragmentAnalysis(model, currStartIndex, fragmentLen, currStartTime, dt, fft);
				fragments[i] = frag;
				
				PHCoefEntry[] sortedCoefEntries = new PHCoefEntry[1];
				PHCoefEntry coefEntry = sortedCoefEntries[0] = new PHCoefEntry();
				double[] p = coefEntry.getP();
				p[0] = 1.0;
				double baseFreq = 440.0;  // 440 Hz for a flute "LA"
				p[1] = baseFreq * (2.0*Math.PI);  // w=2.pi.f
				p[2]= 0.0;
				frag.getPhCoefAnalysisFragment().setSortedCoefEntries(sortedCoefEntries);
				
				currStartIndex += fragmentLen;
				currStartTime += fragmentDuration;
			} //for frag
			doAnalyseSynth("synth", frameRate, totalFrameLength, fragments, tabbedPane, play);
		}
		
		if (useSynthPH) {
			SoundAnalysisModel model = new SoundAnalysisModel("PH-synth");
			model.setFrameRate(frameRate);
			SoundFragmentAnalysis[] fragments = new SoundFragmentAnalysis[synthFragmentsCount];
			int currStartIndex = 0;
			double currStartTime = 0.0;
			for (int i = 0; i < synthFragmentsCount; i++) {
				SoundFragmentAnalysis frag = new SoundFragmentAnalysis(model, currStartIndex, fragmentLen, currStartTime, dt, fft);
				fragments[i] = frag;
				
				int coefEntriesCount = 1;
				PHCoefEntry[] sortedCoefEntries = new PHCoefEntry[coefEntriesCount];
				for(int c = 0; c < coefEntriesCount; c++) {
					PHCoefEntry coefEntry = sortedCoefEntries[c] = new PHCoefEntry();
					double[] p = coefEntry.getP();
					
					double shiftStart = ((i%2) == 0)? +0.3 : -0.3;
					double shiftEnd = -2 * shiftStart;
					
					p[0] = 1.0 + shiftStart;
					double baseFreq = (1.0+(double)c/5) * 440.0;  // 440 Hz for a flute "LA"
					p[1] = baseFreq * (2.0*Math.PI);  // w=2.pi.f
					p[2]= 0.0; // TODO... currStartTime modulo freq ??
					
					p[3] = shiftEnd;
					
					// TODO ARNAUD
					
				}
				frag.getPhCoefAnalysisFragment().setSortedCoefEntries(sortedCoefEntries);

				currStartIndex += fragmentLen;
				currStartTime += fragmentDuration;
			} //for frag
			doAnalyseSynth("PH-synth", frameRate, totalFrameLength, fragments, tabbedPane, play);
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
			int frameRate,
			int totalFrameLength,
			SoundFragmentAnalysis[] fragments,
			JTabbedPane tabbedPane, boolean play) {
		// System.out.println("synthetizing " + synthName);
		
		SoundAnalysisModel model = new SoundAnalysisModel(synthName);
		model.setFrameRate(frameRate);
		model.setFrameLength(totalFrameLength);
		double[] synthData = new double[totalFrameLength];
		
		double dt = 1.0 / model.getFrameRate();
		double currStartTime = 0.0;
		int currStartIndex = 0;
		
		for (SoundFragmentAnalysis frag : fragments) {
			PHCoefFragmentAnalysis phFrag = frag.getPhCoefAnalysisFragment();
			double fragDuration = frag.getDuration();
			int fragLen = frag.getFragmentLen();
			int currEndIndex = currStartIndex + fragLen;
			
			int harmonicCount = phFrag.getSortedCoefEntries().length;
			
			phFrag.getReconstructedMainHarmonics(harmonicCount, 
					currStartIndex, currEndIndex, currStartTime, dt, 
					synthData, null);
			
			currStartIndex = currEndIndex;
			currStartTime += fragDuration;
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
