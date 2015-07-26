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
 * <b>description:</b> store 100ms of samples (1600 samples), and calculate the averge square mean<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> micInput<br>
 * <b>input:</b> 100ms signal<br>
 * <b>output:</b> 100ms signal, squared mean energy
 * @author Keith Fung
 */

public class objFrame{
    final int BUFFER_SIZE = 3200;
    private short sample[] = new short[(int)(BUFFER_SIZE / 2)];
    private double avgEnergy = 0;

    /**
     * constructor to calculate squared mean energy<br>
     * calls: none<br>
     * called by: volume
     * @param bufferRead 100ms speech signal
     */
    public objFrame(short bufferRead[]){

        double runningSum = 0;
        for (int c = 0; c < sample.length; c++){
            sample[c] = bufferRead[c];
            runningSum += bufferRead[c] * bufferRead[c];
        }
        avgEnergy = runningSum / (BUFFER_SIZE / 2);
    }

    /**
     * return squared mean energy<br>
     * calls: none<br>
     * called by: volume
     * @return squared mean energy
     */
    public double getAvgEnergy(){
        return avgEnergy;
    }

    /**
     * return 100ms speech signal<br>
     * calls: none<br>
     * called by: volume     
     * @return 100ms speech signal
     */
    public short[] getSample(){
        return sample;
    }
}
