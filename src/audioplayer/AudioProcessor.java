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
public abstract class AudioProcessor {
    
    public abstract short[][] requestAudio(int samples);
    
    public abstract int getChannels();
}
