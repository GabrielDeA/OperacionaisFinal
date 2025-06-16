import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
        List<Processo> daemonProcesses = processos.stream()
                .filter(Processo::isDaemon)
                .toList();

        if (!daemonProcesses.isEmpty()) {
            for (Processo daemon : daemonProcesses) {
                processos.remove(daemon);
                fila.remove(daemon);
                numCores--;

                Thread daemonThread = new Thread(() -> {
                    int ciclo = 0;
                    while (true) {
                        System.out.println("[Daemon Core] Processo " + daemon.getNome() + " está rodando como daemon no ciclo " + ciclo);
                        EscalonadorUtils.logProcessEvent("process_log.csv", daemon.getNome(), ciclo, "DAEMON_RUNNING");
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            break;
                        }
                        ciclo++;
                    }
                });
                daemonThread.setDaemon(true);
                daemonThread.start();
            }
        }

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
                    // Exit condition: all processes finished and nothing left to do
                    if (finalizados.size() >= totalProcessos &&
                            fila.isEmpty() && esperando.isEmpty() && processoAtual == null) break;

                    // Process all waiting processes
                    synchronized (esperando) {
                        for (Processo p : esperando.toArray(new Processo[0])) {
                            if (ProcessaEspera.processaEsperaThread(p, cicloAtual.get(), "[Core " + finalCoreId + "]")) {
                                esperando.remove(p);
                                System.out.println("[Core " + finalCoreId + "] Processo " + p.getNome() + " finalizou sua espera no ciclo " + cicloAtual.get());
                                EscalonadorUtils.logProcessEvent("process_log.csv", p.getNome(), cicloAtual.get(), "WAIT_FINISHED");
                                fila.add(p);
                            }
                        }
                    }

                    // Try to get a new process if not currently executing one
                    if (processoAtual == null) {
                        Processo proc = fila.poll();
                        if (proc != null) {
                            switch (proc.getStatus()) {
                                case Pronto:
                                    processoAtual = proc;
                                    processoAtual.setStatus(Status.Executando);
                                    System.out.println("[Core " + finalCoreId + "] Processo " + processoAtual.getNome() + " começou a ser executado no ciclo " + cicloAtual.get());
                                    EscalonadorUtils.logProcessEvent("process_log.csv", processoAtual.getNome(), cicloAtual.get(), "STARTED");
                                    break;
                                case Esperando:
                                    esperando.add(proc);
                                    break;
                                case Finalizado:
                                    finalizados.add(proc);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

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

