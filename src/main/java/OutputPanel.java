
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import org.apache.http.util.ByteArrayBuffer;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.concurrent.locks.Condition;

public class OutputPanel extends JPanel {
    private JLabel systemOutput;
    private JTextArea userOutput;
    private boolean capitalize = false;
    private LocalMaryInterface maryInterface;
    Clip clip;
    private Thread speech;
    private String lastMessage;

    final Font COMIC_SANS = new Font("Comic Sans", Font.BOLD, 30);
    public OutputPanel(){
        super(new GridBagLayout());
        initSpeech();

        systemOutput = new JLabel("Type Usage Keys");
        systemOutput.setFont(COMIC_SANS);
        userOutput = new JTextArea();
        userOutput.setEnabled(false);
        userOutput.setEditable(false);
        userOutput.setLineWrap(true);
        userOutput.setFont(COMIC_SANS);
        userOutput.setColumns(30);
        userOutput.setDisabledTextColor(Color.BLACK);

        GridBagConstraints gc = new GridBagConstraints();

        gc.anchor = GridBagConstraints.NORTH;
        gc.gridx = 0;
        gc.weighty = 1;
        gc.gridy = 0;
        add(systemOutput,  gc);

        gc.anchor = GridBagConstraints.SOUTH;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.weightx= 200;
        gc.weighty = 10;
        gc.gridy = 1;
        add(new JScrollPane(userOutput), gc);
    }

    public void initSpeech(){
        try {
            maryInterface = new LocalMaryInterface();

        } catch (MaryConfigurationException e) {
            e.printStackTrace();
        }
    }

    //Find a way to prevent the words spoken from tripping over each other
    public void speak(String message){
        if (clip != null && clip.isOpen()){
            clip.close();
            System.out.println("Due?");
        }
         speech = new Thread(() -> {
            try {
                AudioInputStream ais = maryInterface.generateAudio(message);
                clip = AudioSystem.getClip();
                clip.open(ais);
                clip.start();
                if (Thread.currentThread().isInterrupted()){
                    clip.close();
                }
            } catch (LineUnavailableException e) {
                e.printStackTrace();
            } catch (SynthesisException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }

        });
        speech.start();

    }

    public void resetMap(){
        systemOutput.setText("Type Usage Keys");
    }

    //Convert to switch statement
    public void message(String message){
        if (message.equalsIgnoreCase("Backspace")){
            String current = userOutput.getText();
            userOutput.setText(current.substring(0, current.length()-1));
        }
        else if( message.equalsIgnoreCase("Space")){
            userOutput.append(" ");
        }
        else if( message.equalsIgnoreCase("New Line")){
            userOutput.append("\n");
        }
        else if( message.equalsIgnoreCase("Tab")){
            userOutput.append("\t");
        }
        else if( message.equalsIgnoreCase("Capitalize")){
            capitalize = !capitalize;
        }
        else{
            message = capitalize ? message.toUpperCase(): message;
            userOutput.append(message);
        }
        //In the case of constant repeated letters
            speak(message);
        lastMessage = message;
    }

    public String getText(){ return userOutput.getText();}

    public void systemMessage(String message){systemOutput.setText(message);}



}



