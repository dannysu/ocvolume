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

package ocvolume.dsp;

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> FFT class for real signals. Upon entry, N contains the numbers of points in the DFT, real[] and imaginary[]
 * contain the real and imaginary parts of the input. Upon return, real[] and imaginary[] contain the DFT output.
 * All signals run from 0 to N - 1<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> featureExtraction<br>
 * <b>input:</b> speech signal<br>
 * <b>output:</b> real and imaginary part of DFT output
 * @author Danny Su
 */
public class fft{
    /**
     * number of points
     */
    protected static int numPoints;
    /**
     * real part
     */
    public static double real[];
    /**
     * imaginary part
     */
    public static double imag[];
    
    /**
     * performs Fast Fourier Transformation<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param signal  */
    public static void computeFFT(double signal[]){
        numPoints = signal.length;

        // initialize real & imag array
        real = new double[numPoints];
        imag = new double[numPoints];
        
        // move the N point signal into the real part of the complex DFT's time domain
        real = signal;
        
        // set all of the samples in the imaginary part to zero
        for (int i = 0; i < imag.length; i++){
            imag[i] = 0;
        }
        
        // perform FFT using the real & imag array
        FFT();
    }
    
    /**
     * performs Fast Fourier Transformation<br>
     * calls: none<br>
     * called by: fft
     */
    private static void FFT(){
        final double pi = Math.PI;
        final int numStages = (int)(Math.log(numPoints) / Math.log(2));
        
        int j = numPoints / 2;
        
        // FFT time domain decomposition carried out by "bit reversal sorting" algorithm
        int k = 0;
        for (int i = 0; i < numPoints - 2; i++){
            if (i < j){
                // swap
                double tempReal = real[j];
                double tempImag = imag[j];
                real[j] = real[i];
                imag[j] = imag[i];
                real[i] = tempReal;
                imag[i] = tempImag;
            }
            
            k = numPoints / 2;
            
            while ( k <= j ){
                j -= k;
                k /= 2;
            }
            
            j += k;
        }

        // loop for each stage
        for (int stage = 1; stage <= numStages; stage++){
            int LE = (int)Math.pow(2, stage);
            int LE2 = LE / 2;
            double UR = 1;
            double UI = 0;
            
            // calculate sine & cosine values
            double SR = Math.cos( pi / LE2 );
            double SI = -Math.sin( pi / LE2 );
            
            // loop for each sub DFT
            for (int subDFT = 1; subDFT <= LE2; subDFT++){
                
                // loop for each butterfly
                for (int butterfly = subDFT - 1; butterfly <= numPoints - 1; butterfly+=LE){
                    int ip = butterfly + LE2;
                    
                    // butterfly calculation
                    double tempReal = real[ip] * UR - imag[ip] * UI;
                    double tempImag = real[ip] * UI + imag[ip] * UR;
                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }
                
                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }
    }
}