
import marytts.LocalMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class OutputPanel extends JPanel {
    private JLabel systemOutput;
    private JTextArea userOutput;
    private JButton toggleAudio;

    private boolean capitalize = false;
    private Thread speech;
    private boolean audioOn = false;
    private String message = ".";
    private BlockingQueue speechQueue;

    final Font COMIC_SANS = new Font("Comic Sans", Font.BOLD, 30);
    public OutputPanel(){
        super(new GridBagLayout());
        initSpeech();

        systemOutput = new JLabel("Type Usage Keys");
        systemOutput.setFont(COMIC_SANS);

        toggleAudio = new JButton("Toggle Audio " + (this.audioOn ? "Off" : "On"));
        toggleAudio.setFocusable(false);

        toggleAudio.addActionListener(actionEvent -> {
            audioOn = !audioOn;
            String text = "Toggle Audio " + (audioOn ?  "Off" : "On");
            toggleAudio.setText(text);
        });

        userOutput = new JTextArea();
        userOutput.setEnabled(false);
        userOutput.setEditable(false);
        userOutput.setLineWrap(true);
        userOutput.setFont(COMIC_SANS);
        userOutput.setColumns(30);
        userOutput.setDisabledTextColor(Color.BLACK);


        GridBagConstraints gc = new GridBagConstraints();


        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 2;
        add(toggleAudio, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.weighty= 1;
        gc.weightx = 10;
        add(systemOutput, gc);

        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = 2;
        gc.weightx= 1;
        gc.weighty = 1;
        add(new JScrollPane(userOutput), gc);
    }

    //Uses MarryTTS to generate audio, may need to create custom sounds.
    public void initSpeech(){
        try {
            LocalMaryInterface maryInterface = new LocalMaryInterface();
            speechQueue = new ArrayBlockingQueue<String>(1);
            Clip clip = AudioSystem.getClip();
            speech = new Thread(() -> {
                try {
                    while (audioOn) {
                        AudioInputStream ais = maryInterface.generateAudio(this.message);
                        clip.open(ais);
                        clip.start();
                        this.message = (String) speechQueue.take();
                        clip.close();
                    }
                } catch (LineUnavailableException | IOException | SynthesisException | InterruptedException e) {
                    e.printStackTrace();
                }

            });
        } catch (MaryConfigurationException | LineUnavailableException e) {
            e.printStackTrace();
        }
        speech.start();
    }

    //Only speak if audio is on and the queue is empty (prevents sounds from playing over each other)
    public void speak(String message){
        if (audioOn && speechQueue.isEmpty()) {
            speechQueue.add(message);
        }

    }
    //Not much right now, but if output panel will be needed to reinitialize certain things
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
        speak(message);
    }

    public String getText(){ return userOutput.getText();}

    public void systemMessage(String message){systemOutput.setText(message);}




}



