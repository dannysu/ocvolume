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

package ocvolume;

import java.io.*;
import java.util.*;
import ocvolume.audio.*;
import ocvolume.wav.*;
import ocvolume.dsp.*;
import ocvolume.dsp.featureExtraction;
import ocvolume.vq.*;
import ocvolume.hmm.markov;
//import ocvolume.database.*;

/**
 * speech recognitione engine
 * last updated on June 18, 2002
 * @author Danny Su
 */
public class ocvolume{
    /**
     * words in the dictionary
     */
    private String words[];
    /**
     * boolean indicating whether to use HMM recognition or not
     * if not then use VQ recognition
     */
    private boolean useHMM = false;
    /**
     * hidden markov models for the words in the dictionary
     */
    private markov hmmModels[];
    /**
     * codebooks for the words in the dictionary
     */
    private codebook codebooks[];
    /**
     * number of words in the dictionary
     */
    private int num_words = 0;
    
    /**
     * constructor to create a speech recognition engine using VQ for recognition
     * @param dict file path of the dictionary file that contains all the words that the engine can recognize
     * @param folder path of the folder where *.vq are located
     */
    public ocvolume(String dict, String folder){
        // get all words in the dictionary
        getDict(dict);
        
        // load all VQ codebooks for words in the dictionary file
        codebooks = new codebook[num_words];
        for (int i = 0; i < num_words; i++){
            codebooks[i] = new codebook(folder + words[i] + ".vq");
        }
    }
    
    /**
     * constructor to create a speech recognition engine using HMM for recognition
     * @param dict file path of the dictionary file that contains all the words that the engine can recognize
     * @param pathForHMM path of the folder where *.hmm are located
     * @param pathForCodebook file path of the codebook file that should be used for HMM
     */
    public ocvolume(String dict, String pathForHMM, String pathForCodebook){
        useHMM = true;
        
        // get all words in the dictionary
        getDict(dict);
        
        // load the general codebook used by HMM
        codebooks = new codebook[1];
        codebooks[0] = new codebook(pathForCodebook);
        
        // load all HMM models for words in the dictionary file
        hmmModels = new markov[num_words];
        for (int i = 0; i < num_words; i++){
            hmmModels[i] = new markov(pathForHMM + words[i] + ".hmm");
        }
    }
    
    public String getWord(short signal[]){
        // End-Point Detection
        short signalAfterEndPoint[] = endPt.absCut(signal);
        
        double mfcc[][] = featureExtraction.process(signalAfterEndPoint);
        
        point pts[] = new point[mfcc.length];
        for (int index = 0; index < mfcc.length; index++){
            double temp[] = new double[mfcc[index].length - 1];
            
            for (int i = 1; i < mfcc[index].length; i++){
                temp[i - 1] = mfcc[index][i];
            }
            pts[index] = new point(temp);
        }
        
        if (useHMM){
            int quantized[] = codebooks[0].quantize(pts);
            
            double probs[] = new double[words.length];
            
            for (int j = 0; j < words.length; j++){
                probs[j] = hmmModels[j].viterbi(quantized);
            }
            
            double highest = Double.NEGATIVE_INFINITY;
            int wordIndex = -1;
            
            for (int j = 0; j < words.length; j++){
                if (probs[j] > highest){
                    highest = probs[j];
                    wordIndex = j;
                }
            }
            
            return words[wordIndex];
        }
        else{
            int lowest_index = -1;
            double lowest_dist = Double.MAX_VALUE;
            double dist = 0;
            
            for (int index = 0; index < words.length; index++){
                dist = codebooks[index].getDistortion(pts);
                
                if (dist < lowest_dist){
                    lowest_dist = dist;
                    lowest_index = index;
                }
            }
            
            return words[lowest_index];
        }
    }
    
    /**
     * loads words from dictionary
     * @param file path of the dictionary file
     */
    private void getDict(String dict){
        try{
            FileReader fr = new FileReader(dict);
            BufferedReader br = new BufferedReader(fr);
            String tmp = "";
            Vector v = new Vector(1, 1);
            num_words = 0;
            
            while ( (tmp = br.readLine()) != null ){
                num_words++;
                v.add(tmp);
            }
            
            words = new String[num_words];
            for (int i = 0; i < num_words; i++){
                words[i] = (String)v.elementAt(i);
            }
            
            br.close();
        }
        catch(Exception e){
        }
    }
}