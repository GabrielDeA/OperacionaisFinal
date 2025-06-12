import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class EscalonadorUtils {

    public static int executarProcessos(List<Processo> processos) {
        int tempoAtual = 0;
        /* 
        for (Processo processo : processos) {
            System.out.println(processo.nome + " iniciado no tempo " + tempoAtual);
            if (!processo.executado) {
                processo.executado = true;
                tempoAtual += processo.tempoParaExecucao;
                System.out.println(processo.nome + " executado completamente no tempo " + tempoAtual);
            } else {
                System.out.println(processo.nome + " já foi executado anteriormente.");
            }
        }*/
        return tempoAtual;

    }

    public static void logProcessEvent(String filename, String processName, int time, String event) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(processName + "," + time + "," + event + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
