import java.util.Comparator;
import java.util.List;

public class SJF {
    public static void executarSJF(List<Processo> processos) {
        System.out.println("Iniciando execução dos processos em SJF:");

        // Ordena por tempo de execução
        processos.sort(Comparator.comparingInt(Processo::getTempoParaExecucao));

        int tempoFinal = EscalonadorUtils.executarProcessos(processos);
        System.out.println("Todos os processos foram executados em SJF, tempo final: " + tempoFinal);
    }
}
