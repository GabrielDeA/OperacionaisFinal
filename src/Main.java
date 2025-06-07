import java.util.List;
import java.util.Queue;

public class Main {
    public static void main(String[] args) {
        
        int contadorId = 0;

        Processo processo1 = new Processo("Processo1", contadorId++, 2, 1, TipoEspera.IO, 3);
        Processo processo2 = new Processo("Processo2", contadorId++, 5, 2, TipoEspera.Nenhum, 0);
        Processo processo3 = new Processo("Processo3", contadorId++, 8, 3, TipoEspera.Memoria, 2);
        Processo processo4 = new Processo("Processo4", contadorId++, 6, 4, TipoEspera.IO, 1);
        Processo processo5 = new Processo("Processo5", contadorId++, 12, 5, TipoEspera.Memoria, 4);

        Queue<Processo> processos = new java.util.LinkedList<>();
        processos.add(processo1);
        processos.add(processo2);
        processos.add(processo3);
        processos.add(processo4);
        processos.add(processo5);
        FIFO.executarFIFO(processos, 1000);
        //SJF.executarSJF(processos);
        //RoundRobin.executarRoundRobin(processos,6);
    }
}