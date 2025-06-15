import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
                EscalonadorUtils.logProcessEvent("process_log.csv", "todos", 0, " os processos finalizaram");
                return;
            }
            // escolher o próximo a ser executado
            if (processoAtual == null && !processos.isEmpty()) {
                switch (processos.peek().getStatus()) {
                    case Pronto:
                        processoAtual = processos.poll();
                        processoAtual.setStatus(Status.Executando);
                        System.out.println("Processo " + processoAtual.getNome() + " começou a ser executado no ciclo " + cicloAtual);
                        EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual, "STARTED");
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
                    EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual, "WAITING_" + processoAtual.getTipoEspera());
                    processoAtual = null;
                    cicloAtual++;
                    continue;
                }
                if (processoAtual.getTempoRestante() <= 0) {
                    processoAtual.setStatus(Status.Finalizado);
                    finalizados.add(processoAtual);
                    System.out.println("Processo " + processoAtual.getNome() + " finalizou sua execução no ciclo " + cicloAtual);
                    EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual, "FINISHED");
                    processoAtual = null;
                }
            }

            if (ProcessaEspera.processaEspera(esperando.peek())) {
                System.out.println("Processo " + esperando.peek().getNome() + " finalizou sua espera no ciclo " + cicloAtual);
                EscalonadorUtils.logProcessEvent("process_log.csv", esperando.peek().getNome(), cicloAtual, "WAIT_FINISHED");
                processos.add(esperando.poll());
            }

            cicloAtual++;
        }
        System.out.println("Todos os processos foram executados em FIFO, tempo final: " + cicloAtual);
        EscalonadorUtils.logProcessEvent("process_log.csv", "todos", cicloAtual, "ALL_FINISHED");
    }
    public static void executarFIFOComCores(List<Processo> processos, int numCores) {
        System.out.println("Iniciando FIFO com " + numCores + " cores");

        Queue<Processo> fila = new ConcurrentLinkedQueue<>(processos);
        Queue<Processo> esperando = new ConcurrentLinkedQueue<>();
        Queue<Processo> finalizados = new ConcurrentLinkedQueue<>();
        ExecutorService pool = Executors.newFixedThreadPool(numCores);
        CountDownLatch latch = new CountDownLatch(numCores);
        AtomicInteger cicloAtual = new AtomicInteger(0);
        int totalProcessos = processos.size();

        CyclicBarrier barrier = new CyclicBarrier(numCores, () -> {
            synchronized (System.out) {
                System.out.println("Cycle " + cicloAtual.incrementAndGet() + " ended. [All cores finished their turn]");
            }
        });

        for (int coreId = 0; coreId < numCores; coreId++) {
            int finalCoreId = coreId;
            pool.execute(() -> {
                Processo processoAtual = null;
                while (true) {
                    // Exit condition: all processes finished
                    if (finalizados.size() >= totalProcessos) break;

                    // Process waiting queue (all threads can help)
                    Processo esperandoPeek = esperando.peek();
                    while (esperandoPeek != null && ProcessaEspera.processaEspera(esperandoPeek)) {
                        System.out.println("[Core " + finalCoreId + "] Processo " + esperandoPeek.getNome() + " finalizou sua espera no ciclo " + cicloAtual.get());
                        EscalonadorUtils.logProcessEvent("process_log.csv", esperandoPeek.getNome(), cicloAtual.get(), "WAIT_FINISHED");
                        Processo proc = esperando.poll();
                        if (proc != null) {
                            fila.add(proc);
                        }
                        esperandoPeek = esperando.peek();
                    }

                    // Try to get a new process if not currently executing one
                    if (processoAtual == null) {
                        Processo peek = fila.peek();
                        if (peek != null) {
                            switch (peek.getStatus()) {
                                case Pronto:
                                    processoAtual = fila.poll();
                                    processoAtual.setStatus(Status.Executando);
                                    System.out.println("[Core " + finalCoreId + "] Processo " + processoAtual.getNome() + " começou a ser executado no ciclo " + cicloAtual.get());
                                    EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual.get(), "STARTED");
                                    break;
                                case Esperando:
                                    esperando.add(fila.poll());
                                    break;
                                case Finalizado:
                                    finalizados.add(fila.poll());
                                    break;
                                default:
                                    fila.poll();
                                    break;
                            }
                        }
                    }

                    // Execute or move to waiting
                    if (processoAtual != null) {
                        if (processoAtual.getTipoEspera() == TipoEspera.Nenhum) {
                            processoAtual.atualizaTempoExecucao();
                        } else {
                            esperando.add(processoAtual);
                            System.out.println("[Core " + finalCoreId + "] Processo " + processoAtual.getNome() + " está esperando sua operação de "
                                    + processoAtual.getTipoEspera() + " no ciclo " + cicloAtual.get());
                            EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual.get(), "WAITING_" + processoAtual.getTipoEspera());
                            processoAtual = null;
                        }
                        if (processoAtual != null && processoAtual.getTempoRestante() <= 0) {
                            processoAtual.setStatus(Status.Finalizado);
                            finalizados.add(processoAtual);
                            System.out.println("[Core " + finalCoreId + "] Processo " + processoAtual.getNome() + " finalizou sua execução no ciclo " + cicloAtual.get());
                            EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual.get(), "FINISHED");
                            processoAtual = null;
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
        System.out.println("FIFO finalizado.");
    }
}

