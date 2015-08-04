/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioplayer;

import java.io.File;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author User
 */
public class OutputThread extends Thread {

    AudioProcessor input;

    public synchronized void setInput(AudioProcessor input) {
        this.input = input;
    }

    protected synchronized void requestSamples() {
        int bf = (currentBuffer + 1) % inputBuffers.length;
        int channels = input.getChannels();
        short[][] data = input.requestAudio(inputBuffers[bf].length / 4);
        int second = Math.min(2, channels) - 1;
        for (int i = 0; i < inputBuffers[bf].length / 4; i++) {
            inputBuffers[bf][i*4] = (byte)(data[0][i]);
            inputBuffers[bf][i*4+1] = (byte)(data[0][i] >> 8);
            inputBuffers[bf][i * 4 + 2] = (byte) (data[second][i] >> 0);
            inputBuffers[bf][i * 4 + 3] = (byte) (data[second][i] >> 8);
        }
    }

    int currentBuffer = 0;
    byte[][] inputBuffers = new byte[2][1 << 16];

    @Override
    public void run() {

        this.setPriority(MAX_PRIORITY);

        float fSampleRate = 44100.0F;
        AudioFormat audioFormat = new AudioFormat(fSampleRate, 16, 2, true, false);
        SourceDataLine line = null;
        DataLine.Info info = new DataLine.Info(
                SourceDataLine.class,
                audioFormat);
        //setInput(new FilePlayer(new File("C:/Users/User/Music/blub.wav")));
        FilePlayer fp = new FilePlayer(new File("C:/Users/User/Music/SevenNationArmyEOTLTrailer.mp3"));
        setInput(fp);
        fp.setSpeed(1.11f);
        //setInput(new SineSynth());

        System.out.println("starting audio");
        /*for (currentBuffer = 0; currentBuffer < inputBuffers.length - 1; currentBuffer ++){
         requestSamples();
            
         }*/
        currentBuffer = 0;
        requestSamples();

        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat, 1 << 12);
            line.start();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int cnt = 0;
        long start = System.currentTimeMillis();
        long sstart = System.currentTimeMillis();
        while (System.currentTimeMillis() - sstart < 200000) {
            //(byte) (Math.sin((cnt) / 500 / Math.PI) * 128);
            //cnt += line.write(buffer, cnt%buffer.length, 2);
            //byte b[] = new byte[]{inputBuffers[currentBuffer][cnt],inputBuffers[currentBuffer][cnt+1],inputBuffers[currentBuffer][cnt+2],inputBuffers[currentBuffer][cnt+3]};
            //int written = line.write(b, 0, 4);
            int written = line.write(inputBuffers[currentBuffer], cnt, 4);
            cnt += written;
            if (cnt >= inputBuffers[currentBuffer].length) {
                //System.out.println(cnt);
                cnt = 0;
                currentBuffer = (currentBuffer + 1) % inputBuffers.length;
                new Runnable(){

                 @Override
                 public void run() {
                 requestSamples();
                 }
                    
                 }.run();
                ((FilePlayer)input).setSpeed(Math.max(1f,1.1f-(System.currentTimeMillis() - sstart)/ 1000000f));
                System.out.println(((FilePlayer)input).getSpeed());
                //requestSamples();
            }
            /*if (System.currentTimeMillis() - start > 2000 ) {

             System.out.println( 1000* cnt / (System.currentTimeMillis() - start) );
             cnt = 0;
             start = System.currentTimeMillis();
             //break;
             }*/
        }
    }
}
