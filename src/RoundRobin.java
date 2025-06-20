import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {

    public static void executarRoundRobin(List<Processo> processos, int quantum) {
        System.out.println("Iniciando execucao dos processos em Round Robin (quantum = " + quantum + "):");

        int tempoAtual = 0;
        Queue<Processo> fila = new LinkedList<>(processos);
        List<Processo> esperando = new ArrayList<>();
        List<Processo> finalizados = new ArrayList<>();

        while (finalizados.size() < processos.size()) {

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
                tempoAtual++;
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
                        fila.add(atual); // ainda tem execucao, volta pra fila
                        System.out.println("[" + tempoAtual + "] Processo " + atual.getNome() + " pausado (resta " + atual.getTempoRestante() + ")");
                    }
                }

            } else {
                fila.add(atual);
                tempoAtual++;
            }
        }

        System.out.println("Todos os processos foram executados em Round Robin. Tempo total de execucao: " + tempoAtual + ".");
    }

    public static void executarRoundRobinComCores(List<Processo> processos, int quantum, int numCores) {
        System.out.println("Iniciando Round Robin com " + numCores + " cores e quantum " + quantum);

        Queue<Processo> fila = new ConcurrentLinkedQueue<>(processos);
        List<Processo> esperando = Collections.synchronizedList(new ArrayList<>());
        List<Processo> finalizados = Collections.synchronizedList(new ArrayList<>());
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        CountDownLatch latch = new CountDownLatch(numCores);
        AtomicInteger tempoAtual = new AtomicInteger(0);
        AtomicInteger cicloAtual = new AtomicInteger(1);

        int[] temposExecutados = new int[numCores];
        Object temposLock = new Object();

        CyclicBarrier barrier = new CyclicBarrier(numCores, () -> {
            int maxTempo = 0;
            synchronized (temposLock) {
                for (int t : temposExecutados) {
                    if (t > maxTempo) maxTempo = t;
                }
                Arrays.fill(temposExecutados, 0);
            }
            tempoAtual.addAndGet(maxTempo);
            synchronized (System.out) {
                System.out.println("Cycle " + cicloAtual.get() + " ended. [All cores finished their turn]");
                EscalonadorUtils.logProcessEvent("process_log.csv", "CYCLE", cicloAtual.get(), "CYCLE_END");
            }
            cicloAtual.incrementAndGet();
        });

        for (int coreId = 0; coreId < numCores; coreId++) {
            int finalCoreId = coreId;
            pool.execute(() -> {
                try {
                    while (finalizados.size() < processos.size()) {
                        // Process waiting
                        synchronized (esperando) {
                            Iterator<Processo> itEspera = esperando.iterator();
                            while (itEspera.hasNext()) {
                                Processo p = itEspera.next();
                                if (ProcessaEspera.processaEspera(p)) {
                                    p.setStatus(Status.Pronto);
                                    fila.add(p);
                                    itEspera.remove();
                                    EscalonadorUtils.logProcessEvent("process_log.csv", p.getNome(), tempoAtual.get(), "WAIT_FINISHED");
                                }
                            }
                        }

                        Processo atual = fila.poll();
                        int tempoExecutado = 0;
                        if (atual == null) {
                            // Core idle
                        } else {
                            synchronized (atual) {
                                if (atual.getStatus() == Status.Pronto) {
                                    atual.setStatus(Status.Executando);
                                    System.out.println("[Core " + finalCoreId + " | " + tempoAtual.get() + "] Executando " + atual.getNome());
                                    EscalonadorUtils.logProcessEvent("process_log.csv", atual.getNome(), tempoAtual.get(), "STARTED");

                                    boolean entrouEmEspera = false;

                                    while (tempoExecutado < quantum && atual.getTempoRestante() > 0) {
                                        if (atual.getTipoEspera() != TipoEspera.Nenhum) {
                                            atual.setStatus(Status.Esperando);
                                            esperando.add(atual);
                                            System.out.println("[Core " + finalCoreId + " | " + tempoAtual.get() + "] Processo " + atual.getNome() + " está esperando sua operacao de " + atual.getTipoEspera());
                                            EscalonadorUtils.logProcessEvent("process_log.csv", atual.getNome(), tempoAtual.get() +1, "WAITING_" + atual.getTipoEspera());
                                            entrouEmEspera = true;
                                            break;
                                        }
                                        atual.atualizaTempoExecucao();
                                        tempoExecutado++;
                                    }

                                    if (!entrouEmEspera) {
                                        if (atual.getTempoRestante() <= 0) {
                                            atual.setStatus(Status.Finalizado);
                                            finalizados.add(atual);
                                            System.out.println("[Core " + finalCoreId + " | " + tempoAtual.get() + "] Processo " + atual.getNome() + " finalizado.");
                                            EscalonadorUtils.logProcessEvent("process_log.csv", atual.getNome(), tempoAtual.get() +1, "FINISHED");
                                        } else {
                                            atual.setStatus(Status.Pronto);
                                            fila.add(atual);
                                            System.out.println("[Core " + finalCoreId + " | " + tempoAtual.get() + "] Processo " + atual.getNome() + " pausado (resta " + atual.getTempoRestante() + ")");
                                            EscalonadorUtils.logProcessEvent("process_log.csv", atual.getNome(), tempoAtual.get(), "PAUSED");
                                        }
                                    }
                                } else {
                                    fila.add(atual);
                                }
                            }
                        }
                        synchronized (temposLock) {
                            temposExecutados[finalCoreId] = tempoExecutado;
                        }
                        barrier.await();
                    }
                } catch (InterruptedException | BrokenBarrierException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pool.shutdown();
        System.out.println("Todos os processos foram executados em Round Robin com cores. Tempo total de execucao: " + tempoAtual.get() + ".");
        EscalonadorUtils.logProcessEvent("process_log.csv", "todos", tempoAtual.get(), "ALL_FINISHED");
    }
}
