import java.util.LinkedList;
import java.util.Queue;

public class FIFO {
    public static void executarFIFO(Queue<Processo> processos, int maxCiclos) {
        System.out.println("Iniciando execução dos processos em FIFO:");
        int cicloAtual = 0;
        Queue<Processo> esperando = new LinkedList<Processo>();
        Queue<Processo> finalizados = new LinkedList<Processo>();
        int totalProcessos = processos.size();
        Processo processoAtual = null;
        while (cicloAtual <= maxCiclos && (!processos.isEmpty() || !esperando.isEmpty() || processoAtual != null)) {
            if (totalProcessos == finalizados.size()) {
                System.out.println("Todos os processos foram finalizados em FIFO, tempo final: " + cicloAtual);
                return;
            }
            // escolher o próximo a ser executado
            if (processoAtual == null && !processos.isEmpty()) {
                switch (processos.peek().getStatus()) {
                    case Pronto:
                        processoAtual = processos.poll();
                        processoAtual.setStatus(Status.Executando);
                        System.out.println("Processo " + processoAtual.getNome() + " começou a ser executado no ciclo "
                                + cicloAtual);
                        break;
                    case Esperando:
                        esperando.add(processos.poll());
                        break;
                    case Finalizado:
                        finalizados.add(processos.poll());
                        break;
                    default:
                        break;
                }
                cicloAtual++;
                continue;
            }

            if (processoAtual != null) {
                if (processoAtual.getTipoEspera() == TipoEspera.Nenhum) {
                    processoAtual.atualizaTempoExecucao();
                } else {
                    esperando.add(processoAtual);
                    System.out.println("Processo " + processoAtual.getNome() + " está esperando sua operação de "
                            + processoAtual.getTipoEspera() + " no ciclo " + cicloAtual);
                    processoAtual = null;
                    cicloAtual++;
                    continue;
                }
                if (processoAtual.getTempoRestante() <= 0) {
                    processoAtual.setStatus(Status.Finalizado);
                    finalizados.add(processoAtual);
                    System.out.println("Processo " + processoAtual.getNome() + " finalizou sua execução no ciclo " + cicloAtual);
                    processoAtual = null;
                }
            }

            if (ProcessaEspera.processaEspera(esperando.peek())) {
                System.out.println("Processo " + esperando.peek().getNome() + " finalizou sua espera no ciclo " + cicloAtual);
                processos.add(esperando.poll());
            }

            cicloAtual++;
        }
        System.out.println("Todos os processos foram executados em FIFO, tempo final: " + cicloAtual);
    }

    // int tempoFinal = EscalonadorUtils.executarProcessos(processos);
    // System.out.println("Todos os processos foram executados em FIFO, tempo final:
    // " + tempoFinal);
}
