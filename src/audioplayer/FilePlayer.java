/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package audioplayer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author User
 */
public class FilePlayer extends AudioProcessor {

    protected short[][] samples; // channel, sample
    int channels;
    float currentSample = 0;

    float speed = 1;
    float _speed = 1; //for sample rate correction

    public FilePlayer(File file) {
        boolean supportedFormat = true;
        try {
            AudioSystem.getAudioInputStream(file);
        } catch (UnsupportedAudioFileException ex) {
            supportedFormat = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            if (!supportedFormat) {
                if (new File("temp.wav").exists()) {
                    new File("temp.wav").delete();
                }
                Runtime r = Runtime.getRuntime();
                System.out.println("Converting file");
                Process p = r.exec("ffmpeg/ffmpeg.exe -i " + file.getAbsolutePath() + " -ar 44100 -ac 2 temp.wav");
                p.waitFor();

                System.out.println("done");
                file = new File("temp.wav");
            }
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            long length = ais.getFrameLength();
            if (ais.getFrameLength() <= 0) {
                length = 0;
                int read = 0;
                do {
                    read = is.read(new byte[4096], 0, 4);
                    length += read;
                } while (read > 0);
                ais = AudioSystem.getAudioInputStream(file);
                System.out.println(length);
            }
            DataInputStream dis = new DataInputStream(is);
            AudioFormat format = ais.getFormat();
            _speed = format.getFrameRate() / 44100;
            length = length / Math.max(format.getFrameSize(), 2) * Math.max(format.getFrameSize(), 2);
            System.out.println(length);
            /*Clip clip = AudioSystem.getClip();
             clip.open(is);
             clip.start(); //*/
            channels = format.getChannels();
            byte[] bytes = new byte[(int) Math.max(length, ais.getFrameLength() * format.getFrameSize())];
            samples = new short[channels][bytes.length / 2 / channels];
            int read = 1;
            int index = 0;
            while (read > 0) {
                read = dis.read(bytes, index, bytes.length - index);
                index += read;
            }
            for (int i = 0; i < bytes.length / 2; i++) {
                short val = 0;
                if (format.isBigEndian()) {
                    val += (bytes[i] << 8) & 0xFF00;
                    val += (bytes[i + 1]) & 0xFF; // sign
                } else {
                    val += (bytes[i * 2 + 1] << 8) & 0xFF00;
                    val += (bytes[i * 2]) & 0xFF; // sign
                    //samples[i] += (bytes[i*2 + 1] << 8);
                    //samples[i] += bytes[i*2];
                }
                if (i < 44) {
                    val = 0;
                }
                samples[i % channels][i / channels] = val;
                //System.out.println(samples[i/2]);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            channels = 1;
            samples = new short[1][0];
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    @Override
    public short[][] requestAudio(int samples) {
        short[][] ret = new short[channels][samples];
        for (int i = 0; i < samples; i++) {
            for (int c = 0; c < channels; c++) {
                if (currentSample + i * speed * _speed + 1.0001 > this.samples[c].length) {
                    break;
                }
                int index = (int) ((currentSample + i * speed * _speed));
                float dist = (currentSample + i * speed * _speed) % 1.0f;
                ret[c][i] = (short) Math.round(this.samples[c][index]* (1-dist) + this.samples[c][index+1] * (dist));
            }
            //System.out.println((currentSample + i * speed * _speed) + " = " + (index) + " + " + dist);
            //System.out.println(this.samples[index] + " - " + this.samples[index+1] + " -> " + ret[i]);

        }
        currentSample = Math.min(currentSample + samples * speed * _speed, this.samples[0].length);
        System.out.println("Current sample is " + currentSample + "(+ " + samples + " )");

        return ret;
    }

    @Override
    public int getChannels() {
        return channels;
    }

}
