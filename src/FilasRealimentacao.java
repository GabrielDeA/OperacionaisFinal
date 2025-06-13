import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class FilasRealimentacao {

    private static class Fila {
        Queue<Processo> processos = new LinkedList<>();
        int quantum;
        int id;

        Fila(int id, int quantum) {
            this.quantum = quantum;
        }

        private int getQuantum() {
            return this.quantum;
        }
    }

    public static void executarFilasRealimentacao(Queue<Processo> processos, int maxCiclos) {
        Random random = new Random();
        int contadorId = -1;
        Fila fila0 = new Fila(contadorId++, random.nextInt(10));
        Fila fila1 = new Fila(contadorId++, random.nextInt(fila0.getQuantum(), fila0.getQuantum() + 10));
        Fila fila2 = new Fila(contadorId++, random.nextInt(fila1.getQuantum(), fila1.getQuantum() + 10));
        Queue<Processo> esperando = new LinkedList<>();
        Queue<Processo> finalizados = new LinkedList<>();
        // ordenadas por prioridade
        Fila[] filas = { fila0, fila1, fila2 };

        int cicloAtual = 0;
        Processo atual = null;

        // Processos iniciam na fila de maior prioridade
        fila0.processos = processos;


        class MetodosPrivados {
            private void promover(Processo processo) {
                if (processo.getFila() == 0)
                    return;
                processo.setFila(processo.getFila() - 1);
                for (Fila fila : filas) {
                    if (fila.id == processo.getFila())
                        fila.processos.add(processo);
                }
            }

            private void rebaixar(Processo processo) {
                if (processo.getFila() == filas.length)
                    return;
                processo.setFila(processo.getFila() + 1);
                for (Fila fila : filas) {
                    if (fila.id == processo.getFila())
                        fila.processos.add(processo);
                }
            }

        }
        MetodosPrivados metodos = new MetodosPrivados();
        //#region Inicio da execução
        while (true) {
            // Escolhe proximo processo a ser executado
            if (atual == null) {
                if (!fila0.processos.isEmpty()) {
                    atual = fila0.processos.poll();
                    atual.setQuantum(fila0.getQuantum());
                    atual.setFila(fila0.id);
                } else if (!fila1.processos.isEmpty()) {
                    atual = fila1.processos.poll();
                    atual.setQuantum(fila1.getQuantum());
                    atual.setFila(fila1.id);
                } else if (!fila2.processos.isEmpty()) {
                    atual = fila2.processos.poll();
                    atual.setQuantum(fila2.getQuantum());
                    atual.setFila(fila2.id);
                }
                cicloAtual++;
                continue;
            }

            if(ProcessaEspera.processaEspera(esperando.peek())){
                for (Fila fila : filas) {
                    if(fila.id == esperando.peek().getFila()) {
                        fila.processos.add(esperando.poll());
                    }
                }
                cicloAtual++;
                continue;
            }

            if(atual.getQuantum() <= 0) {
                metodos.rebaixar(atual);
                atual = null;
                cicloAtual++;
                continue;
            }

            if(atual.getTempoRestante() <= 0) {
                finalizados.add(atual);
                atual = null;
            }

            if (atual.getTipoEspera() == TipoEspera.Nenhum) {
                atual.atualizaTempoEspera();
                atual.atualizaQuantum();
                cicloAtual++;
                continue;
            } else {
                esperando.add(atual);
                metodos.promover(atual);
                atual = null;
                cicloAtual++;
                continue;
            }

        }

    }
}
