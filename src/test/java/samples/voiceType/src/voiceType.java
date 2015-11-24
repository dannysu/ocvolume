package samples.voiceType.src;/*
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

import org.oc.ocvolume.audio.micInput;
import org.oc.ocvolume.ocvolume;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

// for ocvolume
// end for ocvolume

/**
 * Voice TypeWriter using OC Volume
 * @author Danny Su
 * @author Keith Fung
 */
public class voiceType extends JFrame implements Runnable{
    
    // for ocvolume
    private ocvolume engine = new ocvolume("dict", "");
    private JFrame frame = this;
    private JButton record; // button for start and stop recording
    private micInput mic = new micInput();
    private Thread checking; // thread checking if there is new word to be recognized
    // end ocvolume
    
    /**
     * width of window
     */
    protected final int WIDTH = 300;
    /**
     * height of window
     */
    protected final int HEIGHT = 350;
    /**
     * words in the dictionary
     */
    private String words[];
    /**
     * text area
     */
    private JTextArea txt = new JTextArea();
    /**
     * menubar
     */
    private JMenuBar menuBar;
    /**
     * menu
     */
    private JMenu menu;
    /**
     * each item in the menu
     */
    private JMenuItem menuItem;
    /**
     * checkbox for word wrap
     */
    private JCheckBoxMenuItem cbMenuItem;
    /**
     * checkbox for using continuous recording or not
     */
    private JCheckBoxMenuItem cbMenuItem2;
    /**
     * file chooser to open or save file
     */
    private JFileChooser fc = new JFileChooser();
    /**
     * option pane use to show msg box
     */
    private JOptionPane optionPane = new JOptionPane();
    /**
     * default font
     */
    private Font defaultFont = new Font("Arial", Font.PLAIN, 10);
    /**
     * boolean indicating whether the txt file has been saved
     */
    private boolean saved = true;
    private String filepath = "";
    /**
     * codebooks for the words in the dictionary
     */
    
    private boolean continuous = true;
    
    /**
     * creates new form volume
     */
    public voiceType(String winName){
        super(winName);
        initComponents();
        setSize(WIDTH, HEIGHT);
        this.setLocation(WIDTH, HEIGHT);
    }
    
    /**
     * this method is called from within the constructor to
     * initialize the form
     */
    private void initComponents(){
        
        //Create the menu bar.
        menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        //Build the first menu.
        menu = new JMenu("File");
        menu.setMnemonic(KeyEvent.VK_F);
        menu.setFont(defaultFont);
        menuBar.add(menu);
        
        menuItem = new JMenuItem("New", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                checkSaved();
                clearText();
            }
        }
        );
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Open...", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                checkSaved();
                if (saved){
                    int returnVal = fc.showOpenDialog(frame);
                    
                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        
                        //this is where a real application would open the file.
                        try{
                            FileReader fr = new FileReader(file);
                            BufferedReader br = new BufferedReader(fr);
                            String temp = "";
                            
                            clearText();
                            
                            while( (temp = br.readLine()) != null ){
                                txt.append(temp + "\n");
                            }
                            
                            br.close();
                        }
                        catch(Exception e){}
                    }
                    else{
                        
                    }
                }
            }
        }
        );
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if (filepath != ""){
                }
                else{
                    saveFile();
                }
            }
        }
        );
        menu.add(menuItem);
        
        menuItem = new JMenuItem("Save As...");
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                saveFile();
            }
        }
        );
        menu.add(menuItem);
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Exit", KeyEvent.VK_X);
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                exit();
            }
        }
        );
        menu.add(menuItem);
        
        // Format menu
        menu = new JMenu("Configuration");
        menu.setMnemonic(KeyEvent.VK_O);
        menu.setFont(defaultFont);
        menuBar.add(menu);
        
        cbMenuItem = new JCheckBoxMenuItem("Word Wrap");
        cbMenuItem.setFont(defaultFont);
        cbMenuItem.setState(true);
        cbMenuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                // if word wrap is checked
                if (cbMenuItem.getState()){
                    txt.setLineWrap(true);
                }
                else{
                    txt.setLineWrap(false);
                }
            }
        }
        );
        menu.add(cbMenuItem);
        
        cbMenuItem2 = new JCheckBoxMenuItem("continous recording");
        cbMenuItem2.setFont(defaultFont);
        cbMenuItem2.setState(true);
        cbMenuItem2.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                // if continuous recording is checked
                if (cbMenuItem2.getState()){
                    continuous = true;
                }
                else{
                    continuous = false;
                }
            }
        }
        );
        menu.add(cbMenuItem2);
        
        // Help menu
        menu = new JMenu("Help");
        menu.setMnemonic(KeyEvent.VK_H);
        menu.setFont(defaultFont);
        menuBar.add(menu);
        
        menuItem = new JMenuItem("About");
        menuItem.setFont(defaultFont);
        menuItem.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                JPanel panel = new JPanel();
                panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
                
                JLabel name = new JLabel("OC Volume - training");
                name.setFont(defaultFont);
                panel.add(name);
                
                JLabel url = new JLabel("Visit http://ocvolume.sourceforge.net for more detail.");
                url.setFont(defaultFont);
                panel.add(url);
                
                optionPane.showMessageDialog(voiceType.this, panel, "About", JOptionPane.PLAIN_MESSAGE);
            }
        }
        );
        menu.add(menuItem);
        
        txt.addKeyListener(
        new KeyListener(){
            public void keyTyped(KeyEvent e){
                saved = false;
            }
            public void keyPressed(KeyEvent e){}
            public void keyReleased(KeyEvent e){}
        }
        );
        
        txt.setWrapStyleWord(true);
        txt.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(txt);
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        
        // for ocvolume
        record = new JButton("record");
        record.addActionListener(
        new ActionListener(){
            public void actionPerformed(ActionEvent event){
                if ( record.getLabel().equals("record") == true ){
                    record.setLabel("stop");
                    mic = new micInput();
                    
                    if (!continuous){
                        mic.setDiscrete();
                    }
                    mic.start();
                    try{
                        checking.stop();
                    }
                    catch(NullPointerException e){}
                    checking = new Thread((Runnable)frame);
                    checking.start();
                }
                else if ( record.getLabel().equals("stop") == true ){
                    record.setLabel("record");
                    mic.stopRecord();
                }
            }
        }
        );
        getContentPane().add(record, BorderLayout.SOUTH);
        // end for ocvolume
        
        addWindowListener(
        new WindowAdapter(){
            public void windowClosing(WindowEvent event) {
                exit();
            }
        }
        );
        
        pack();
    }
    
    // for ocvolume
    public void run(){
        while(true){
            mic.removeOldWord();
            
            while(!mic.byteArrayComplete()){
                try{
                    checking.sleep(200);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            
            mic.newWord();
            short recordedSample[] = mic.returnShortArray();
            
            String recognizedWord = engine.getWord(recordedSample);
            
		txt.append(recognizedWord + " ");
        }
    }
    // end for ocvolume
    
    /**
     * exit the program
     */
    private void exit(){
        checkSaved();
        if (saved){
            System.exit(0);
        }
    }
    
    /**
     * create an instance of volume
     */
    public static void main(String args[]) {
        new voiceType("OC Volume - Voice TypeWriter").show();
    }
    
    /**
     * checks whether the txt has been saved
     */
    private void checkSaved(){
        int n = JOptionPane.CANCEL_OPTION;
        
        if (!saved){
            Object options[] = { "Yes", "No", "Cancel" };
            
            n = optionPane.showOptionDialog(voiceType.this,
            "The text in the file has changed.\n\nDo you want to save the changes?",
            "OC Volume - Voice TypeWriter",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[2]);
            if (n == JOptionPane.YES_OPTION){
                saveFile();
            }
            else if (n == JOptionPane.NO_OPTION){
                saved = true;
            }
        }
    }
    
    /**
     * clears all txt in text area
     */
    private void clearText(){
        if (saved){
            txt.setText("");
        }
    }
    
    /**
     * saves the txt file
     */
    private void saveFile(){
        int returnVal = fc.showSaveDialog(voiceType.this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
            //this is where a real application would open the file.
            try{
                FileWriter fw = new FileWriter(file);
                BufferedWriter bw = new BufferedWriter(fw);
                
                bw.write(txt.getText());
                
                bw.close();
                saved = true;
            }
            catch(Exception e){}
        }
    }
}