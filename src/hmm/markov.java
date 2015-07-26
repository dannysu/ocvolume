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

package ocvolume.hmm;

import java.io.*;
import java.util.*;
import java.sql.*;

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> this class represents a left-to-right Hidden Markov Model and its essential methods for speech recognition.
 * The collection of methods include Forward-Backward Algorithm, Baum-Welch Algorithm, Scaling, Viterbi, etc.<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> volume, train<br>
 * <b>input:</b> sequence of integers<br>
 * <b>output:</b> probability
 * @author Danny Su
 */
public class markov{
    /**
     * minimum probability
     */
    final double MIN_PROBABILITY = 0.00000000001;
    /**
     * length of observation sequence
     */
    protected int len_obSeq;
    /**
     * number of state in the model
     * example: number of urns
     */
    protected int num_states;
    /**
     * number of observation symbols per state
     * example: how many different colour balls there are
     */
    protected int num_symbols;
    /**
     * number of states the model is allowed to jump
     */
    protected final int delta = 2;
    /**
     * discrete set of observation symbols
     * example: sequence of colour of balls
     */
    protected int obSeq[][];
    /**
     * current observation sequence
     */
    protected int currentSeq[];
    /**
     * number of observation sequence
     */
    protected int num_obSeq;
    /**
     * state transition probability
     * example: probability from one state to another state
     */
    protected double transition[][];
    /**
     * discrete output probability
     * example: probability of a specific output from a state
     */
    protected double output[][];
    /**
     * initial state distribution
     * example: which state is the starting state
     */
    protected double pi[];
    /**
     * forward variable alpha
     */
    protected double alpha[][];
    /**
     * backward variable beta
     */
    protected double beta[][];
    /**
     * Scale Coefficient
     */
    protected double scaleFactor[];
    /**
     * variable for viterbi algorithm
     */
    private int psi[][];
    /**
     * best state sequence
     */
    public int q[];
    /**
     * viterbi algorithm used to get best state sequence and probability<br>
     * calls: none<br>
     * called by: volume
     * @param testSeq test sequence
     * @return probability
     */
    public double viterbi(int testSeq[]){
        setObSeq(testSeq);
        double phi[][] = new double[len_obSeq][num_states];
        psi = new int[len_obSeq][num_states];
        q = new int[len_obSeq];
        
        for (int i = 0; i < num_states; i++){
            double temp = pi[i];
            if (temp == 0){
                temp = MIN_PROBABILITY;
            }
            
            phi[0][i] = Math.log(temp) + Math.log(output[i][currentSeq[0]]);
            psi[0][i] = 0;
        }
        
        for (int t = 1; t < len_obSeq; t++){
            for (int j = 0; j < num_states; j++){
                double max = phi[t - 1][0] + Math.log(transition[0][j]);
                double temp = 0;
                int index = 0;
                
                for (int i = 1; i < num_states; i++){
                    
                    temp = phi[t - 1][i] + Math.log(transition[i][j]);
                    if (temp > max){
                        max = temp;
                        index = i;
                    }
                    
                }
                
                phi[t][j] = max + Math.log( output[j][currentSeq[t]] );
                psi[t][j] = index;
            }
        }
        
        double max = phi[len_obSeq - 1][0];
        double temp = 0;
        int index = 0;
        for (int i = 1; i < num_states; i++){
            temp = phi[len_obSeq - 1][i];
            
            if (temp > max){
                max = temp;
                index = i;
            }
        }
        
        q[len_obSeq - 1] = index;
        
        for (int t = len_obSeq - 2; t >= 0; t--){
            q[t] = psi[t + 1][q[t + 1]];
        }
        
        return max;
    }
    /**
     * rescales backward variable beta to prevent underflow<br>
     * calls: none<br>
     * called by: markov
     * @param t index number of backward variable beta
     */
    private void rescaleBeta(int t){
        for (int i = 0; i < num_states; i++){
            beta[t][i] *= scaleFactor[t];
        }
    }
    /**
     * rescales forward variable alpha to prevent underflow<br>
     * calls: none<br>
     * called by: markov
     * @param t index number of forward variable alpha
     */
    private void rescaleAlpha(int t){
        // calculate scale coefficients
        for (int i = 0; i < num_states; i++){
            scaleFactor[t] += alpha[t][i];
        }
        
        scaleFactor[t] = 1 / scaleFactor[t];
        
        // apply scale coefficients
        for (int i = 0; i < num_states; i++){
            alpha[t][i] *= scaleFactor[t];
        }
    }
    /**
     * returns the probability calculated from the testing sequence<br>
     * calls: none<br>
     * called by: volume
     * @param testSeq testing sequence
     * @return probability of observation sequence given the model
     */
    public double getProbability(int testSeq[]){
        setObSeq(testSeq);
        double temp = computeAlpha();
        
        return temp;
    }
    /**
     * calculate forward variable alpha<br>
     * calls: none<br>
     * called by: markov
     * @return probability
     */
    protected double computeAlpha(){
        /**
         * Pr(obSeq | model);
         * Probability of the observation sequence given the hmm model
         */
        double probability = 0;
        
        // reset scaleFactor[]
        for (int t = 0; t < len_obSeq; t++){
            scaleFactor[t] = 0;
        }
        
        /**
         * Initialization: Calculating all alpha variables at time = 0
         */
        for (int i = 0; i < num_states; i++){
            alpha[0][i] = pi[i] * output[i][currentSeq[0]];
        }
        rescaleAlpha(0);
        
        /**
         * Induction:
         */
        for (int t = 0; t < len_obSeq - 1; t++){
            for (int j = 0; j < num_states; j++){
                
                /**
                 * Sum of all alpha[t][i] * transition[i][j]
                 */
                double sum = 0;
                
                /**
                 * Calculate sum of all alpha[t][i] * transition[i][j], 0 <= i < num_states
                 */
                for (int i = 0; i < num_states; i++){
                    sum += alpha[t][i] * transition[i][j];
                }
                
                alpha[t + 1][j] = sum * output[j][currentSeq[t + 1]];
            }
            rescaleAlpha(t + 1);
        }
        
        
        /**
         * Termination: Calculate Pr(obSeq | model)
         */
        for (int i = 0; i < num_states; i++){
            probability += alpha[len_obSeq - 1][i];
        }
        
        probability = 0;
        //double totalScaleFactor = 1;
        for (int t = 0; t < len_obSeq; t++){
            // System.out.println("s: " + Math.log(scaleFactor[t]));
            
            probability += Math.log( scaleFactor[t] );
            
            // totalScaleFactor *= scaleFactor[t];
        }
        
        return -probability;
        //return porbability / totalScaleFactor;
    }
    /**
     * calculate backward variable beta for later use with Re-Estimation method<br>
     * calls: none<br>
     * called by: markov
     */
    protected void computeBeta(){
        /**
         * Initialization: Set all beta variables to 1 at time = len_obSeq - 1
         */
        for (int i = 0; i < num_states; i++){
            beta[len_obSeq - 1][i] = 1;
        }
        rescaleBeta(len_obSeq - 1);
        
        /**
         * Induction:
         */
        for (int t = len_obSeq - 2; t >= 0; t--){
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_states; j++){
                    beta[t][i] += transition[i][j] * output[j][currentSeq[t + 1]] * beta[t + 1][j];
                }
            }
            rescaleBeta(t);
        }
    }
    /**
     * set the number of training sequences<br>
     * calls: none<br>
     * called by: trainHMM
     * @param k number of training sequences
     */
    public void setNumObSeq(int k){
        num_obSeq = k;
        obSeq = new int[k][];
    }
    /**
     * set a training sequence for re-estimation step<br>
     * calls: none<br>
     * called by: trainHMM
     * @param k index representing kth training sequence
     * @param trainSeq training sequence
     */
    public void setTrainSeq(int k, int trainSeq[]){
        obSeq[k] = trainSeq;
    }
    /**
     * set training sequences for re-estimation step<br>
     * calls: none<br>
     * called by: trainHMM
     * @param trainSeq training sequences
     */
    public void setTrainSeq(int trainSeq[][]){
        num_obSeq = trainSeq.length;
        
        for (int k = 0; k < num_obSeq; k++){
            obSeq[k] = trainSeq[k];
        }
    }
    /**
     * train the hmm model until no more improvement<br>
     * calls: none<br>
     * called by: trainHMM
     */
    public void train(){
        // re-estimate 25 times
        // NOTE: should be changed to re-estimate until no more improvement
        for (int i = 0; i < 25; i++){
            reestimate();
        }
    }
    
    /**
     * Baum-Welch Algorithm - Re-estimate (iterative udpate and improvement) of HMM parameters<br>
     * calls: none<br>
     * called by: trainHMM
     */
    private void reestimate(){
        // new probabilities that will be the optimized and replace the older version
        double newTransition[][] = new double[num_states][num_states];
        double newOutput[][] = new double[num_states][num_symbols];
        double numerator[] = new double[num_obSeq];
        double denominator[] = new double[num_obSeq];
        
        // calculate new transition probability matrix
        double sumP = 0;
        
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_states; j++){
                
                if (j < i || j > i + delta){
                    newTransition[i][j] = 0;
                }
                else{
                    for (int k = 0; k < num_obSeq; k++){
                        numerator[k] = denominator[k] = 0;
                        setObSeq(obSeq[k]);
                        
                        sumP += computeAlpha();
                        computeBeta();
                        
                        for (int t = 0; t < len_obSeq - 1; t++){
                            numerator[k] += alpha[t][i] * transition[i][j] * output[j][currentSeq[t + 1]] * beta[t + 1][j];
                            denominator[k] += alpha[t][i] * beta[t][i];
                        }
                    }
                    
                    double denom = 0;
                    for (int k = 0; k < num_obSeq; k++){
                        newTransition[i][j] += (1 / sumP) * numerator[k];
                        denom += (1 / sumP) * denominator[k];
                    }
                    
                    newTransition[i][j] /= denom;
                    newTransition[i][j] += MIN_PROBABILITY;
                }
            }
        }
        
        // calculate new output probability matrix
        sumP = 0;
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_symbols; j++){
                for (int k = 0; k < num_obSeq; k++){
                    numerator[k] = denominator[k] = 0;
                    setObSeq(obSeq[k]);
                    
                    sumP += computeAlpha();
                    computeBeta();
                    
                    for (int t = 0; t < len_obSeq - 1; t++){
                        if ( currentSeq[t] == j ){
                            numerator[k] += alpha[t][i] * beta[t][i];
                        }
                        denominator[k] += alpha[t][i] * beta[t][i];
                    }
                }
                
                double denom = 0;
                for (int k = 0; k < num_obSeq; k++){
                    newOutput[i][j] += (1 / sumP) * numerator[k];
                    denom += (1 / sumP) * denominator[k];
                }
                
                newOutput[i][j] /= denom;
                newOutput[i][j] += MIN_PROBABILITY;
            }
        }
        
        // replace old matrices after re-estimate
        transition = newTransition;
        output = newOutput;
    }
    
    /**
     * set observation sequence<br>
     * calls: none<br>
     * called by: trainHMM
     * @param observationSeq observation sequence
     */
    public void setObSeq(int observationSeq[]){
        currentSeq = observationSeq;
        len_obSeq = observationSeq.length;
        
        alpha = new double[len_obSeq][num_states];
        beta = new double[len_obSeq][num_states];
        scaleFactor = new double[len_obSeq];
    }
    
    /**
     * class constructor - used to create a model from a saved file<br>
     * calls: none<br>
     * called by: volume, trainHMM
     * @param filepath path of the file to load
     */
    public markov(String filepath){
        try{
            FileReader fr = new FileReader(filepath);
            BufferedReader br = new BufferedReader(fr);
            
            String temp = "";
            StringTokenizer st;
            
            while( (temp = br.readLine()) != null ){
                if ( temp.equals("Number of States:") ){
                    num_states = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Observation Symbols:") ){
                    num_symbols = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Number of Samples:") ){
                    num_obSeq = Integer.parseInt( br.readLine() );
                }
                else if ( temp.equals("Output Matrix:") ){
                    output = new double[num_states][num_symbols];
                    for (int i = 0; i < num_states; i++){
                        st = new StringTokenizer( br.readLine() );
                        for (int j = 0; j < num_symbols; j++){
                            output[i][j] = Double.parseDouble(st.nextToken());
                        }
                    }
                }
                else if ( temp.equals("Transition Matrix:") ){
                    transition = new double[num_states][num_states];
                    for (int i = 0; i < num_states; i++){
                        st = new StringTokenizer( br.readLine() );
                        for (int j = 0; j < num_states; j++){
                            transition[i][j] = Double.parseDouble(st.nextToken());
                        }
                    }
                }
                else if ( temp.equals("pi:") ){
                    pi = new double[num_states];
                    st = new StringTokenizer( br.readLine() );
                    
                    for (int i = 0; i < num_states; i++){
                        pi[i] = Double.parseDouble(st.nextToken());
                    }
                }
            }
            
            br.close();
        }
        catch(IOException e){
        }
        catch(Exception e){
        }
    }
    
    /**
     * class constructor - used to create a left-to-right model with multiple observation sequences for training<br>
     * calls: none<br>
     * called by: trainHMM
     * @param num_states number of states in the model
     * @param num_symbols number of symbols per state
     */
    public markov(int num_states, int num_symbols){
        this.num_states = num_states;
        this.num_symbols = num_symbols;
        transition = new double[num_states][num_states];
        output = new double[num_states][num_symbols];
        pi = new double[num_states];
        
        /**
         * in a left-to-right HMM model, the first state is always the initial state. e.g. probability = 1
         */
        pi[0] = 1;
        for (int i = 1; i < num_states; i++){
            pi[i] = 0;
        }
        
        // generate random probability for all the other probability matrices
        randomProb();
    }
    
    /**
     * generates random probabilities for transition, output probabilities<br>
     * calls: none<br>
     * called by: markov
     */
    private void randomProb(){
        double remainder = 1;
        for (int i = 0; i < num_states; i++){
            for (int j = 0; j < num_states - 1; j++){
                if (j < i || j > i + delta){
                    transition[i][j] = 0;
                }
                else{
                    double randNum = Math.random() * remainder;
                    transition[i][j] = randNum;
                    remainder -= randNum;
                }
            }
            transition[i][num_states - 1] = remainder;
            
            remainder = 1;
            for (int j = 0; j < num_symbols - 1; j++){
                double randNum = Math.random() * remainder;
                output[i][j] = randNum;
                remainder -= output[i][j];
            }
            output[i][num_symbols - 1] = remainder;
        }
    }
    
    /**
     * save HMM model to file<br>
     * calls: none<br>
     * called by: trainHMM
     * @param filepath file location
     */
    public void save(String filepath){
        try{
            FileWriter fw = new FileWriter(filepath);
            BufferedWriter bw = new BufferedWriter(fw);
            
            bw.write("Number of States:");
            bw.newLine();
            bw.write(num_states + "");
            bw.newLine();
            bw.write("Observation Symbols:");
            bw.newLine();
            bw.write(num_symbols + "");
            bw.newLine();
            bw.write("Number of Samples:");
            bw.newLine();
            bw.write(num_obSeq + "");
            bw.newLine();
            bw.write("Output Matrix:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_symbols; j++){
                    bw.write(output[i][j] + "");
                    if (j != num_symbols - 1){
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }
            
            bw.write("Transition Matrix:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                for (int j = 0; j < num_states; j++){
                    bw.write(transition[i][j] + "");
                    if (j != num_states - 1){
                        bw.write(" ");
                    }
                }
                bw.newLine();
            }
            
            bw.write("pi:");
            bw.newLine();
            
            for (int i = 0; i < num_states; i++){
                bw.write(pi[i] + "");
                if (i != num_states - 1){
                    bw.write(" ");
                }
            }
            
            bw.close();
        }
        catch(IOException e){
        }
        catch(Exception e){
        }
    }
}