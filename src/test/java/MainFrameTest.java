import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class MainFrameTest {

    void test() throws IOException {
        Properties props= new Properties();
        props.load(new InputStreamReader(MainFrame.class.getResourceAsStream("key.properties")));
        assertEquals(" ", props.get("10"));

    }




}