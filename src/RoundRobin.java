import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {

    public static void executarRoundRobin(List<Processo> processos, int quantum) {
        System.out.println("Iniciando execução dos processos em Round Robin (quantum = " + quantum + "):");

        int tempoAtual = 0;
        Queue<Processo> fila = new LinkedList<>(processos);
        List<Processo> esperando = new ArrayList<>();
        List<Processo> finalizados = new ArrayList<>();

        while (finalizados.size() < processos.size()) {

            // Processar esperas
            Iterator<Processo> itEspera = esperando.iterator();
            while (itEspera.hasNext()) {
                Processo p = itEspera.next();
                if (ProcessaEspera.processaEspera(p)) {
                    p.setStatus(Status.Pronto);
                    fila.add(p);
                    itEspera.remove();
                }
            }

            Processo atual = fila.poll();
            if (atual == null) {
                tempoAtual++; // avança tempo enquanto espera processo sair do bloqueio
                continue;
            }

            if (atual.getStatus() == Status.Pronto) {
                atual.setStatus(Status.Executando);
                System.out.println("[" + tempoAtual + "] Executando " + atual.getNome());

                int tempoExecutado = 0;
                boolean entrouEmEspera = false;

                while (tempoExecutado < quantum && atual.getTempoRestante() > 0) {
                    if (atual.getTipoEspera() != TipoEspera.Nenhum) {
                        atual.setStatus(Status.Esperando);
                        esperando.add(atual);
                        entrouEmEspera = true;
                        break;
                    }

                    atual.atualizaTempoExecucao();
                    tempoExecutado++;
                    tempoAtual++;
                }

                if (!entrouEmEspera) {
                    if (atual.getTempoRestante() <= 0) {
                        atual.setStatus(Status.Finalizado);
                        finalizados.add(atual);
                        System.out.println("[" + tempoAtual + "] Processo " + atual.getNome() + " finalizado.");
                    } else {
                        atual.setStatus(Status.Pronto);
                        fila.add(atual); // ainda tem execução, volta pra fila
                        System.out.println("[" + tempoAtual + "] Processo " + atual.getNome() + " pausado (resta " + atual.getTempoRestante() + ")");
                    }
                }

            } else {
                // Processo inesperadamente em estado errado, talvez acabou de sair da espera
                fila.add(atual);
                tempoAtual++;
            }
        }

        System.out.println("Todos os processos foram executados em Round Robin. Tempo total de execução: " + tempoAtual + ".");
    }

    public static void executarRoundRobinComCores(List<Processo> processos, int quantum, int numCores) {
        System.out.println("Iniciando Round Robin com " + numCores + " cores e quantum " + quantum);

        Queue<Processo> fila = new ConcurrentLinkedQueue<>(processos);
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        CountDownLatch latch = new CountDownLatch(numCores);
        AtomicInteger cicloAtual = new AtomicInteger(1);

        CyclicBarrier barrier = new CyclicBarrier(numCores, () -> {
            synchronized (System.out) {
                System.out.println("Cycle " + cicloAtual.getAndIncrement() + " ended. [All cores finished their turn]");
            }
        });

        for (int coreId = 0; coreId < numCores; coreId++) {
            int finalCoreId = coreId;
            pool.execute(() -> {
                while (true) {
                    Processo processo = fila.poll();
                    if (processo == null) {
                        try {
                            barrier.await();
                        } catch (InterruptedException | BrokenBarrierException e) {
                            break;
                        }
                        if (fila.isEmpty()) break;
                        else continue;
                    }

                    synchronized (processo) {
                        if (processo.getStatus() == Status.Finalizado) continue;

                        processo.setStatus(Status.Executando);
                        System.out.println("[Core " + finalCoreId + "] Executando " + processo.getNome());

                        int tempoExecutado = Math.min(quantum, processo.getTempoRestante());
                        for (int i = 0; i < tempoExecutado; i++) {
                            processo.atualizaTempoExecucao();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        if (processo.getTempoRestante() <= 0) {
                            processo.setStatus(Status.Finalizado);
                            System.out.println("[Core " + finalCoreId + "] Processo " + processo.getNome() + " finalizado.");
                        } else {
                            processo.setStatus(Status.Pronto);
                            fila.add(processo);
                        }
                    }

                    try {
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        break;
                    }
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        pool.shutdown();
        System.out.println("Round Robin finalizado.");
    }

}
