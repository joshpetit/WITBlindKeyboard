import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

//When I Type Blind Keyboard
public class MainFrame  extends JFrame {
    //The Key Listener that will listen for when the user holds down two keys at once
    KeyListen keyListen;
    //The keys the user chose to use the program with
    HashMap<String, String> mappedKeys;
    //This is a special listener class that will set the values for the mappedKeys hashMap
    KeyListener setMap;
    //What Users Actually See
    OutputPanel outputPanel;
    //Contains the Key Code properties
    private static Properties keyCodeValues;
    //A Visual display of the buttons currently Being pressed (Unimplemented)
    ButtonsDisplay buttonsDisplay;


    public MainFrame() {
        super("WIT Blind Keyboard");
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(1000, 700);
        this.setLayout(new GridBagLayout());
        this.setExtendedState(MAXIMIZED_BOTH);

        keyCodeValues = new Properties();
        loadKeyCodeValues();
        //Adds the User's keyListener
        buttonsDisplay = new ButtonsDisplay();
        keyListen = new KeyListen();
        mappedKeys = new HashMap<>();
        outputPanel = new OutputPanel();

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = 0;
        gc.weightx = 2;
        gc.weighty = 3;
        add(outputPanel, gc);

        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = 1;
        gc.weightx = 0;
        gc.weighty = 1;
        add(buttonsDisplay, gc);

        initKeyboard();
        this.setVisible(true);
    }

//Initialization Block:  Creates and starts Keyboard based on User Selection
    public void initKeyboard(){
        resetMappedKeys();
        outputPanel.resetMap();
        systemMessage("Type Usage Keys");
    }

    public void resetMappedKeys(){
        if (this.getKeyListeners().length != 0)
            this.removeKeyListener(keyListen);
        mappedKeys.clear();
        mappedKeys.put("Space", "0");

        //The procedure for resetting the mapped Keys
        setMap = new KeyListener() {
            int currentNum = 1;
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                String keyText = KeyEvent.getKeyText(e.getKeyCode());
                //Checks if that Key has not already been selected, then adds it while incrementing currentNum
                if (!mappedKeys.containsKey(keyText)) {
                    mappedKeys.put(keyText, "" + currentNum);
                    addDisplayButton(keyText, currentNum);
                    reloadFrame();
                    currentNum++;
                    if (currentNum == 6){
                        buttonsDisplay.addEmptyButton();
                    }
                }
                else{
                    systemMessage("Invalid Key: Already In Use");
                }

                //The 10 fields are filled.
                if (currentNum > 10){
                    startKeyboard();
                }

            }
            @Override
            public void keyReleased(KeyEvent e) {}
        };

        //Begins the Procedure
        this.addKeyListener(setMap);
    }

    public void addDisplayButton(String label, int num){
        buttonsDisplay.addButton(label, num);
    }

    public void startKeyboard(){
        //Takes the setMap Listener off and puts on the keyListener (The actual program)
        this.removeKeyListener(setMap);
        this.addKeyListener(keyListen);
        setMaxPossibilities();
        systemMessage("Begin Typing");
    }

//Output Block: Output to GUI
    public void appendToOutput(String text){
        outputPanel.message(text);
    }

    public void systemMessage(String message){
        outputPanel.systemMessage(message);
    }

//Translate Block: Getting KeyPair value from Properties
    //TODO: Find a way around this boilerplate code
    public void checkPair(String key1, String key2){
        //Gets the Integer value for the passed in Strings.
        if (!key1.isEmpty() && !key2.isEmpty()){
            key1 = mappedKeys.getOrDefault(key1, "");
            key2 = mappedKeys.getOrDefault(key2, "");
            String elementByCode = (String) keyCodeValues.getOrDefault(key1 + key2, "");

            if (!elementByCode.isEmpty()) {
                appendToOutput(elementByCode);
            }
        }
    }

    //These are Primarily for Display Uses
    public String getKey(String key1, String key2){
        String elementByCode = "-";
        if (!key1.isEmpty() && !key2.isEmpty()) {
            key1 = mappedKeys.getOrDefault(key1, "");
            key2 = mappedKeys.getOrDefault(key2, "");
            elementByCode = (String) keyCodeValues.getOrDefault(key1 + key2, "-");
        }
        if (elementByCode.equals(" ")){elementByCode = "Space"; }
        else if (elementByCode.equals("\n")){elementByCode = "New Line";}
        return elementByCode;
    }
    public String getKey(String key1, int key2){
        String elementByCode = "-";
        if (!key1.isEmpty()) {
            key1 = mappedKeys.getOrDefault(key1, "");
            elementByCode = (String) keyCodeValues.getOrDefault(key1 + key2, "-");
        }
        if (elementByCode.equals("\n")){elementByCode = "New Line";}
        return elementByCode;
    }

    //Displays the possibilities someone may have with the currently held key if they were to press another button
    public void setPossibilities(String currentKey) {
        mappedKeys.keySet().stream()
                .forEach(x ->{
                    buttonsDisplay.setPossibility(x, getKey(currentKey, x));
                });
    }

    public void setMaxPossibilities(){
        mappedKeys.keySet().stream()
                .forEach( x ->{
                    buttonsDisplay.setPossibility(x, getKey(x, 6)  + "-" + getKey(x, 10));
                });
    }
//Utilities Block: Yea.
    private void loadKeyCodeValues() {
        InputStream resourceAsStream = MainFrame.class.getClassLoader().getResourceAsStream("key.properties");
        try {
            keyCodeValues.load(new InputStreamReader(resourceAsStream));
        } catch (IOException e) {
            System.out.println("Error Loading Properties");
        }
    }

    public void reloadFrame(){
        this.revalidate();
    }

    public void copyToClipTray(){
        String myString = outputPanel.getText();
        StringSelection stringSelection = new StringSelection(myString);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    //Class that Orchestrates the Key Pairs, Receives them nad uses checkPair to send to output
    private class KeyListen implements KeyListener {
        String key1 = "", key2 = "";

        @Override
        public void keyPressed(KeyEvent e) {
            String key = KeyEvent.getKeyText(e.getKeyCode());
            if (key1.equals("")) {
                key1 = key;
                setPossibilities(key1);
            } else if (key2.equals("") || !key2.equals(key)) {
                key2 = key;
            }
            if (key1.equals("Space") && key2.equals("Space")){
                copyToClipTray();
                systemMessage("Copied To Clip Tray");
            }
            System.out.printf("Currently: %s and %s\n", key1, key2);
            if (!key1.isBlank() && !key2.isBlank() ){
                checkPair(key1, key2);
            }
            buttonsDisplay.toggleOn(key);

        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyReleased(KeyEvent e) {
            String key = KeyEvent.getKeyText(e.getKeyCode());

            //If the released key is equal to the first key, empty key1
            if (key1.equals(key)) {
                key1 = "";
            }
            //If the released key is equal to the second key, empty key2
            if (key2.equals(key)) {
                key2 = "";
            }

            buttonsDisplay.toggleOff(key);
            setPossibilities(key1);
            if (key1.isEmpty() && key2.isEmpty()){
                setMaxPossibilities();
            }
            System.out.printf("Currently: %s and %s\n", key1, key2);
        }
    }

}

//The Display Panel at the bottom showing what buttons are currently pressed
class ButtonsDisplay extends JPanel
{
    //TODO: Change the notFoundButton to Space, possibly send message that pressed button is not mapped
    Button notFoundButton;

    JPanel primaryButtonsPanel;

    //A List of created Buttons mapped by the key
    Map<String, Button> buttonsList;

    public ButtonsDisplay(){
        super(new BorderLayout());
        primaryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        primaryButtonsPanel.setVisible(true);
        primaryButtonsPanel.setBackground(Color.BLACK);

        this.setBackground(Color.BLACK);
        notFoundButton = new Button("SPACE", 0);
        buttonsList = new HashMap<>();

        add(primaryButtonsPanel, BorderLayout.CENTER);
            add(notFoundButton, BorderLayout.SOUTH);
    }

    //Sets the possibility based on a certain Button
    public void setPossibility(String keyName, String possibility){

        buttonsList.getOrDefault(keyName, notFoundButton).setPossibilitiesLabel(possibility);
    }

    //Creates and adds a button to the buttonsList
    public void addButton(String label, int num){
        Button button = new Button(label, num);
        buttonsList.put(label, button);
        primaryButtonsPanel.add(button);
    }

    //The blank button that separates the right and left hand.
    public void addEmptyButton(){
        primaryButtonsPanel.add(new Button());
    }

    //Toggles a button on based on the passed String, if not found toggle the notFoundButton on
    public void toggleOn(String button){
        buttonsList.getOrDefault(button, notFoundButton).toggleOn();
    }

    //Toggles a button off based on the passed String
    public void toggleOff(String button){
        buttonsList.getOrDefault(button, notFoundButton).toggleOff();
    }

    //The actual button, independent of most things
    private class Button extends JPanel{
        //The key of the Button
        JLabel keyLabel;
        //The number associated with the button's key
        JLabel numberLabel;
        //The possible outputs if this button were to be pressed
        JLabel possibilitiesLabel;
        final Color BGCOLOR = Color.WHITE;
        final Color FGCOLOR = Color.BLACK;
        final int BUTTON_HEIGHT = 95;
        final int BUTTON_WIDTH = 90;
        public Button(String labelName, int num){
            super(new BorderLayout());
            final Font comic_sans = new Font("Comic Sans", Font.BOLD, 25);
            setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
            setBackground(BGCOLOR);

            keyLabel = new JLabel(labelName);
            keyLabel.setFont(comic_sans);
            keyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            keyLabel.setForeground(FGCOLOR);
            add(keyLabel, BorderLayout.CENTER);

            numberLabel = new JLabel("" + num);
            numberLabel.setFont(comic_sans);
            numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
            numberLabel.setForeground(FGCOLOR);
            add(numberLabel, BorderLayout.NORTH);

            possibilitiesLabel = new JLabel("-");
            possibilitiesLabel.setHorizontalAlignment(SwingConstants.CENTER);
            possibilitiesLabel.setFont(comic_sans);
            possibilitiesLabel.setForeground(FGCOLOR);
            add(possibilitiesLabel, BorderLayout.SOUTH);
        }

        //The Space between the two hands
        public Button(){
            setPreferredSize(new Dimension(BUTTON_WIDTH,BUTTON_HEIGHT));
        }
        //Toggles just change the buttons colors
        public void toggleOn(){
            this.setBackground(Color.GREEN);
        }

        public void toggleOff(){
            this.setBackground(BGCOLOR);
        }

        //Sets the label of the Current possibility
        public void setPossibilitiesLabel(String string){
            possibilitiesLabel.setText(string);
        }

    }


}