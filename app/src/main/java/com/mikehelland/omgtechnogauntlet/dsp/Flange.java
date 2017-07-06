package com.mikehelland.omgtechnogauntlet.dsp;


public class Flange extends UGen {
	final float[] line;
	float phase;
	float cyclesPerSample;
	int pointer;
	
	public Flange(int length, float freq) {
		super();
		this.line = new float[length];
		this.cyclesPerSample = freq/SAMPLE_RATE;
	}
	
	public boolean render(final float[] buffer) {
		renderKids(buffer);
		
		float localPhase = phase;
		int localPointer = pointer;
		final int length = line.length;
		for(int i = 0; i < CHUNK_SIZE; i++) {
			line[localPointer] = buffer[i];
			final int samplePointer = ((int)(localPointer + length*(1.0f-localPhase*(1.0f-localPhase))))%length;
			buffer[i] = 0.5f*buffer[i] - 0.5f*line[samplePointer];
			localPointer = (localPointer+1)%length;
			localPhase += cyclesPerSample;
			localPhase -= (int)localPhase;
		}
		pointer = localPointer;
		phase = localPhase;
		
		return true; 
	}
}
