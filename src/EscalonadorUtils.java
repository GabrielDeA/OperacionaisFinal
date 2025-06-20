import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class EscalonadorUtils {

    public static void logProcessEvent(String filename, String processName, int time, String event) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(processName + "," + time + "," + event + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
