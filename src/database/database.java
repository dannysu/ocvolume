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

package ocvolume.database;

import java.sql.*;
import java.io.*;
import java.util.*;

/**
 * last updated on June 15, 2002<br>
 * <b>description:</b> class used to manipulate database<br>
 * <b>calls:</b> none<br>
 * <b>called by:</b> volume<br>
 * <b>input:</b> data to be stored in database<br>
 * <b>output:</b> data read from database
 * @author Danny Su
 */
public class database{
    /**
     * data type DOUBLE
     */
    public static final String DOUBLE = "DOUBLE";
    /**
     * statement - used to send SQL commands to db
     */
    private Statement stmt;
    /**
     * connection to database
     */
    private Connection con;
    /**
     * get number of rows in a table<br>
     * calls: none<br>
     * called by: volume
     * @param tableName table to get row count from
     * @return number of rows
     */
    public int getRowCount(String tableName){
        int rows = 0;

        try{
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName);
            if ( rs != null ){
                rs.next();
                rows = rs.getInt(1);
            }
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        return rows;
    }
    /**
     * retrieve data from entire table<br>
     * calls: none<br>
     * called by: volume
     * @param tableName table to get data from
     * @return result set
     */
    public ResultSet retrieve(String tableName){
        tableName = tableName.replace(' ', '_');
        ResultSet rs = null;
        try{
            rs = stmt.executeQuery("SELECT * FROM " + tableName);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
        
        return rs;
    }
    
    /**
     * inserts a row to the specified table<br>
     * calls: none<br>
     * called by: chgToDB
     * @param tableName table to add the new row to
     * @param values values for all the columns in the table
     */
    public void insertRow(String tableName, double values[]){
        tableName = tableName.replace(' ', '_');
        String sqlcmd = "INSERT INTO " + tableName + " VALUES(";
        for (int i = 0; i < values.length; i++){
            sqlcmd += values[i] + "";
            if (i != values.length - 1){
                sqlcmd += ", ";
            }
        }
        sqlcmd += ")";
        
        System.out.println(sqlcmd);
        
        try{
            stmt.execute(sqlcmd);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
    }
    
    /**
     * creates a table in database<br>
     * calls: none<br>
     * called by: chgToDB
     * @param tableName name of table to be created
     * @param columns column names
     * @param dataTypes data types for the columns
     */
    public void createTable(String tableName, String columns[], String dataTypes[]){
        tableName = tableName.replace(' ', '_');
        String sqlcmd = "CREATE TABLE " + tableName + " ( ";
        for (int i = 0; i < columns.length; i++){
            sqlcmd += columns[i] + " " + dataTypes[i];
            if (i != columns.length - 1){
                sqlcmd += ", ";
            }
        }
        sqlcmd += " )";
        
        System.out.println(sqlcmd);
        
        try{
            stmt.execute(sqlcmd);
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
    }
    
    /**
     * close connection to database<br>
     * calls: none<br>
     * called by: volume, chgToDB
     */
    public void close(){
        try{
            stmt.close();
            con.close();
        }
        catch(Exception e){
        }
    }
    
    /**
     * constructor to load database<br>
     * calls: none<br>
     * called by: volume, chgToDB
     * @param dataSourceName database source
     */
    public database(String dataSourceName){
        try{           
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            con = DriverManager.getConnection("jdbc:odbc:" + dataSourceName);
            
            stmt = con.createStatement();
        }
        catch(SQLException SQLE) {
            System.out.println(SQLE.toString());
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }
}