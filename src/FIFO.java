import java.util.List;

public class FIFO {
    public static void executarFIFO(List<Processo> processos) {
        System.out.println("Iniciando execução dos processos em FIFO:");
        int tempoFinal = EscalonadorUtils.executarProcessos(processos);
        System.out.println("Todos os processos foram executados em FIFO, tempo final: " + tempoFinal);
    }
}
