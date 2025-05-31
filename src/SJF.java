import java.util.Collections;
import java.util.List;

public class SJF {
    public void executarSJF(List<Processo> processos) {
        int tempoAtual = 0;
        System.out.println("Iniciando execução dos processos em SJF:");
        // Ordena os processos pelo tempo de chegada e depois pelo tempo de execução
        Collections.sort(processos, (p1, p2) -> {
            return Integer.compare(p1.tempoParaExecucao, p2.tempoParaExecucao);
        });

        for (Processo processo : processos) {
            System.out.println(processo.nome + " iniciado no tempo " + tempoAtual);
            if (!processo.executado) {
                processo.executado = true;
                tempoAtual += processo.tempoParaExecucao;
                System.out.println(processo.nome + " executado completamente no tempo " + tempoAtual);
            } else {
                System.out.println(processo.nome + " já foi executado anteriormente.");
            }
        }
        
    }
}
