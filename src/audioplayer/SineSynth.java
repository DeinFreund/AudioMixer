/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioplayer;

/**
 *
 * @author User
 */
public class SineSynth extends AudioProcessor{

    int sample = 0;
    
    @Override
    public short[][] requestAudio(int samples) {
        short[][] s = new short[1][samples];
        for (int i = sample; i < sample + samples; i++){
            s[0][i-sample] = (short)(Math.sin(i/20)*32768/100);
        }
        sample+= samples;
        return s;
    }

    @Override
    public int getChannels() {
        return 1;
    }
    
}
