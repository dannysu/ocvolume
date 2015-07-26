/*
OC Volume - Java Speech Recognition Engine
Copyright (C) 2002 OrangeCow organization

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

Contact information:
Please visit http://ocvolume.sourceforge.net.
*/

package ocvolume.wav;

import javax.sound.sampled.*;
import java.io.*;

/**
 * last modified: June 15, 2002<br>
 * <b>description:</b> input and output to a wave file<br>
 * <b>calls:</b> none<br>
 * <b>called:</b> tarin<br>
 * <b>input:</b> speech signal, path<br>
 * <b>output:</b> none
 * @author Keith Fung
 */
public class waveIO{
    /**
     * encoding method "pulse-code modulation"  predefined encoding
     */
    final static AudioFormat.Encoding SAMPLE_ENCODING = AudioFormat.Encoding.PCM_SIGNED;
    /**
     * capture amound of sample per second, can be changed in the constructor
     * 16 kHz
     */
    final static float SAMPLE_RATE = 16000.0F;
    /**
     * sample size 16 bits
     */
    final static int SAMPLE_BITS = 16;
    /**
     * mono
     */
    final static int SAMPLE_CHANNELS = 1;
    /**
     * frame rate = 16000
     */
    final static float SAMPLE_FRAME_RATE = 16000.0F;
    /**
     * frame size = 2
     */
    final static int SAMPLE_FRAME_SIZE = 2;
    /**
     * use little-endian<br>
     * 00000100 00000001<br>
     * first byte 00000001<br>
     * second byte 00000100
     */
    final static boolean SAMPLE_BIG_ENDIAN = false;    
    /**
     * audio format
     */
    final static AudioFormat FORMAT = new AudioFormat(SAMPLE_ENCODING, SAMPLE_RATE,SAMPLE_BITS,SAMPLE_CHANNELS,SAMPLE_FRAME_SIZE,SAMPLE_FRAME_RATE,SAMPLE_BIG_ENDIAN);

    /**
     * write to wave file<br>
     * calls: none<br>
     * called by: train
     * @param sample the data in 16bits integer (short) array
     * @param path the save location
     */
    public static void writeWave(short sample[], String path){
        //inputting data to a wav file

        byte sampleByte[] = new byte[sample.length * 2];
        
        for (int c=0; c < sample.length; c++){
            sampleByte[2 * c] = (byte)sample[c];
            sampleByte[2 * c + 1] = (byte)(sample[c]>>8);
        }


        try {
            ByteArrayInputStream sampleByteArrayInputStream = new ByteArrayInputStream(sampleByte);
            AudioInputStream sampleAudioInputStream = new AudioInputStream (sampleByteArrayInputStream, FORMAT, sampleByte.length / SAMPLE_FRAME_SIZE);
            if (AudioSystem.write(sampleAudioInputStream,AudioFileFormat.Type.WAVE, new File(path + ".wav")) == -1){
                System.out.println("Unable to write to file");
            }
/*
            else{
                System.out.println("Finish writing to file");
            }
*/
        }
        catch (Exception e){
            e.printStackTrace();               
        }
    }

///////////////////////////////////////////////////////////////

    /**
     * loading wave file<br>
     * calls: none<br>
     * called by: train     
     * @param path of the input wave file
     * @return a short array of anysize containing the amplitudes in the wave file
     */
    public static short[] readWave(String path){


        /**
         * define a file object with the location given
         */
        File fileRead = new File(path);

        /**
         * initial buffer size
         */
        int byteRead = 16000 * 2;

        /**
         * array used to temporary store the read data
         */
        byte waveByte[] = new byte[byteRead];

        /**
         * array used to store the return data
         */
        short waveShort[];

        /**
         * store the number of bytes read from the wave file
         */
        int numByteRead;

        try {
            /**
             * Byte array that used to store data read from the wave file
             */
            ByteArrayOutputStream readByteArrayOutputStream = new ByteArrayOutputStream();

            /**
             * open the wave file
             */
            AudioInputStream readAudioInputStream = AudioSystem.getAudioInputStream(fileRead);

            while ((numByteRead = readAudioInputStream.read(waveByte, 0, waveByte.length)) != -1){
                readByteArrayOutputStream.write(waveByte,0,numByteRead);
            }

            /**
             * temporary array to store data in readByteArrayOutputStream
             */
            byte tempWaveByte[] = readByteArrayOutputStream.toByteArray();

            waveShort = new short[tempWaveByte.length / 2];

            //convert 2 bytes into a short
            for (int c = 0 ; c < waveShort.length ; c++){
                waveShort[c] = (short)((tempWaveByte[2 * c + 1] << 8) + (tempWaveByte[2 * c] >= 0 ? tempWaveByte[2 * c] : tempWaveByte[2 * c] + 256));
            }
            return waveShort;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        waveShort = new short[1];
        return waveShort;
    }
}