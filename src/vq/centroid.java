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

package ocvolume.vq;

import java.util.Vector;
import java.util.Enumeration;

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> centroid of a codebook
 * <b>calls:</b> point<br>
 * <b>called by:</b> codebook<br>
 * <b>input:</b> k-dimensional points<br>
 * <b>output:</b> distortion measure
 * @author Danny Su
 * @author Andrei Leonov
 */
public class centroid extends point{
    /**
     * distortion measure - sum of all points' distances from the centroid
     */
    protected double distortion = 0;
    /**
     * stores the points that belong to this centroid or cell
     */
    protected Vector pts = new Vector(0);
    /**
     * total number of points that belong to this centroid or cell
     */
    protected int total_pts;
    /**
     * constructor to create a centroid from input coordinates<br>
     * calls: none<br>
     * called by: codebook
     * @param Co coordinates array
     */
    public centroid(double Co[]){
        super(Co);
        total_pts = 0;
    }
    /**
     * get a point at specified index<br>
     * calls: none<br>
     * called by: codebook
     * @param index index number
     * @return the point at the specified index
     */
    public point getPoint(int index){
        return (point)pts.get(index);
    }
    /**
     * returns the number of points in this cell<br>
     * calls: none<br>
     * called by: codebook
     * @return number of points
     */
    public int getNumPts(){
        return total_pts;
    }
    /**
     * removes a given point from the centroid's cell<br>
     * calls: none<br>
     * called by: codebook
     * @param pt the point to be removed
     * @param dist distance from the centroid
     */
    public void remove(point pt, double dist){
        point tmpPoint = (point)pts.get(0);
        int i = -1;
        
        Enumeration enum = pts.elements();
        boolean found = false;
        while( enum.hasMoreElements() && !found ){
            tmpPoint = (point)enum.nextElement();
            i++;
            
            // find the identical point in pts vector
            if ( point.equals(pt, tmpPoint) ){
                found = true;
            }
        }
        
        if (found){
            // remove point from pts vector
            pts.remove(i);
            // update distortion measure
            distortion -= dist;
            // update number of points
            total_pts--;
        }
        else{
            System.out.println("err: point not found");
        }
    }
    /**
     * add point to centroid's cell<br>
     * calls: none<br>
     * called by: codebook
     * @param pt a point belonging to the centroid
     * @param dist distance from the centroid
     */
    public void add(point pt, double dist){
        // update number of points
        total_pts++;
        // add point to pts vector
        pts.add(pt);
        // update distortion measure
        distortion += dist;
    }
    /**
     * update centroid by taking average of all points in the cell<br>
     * calls: none<br>
     * called by: codebook
     */
    public void update(){
        double sum_coordinates[] = new double[dimension];
        point tmpPoint;
        Enumeration enum = pts.elements();
        
        while( enum.hasMoreElements() ){
            tmpPoint = (point)enum.nextElement();
            
            // calculate the sum of all coordinates
            for (int k = 0; k < dimension; k++){
                sum_coordinates[k] += tmpPoint.getCo(k);
            }
        }
        
        // divide sum of coordinates by total number points to get average
        for( int k = 0 ; k < dimension; k++){
            setCo(k, sum_coordinates[k] / (double)total_pts);
            pts = new Vector(0);
        }
        
        // reset number of points
        total_pts = 0;
        // reset distortion measure
        distortion = 0;
    }
    /**
     * returns the distortion measure of the current cell<br>
     * calls: none<br>
     * called by: codebook
     * @return distortion of current cell
     */
    public double getDistortion(){
        return distortion;
    }
}