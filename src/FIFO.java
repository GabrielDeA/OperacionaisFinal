import java.util.List;

public class FIFO {
    public static void executarFIFO(List<Processo> processos) {
        int tempoAtual = 0;
        System.out.println("Iniciando execução dos processos em FIFO:");
        for (Processo processo : processos) {
            System.out.println(processo.nome + " iniciado no tempo " + tempoAtual);

            if (!processo.executado) {
                processo.executado = true;
                tempoAtual += processo.tempoParaExecucao;
                System.out.println(processo.nome + " executado completamente no tempo " + tempoAtual);
            }
            else {
                System.out.println(processo.nome + " já foi executado anteriormente.");
            }
        }
        System.out.println("Todos os processos foram executados em FIFO.");
    }
}
