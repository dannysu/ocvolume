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

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> point class which stores coordinates in k-dimensional space<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> centroid, codebook<br>
 * <b>input:</b> set of co-ordinates<br>
 * <b>output:</b> none
 * @author Danny Su
 * @author Andrei Leonov
 */
public class point{
    /**
     * k-dimensional coordinates array
     */
    protected double coordinates[];
    /**
     * k-dimensions
     */
    protected int dimension;
    /**
     * constructor to create a point with k-dimensional coordinates array<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @param co k-dimensional coordinates array
     */
    public point(double co[]){
        dimension = co.length;
        coordinates = co;
    }
    /**
     * get all the coordinates<br>
     * calls: none<br>
     * called by: codebook
     * @return coordinates as a double array
     */
    public double[] getAllCo(){
        return coordinates;
    }
    /**
     * get coordinate at specific index<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @param i index number
     * @return coordinate at index i
     */
    public double getCo(int i){
        return coordinates[i];
    }
    /**
     * set coordinate at specific index<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @param i index number
     * @param value value of coordinate
     */
    public void setCo(int i, double value){
        coordinates[i] = value;
    }
    /**
     * replace coordinates with new ones<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @param tCo new coordinates array
     */
    public void changeCo(double tCo[]){
        coordinates = tCo;
    }
    /**
     * get the k-dimensional space that the point is in<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @return dimension
     */
    public int getDimension(){
        return dimension;
    }
    /**
     * check whether two points are identical<br>
     * calls: none<br>
     * called by: centroid, codebook
     * @param p1 first point
     * @param p2 second point
     * @return true/false indicating whether two points are identical
     */
    public static boolean equals(point p1, point p2){
        boolean equal = true;
        int d = p1.getDimension();
        
        // dimension of two points has to be the same
        if (d == p2.getDimension()){
            // compares all coordinates
            for (int k = 0; k < d && equal; k++){
                // if any of the coordinates are not the same then these two points are not the same
                if ( p1.getCo(k) != p2.getCo(k)){
                    equal = false;
                }
            }
        }
        else{
            equal = false;
        }
        
        return equal;
    }
}