import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FIFO {
    public static void executarFIFO(Queue<Processo> processos, int maxCiclos) {
        System.out.println("Iniciando execução dos processos em FIFO:");
        int clicloAtual = 0;
        Queue<Processo> esperando = new LinkedList<Processo>();
        Queue<Processo> finalizados = new LinkedList<Processo>();
        Processo processoAtual = null;
        while(clicloAtual <= maxCiclos) {
            //escolher o próximo a ser executado
            if(processoAtual == null) {
                switch (processos.peek().getStatus()) {
                    case Pronto:
                        processoAtual = processos.poll();
                        processoAtual.setStatus(Status.Executando);
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
            }

            if(processoAtual.getTipoEspera() == TipoEspera.Nenhum) {
                processoAtual.atualizaTempoExecucao();
            } else {
                esperando.add(processoAtual);
                processoAtual = null;
            }
            
            }

            clicloAtual++;
        }


       // int tempoFinal = EscalonadorUtils.executarProcessos(processos);
       // System.out.println("Todos os processos foram executados em FIFO, tempo final: " + tempoFinal);
    }
