package com.mikehelland.omgtechnogauntlet.dsp;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
//import com.monadpad.tonezart.PcmWriter;


public class Dac extends UGen {
	private final float[] localBuffer;
	private boolean isClean;
	private final AudioTrack track;
	//private final short [] target = new short[UGen.CHUNK_SIZE];
    private short [] target = new short[UGen.CHUNK_SIZE];
	private final short [] silentTarget = new short[UGen.CHUNK_SIZE];

    private boolean playing = true;
    private boolean finishing = false;

	public Dac() {
		localBuffer = new float[CHUNK_SIZE];
		
		int minSize = AudioTrack.getMinBufferSize(
				UGen.SAMPLE_RATE,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT);
		
		track = new AudioTrack(
        		AudioManager.STREAM_MUSIC,
        		UGen.SAMPLE_RATE,
        		AudioFormat.CHANNEL_CONFIGURATION_MONO,
        		AudioFormat.ENCODING_PCM_16BIT,
        		Math.max(UGen.CHUNK_SIZE*4, minSize),
        		AudioTrack.MODE_STREAM);

	}
	
	public boolean render(final float[] _buffer) {
		if(!isClean) {
			zeroBuffer(localBuffer);

			isClean = true;
		}
		// localBuffer is always clean right here, does it stay that way?
		isClean = !renderKids(localBuffer);
		return !isClean; // we did some work if the buffer isn't clean
	}
	
	public void open() {
		track.play();
	}

	public void setVolume(float gain) {
        //track.setVolume(gain);
    }
	
	public void tick() {

        if (finishing) {
            playing = false;
            if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                track.stop();
            }
            track.release();
        }
		
		render(localBuffer);
		
		if(isClean) {
			// sleeping is messy, so lets just queue this silent buffer
            if (playing)
			    track.write(silentTarget, 0, silentTarget.length);

/*            if (recording){
                pcmWriter.write(silentTarget);
            }
*/
		} else {

            //target = new short[UGen.CHUNK_SIZE];
			for(int i = 0; i < CHUNK_SIZE; i++) {
				target[i] = (short)(32768.0f*localBuffer[i]);
			}


            if (playing)
			    track.write(target, 0, target.length);

            /*
            if (recording){
                pcmWriter.write(target);
            }
            */

		}
	}
	
	public void close() {
        finishing = true;
	}

    private int iran = 0;
}
