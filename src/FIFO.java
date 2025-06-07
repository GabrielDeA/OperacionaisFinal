import java.util.LinkedList;
import java.util.Queue;

public class FIFO {
    public static void executarFIFO(Queue<Processo> processos, int maxCiclos) {
        System.out.println("Iniciando execução dos processos em FIFO:");
        int clicloAtual = 0;
        Queue<Processo> esperando = new LinkedList<Processo>();
        Queue<Processo> finalizados = new LinkedList<Processo>();
        int totalProcessos = processos.size();
        Processo processoAtual = null;
        while (clicloAtual <= maxCiclos && (!processos.isEmpty() || !esperando.isEmpty() || processoAtual != null)) {
            if(totalProcessos == finalizados.size()) {
                System.out.println("Todos os processos foram finalizados em FIFO, tempo final: " + clicloAtual);
                return;
            }
            // escolher o próximo a ser executado
            if (processoAtual == null && !processos.isEmpty()) {
                switch (processos.peek().getStatus()) {
                    case Pronto:
                        processoAtual = processos.poll();
                        processoAtual.setStatus(Status.Executando);
                        System.out.println("Processo " + processoAtual.getNome() + " começou a ser executado no ciclo "
                                + clicloAtual);
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
                clicloAtual++;
                continue;
            }

            if (processoAtual != null) {
                if (processoAtual.getTipoEspera() == TipoEspera.Nenhum) {
                    processoAtual.atualizaTempoExecucao();
                } else {
                    esperando.add(processoAtual);
                    System.out.println("Processo " + processoAtual.getNome() + " está esperando sua operação de " 
                            + processoAtual.getTipoEspera() + " no ciclo " + clicloAtual);
                    processoAtual = null;
                    clicloAtual++;
                    continue;
                }
                if (processoAtual.tempoRestante <= 0) {
                    processoAtual.setStatus(Status.Finalizado);
                    finalizados.add(processoAtual);
                    System.out.println("Processo " + processoAtual.getNome() + " finalizou sua execução no ciclo " + clicloAtual);
                    processoAtual = null;
                }
            }

            if (ProcessaEspera.processaEspera(esperando.peek())) {
                System.out.println("Processo " + esperando.peek().getNome() + " finalizou sua espera no ciclo " + clicloAtual);
                processos.add(esperando.poll());
            }

            clicloAtual++;
        }
        System.out.println("Todos os processos foram executados em FIFO, tempo final: " + clicloAtual);
    }

    // int tempoFinal = EscalonadorUtils.executarProcessos(processos);
    // System.out.println("Todos os processos foram executados em FIFO, tempo final:
    // " + tempoFinal);
}
