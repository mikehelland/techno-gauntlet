package com.mikehelland.omgtechnogauntlet.dsp;

public class WtOsc extends UGen {
	public static final int BITS = 8;
	public static final int ENTRIES = 1<<(BITS-1);
	public static final int MASK = ENTRIES-1;
	
	private float phase;
	private float cyclesPerSample;
	
	final float[] table;
	
	public WtOsc () {
		table = new float[ENTRIES];
	}
	
	public synchronized void setFreq(float freq) {
		cyclesPerSample = freq/SAMPLE_RATE;
	} 
	
	public synchronized boolean render(final float[] buffer) { // assume t is in 0.0 to 1.0
		float localPhase = phase;
		for(int i = 0; i < CHUNK_SIZE; i++) {
			final float scaled = localPhase*ENTRIES;
			final int index = (int)scaled;
			final float fraction = scaled-index;
			buffer[i] += (1.0f-fraction)*table[index&MASK]+fraction*table[(index+1)&MASK];
			localPhase += cyclesPerSample;
		}
		phase = localPhase - (int)localPhase;
		
		return true;
	}
	
	public WtOsc fillWithSin() {
		final float dt = (float)(2.0*Math.PI/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float)Math.sin(i*dt);
		}
		return this;
	}
	
	public WtOsc fillWithHardSin(final float exp) {
		final float dt = (float)(2.0*Math.PI/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float) Math.pow(Math.sin(i*dt),exp);
		}
		return this;
	}
	
	public WtOsc fillWithZero() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = 0;
		}
		return this;
	}
	
	public WtOsc fillWithSqr() {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = i<ENTRIES/2?1f:-1f;
		}
		return this;
	}
	
	public WtOsc fillWithSqrDuty(float fraction) {
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = (float)i/ENTRIES<fraction?1f:-1f;
		}
		return this;
	}
	
	public WtOsc fillWithSaw() {
		float dt = (float)(2.0/ENTRIES);
		for(int i = 0; i < ENTRIES; i++) {
			table[i] = 1.0f-i*dt;
		}
		return this;
	}
}
