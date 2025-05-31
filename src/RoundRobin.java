import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class RoundRobin {

    public static void executarRoundRobin(List<Processo> processos, int quantum) {
        System.out.println("Iniciando execução dos processos em Round Robin (quantum = " + quantum + "):");

        int tempoAtual = 0;

        Queue<Processo> fila = new LinkedList<>(processos);

        while (!fila.isEmpty()) {
            Processo processo = fila.poll();

            if (processo.tempoParaExecucao > 0) {
                System.out.println(processo.nome + " iniciado no tempo " + tempoAtual);

                int tempoExecutado = Math.min(quantum, processo.tempoParaExecucao);
                tempoAtual += tempoExecutado;
                processo.tempoParaExecucao -= tempoExecutado;

                if (processo.tempoParaExecucao > 0) {
                    System.out.println(processo.nome + " pausado no tempo " + tempoAtual + " (restando " + processo.tempoParaExecucao + ")");
                    fila.add(processo); // ainda precisa de mais tempo, volta pra fila
                } else {
                    processo.executado = true;
                    System.out.println(processo.nome + " finalizado no tempo " + tempoAtual);
                }
            }
        }

        System.out.println("Todos os processos foram executados em Round Robin. Tempo total de execução: " + tempoAtual + ".");
    }
}
