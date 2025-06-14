import java.io.IOException;
import java.util.List;
import java.util.Queue;

public class Main {
    public static void main(String[] args) throws IOException {
        new java.io.FileWriter("process_log.csv", false).close();
        int contadorId = 0;

        Processo processo1 = new Processo("P1", contadorId++, 2, 1, TipoEspera.IO, 3);
        Processo processo2 = new Processo("P2", contadorId++, 5, 2, TipoEspera.Nenhum, 0);
        Processo processo3 = new Processo("P3", contadorId++, 8, 3, TipoEspera.Memoria, 2);
        Processo processo4 = new Processo("P4", contadorId++, 6, 4, TipoEspera.IO, 1);
        Processo processo5 = new Processo("P5", contadorId++, 12, 5, TipoEspera.Memoria, 4);

        Queue<Processo> processos = new java.util.LinkedList<>();
        processos.add(processo1);
        processos.add(processo2);
        processos.add(processo3);
        processos.add(processo4);
        processos.add(processo5);
        //FIFO.executarFIFO(processos, 1000);
        //SJF.executarSJF((List<Processo>) processos);
        //RoundRobin.executarRoundRobin((List<Processo>) processos,6);
        FilasRealimentacao.executarFilasRealimentacao(processos, 1000);
    }
}