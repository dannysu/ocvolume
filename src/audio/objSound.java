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

package ocvolume.audio;

/**
 * last modified: June 13, 2002<br>
 * <b>description:</b> combine several 100ms frames<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> micInput<br>
 * <b>input:</b> speech signal<br>
 * <b>output:</b> speech signal
 * @author Keith Fung
 */

public class objSound{

    private short sound[];
    private int curPos = 0;

    /**
     * constructor to create a new short array
     * calls: none
     * called by: volume
     * @param totalSample length of short array
     */
    public objSound(int totalSample){
        sound = new short[totalSample];
    }

    /**
     * combining samples
     * calls: none
     * called by: volume     
     * @param sample speech signal
     * @param sampleLength speech signal length
     */
    public void addSound(short sample[], int sampleLength){
        for (int c = 0; c < sampleLength; c++){
            sound[c + curPos] = sample[c];
        }
        curPos += sampleLength;
    }
    /**
     * return speech signal with a given time(index)
     * calls: none
     * called by: volume
     * @param IndexNum time/index
     * @return amplitude for a specified time
     */
    public short getSoundAt(int IndexNum){
        return sound[IndexNum];
    }
    /**
     * return the signal
     * calls: none
     * called by: volume
     * @return signal
     */
    public int getSoundSize(){
        return sound.length;
    }
    
    /**
     * return speech signal
     * calls: none
     * called by: volume     
     * @return speech signal
     */
    public short[] getSound(){
        return sound;
    }
}