import java.util.*;

public class SJF {
    public static void executarSJF(List<Processo> processos) {
        System.out.println("Iniciando execucao dos processos em SJF:");
        int cicloAtual = 0;
        List<Processo> esperando = new ArrayList<>();
        List<Processo> finalizados = new ArrayList<>();
        int totalProcessos = processos.size();

        while (finalizados.size() < totalProcessos) {
            // Move processes to waiting or finalized lists
            Iterator<Processo> it = processos.iterator();
            while (it.hasNext()) {
                Processo p = it.next();
                if (p.getStatus() == Status.Esperando) {
                    esperando.add(p);
                    it.remove();
                } else if (p.getStatus() == Status.Finalizado) {
                    finalizados.add(p);
                    it.remove();
                }
            }

            // Process waiting queue
            Iterator<Processo> itEspera = esperando.iterator();
            while (itEspera.hasNext()) {
                Processo p = itEspera.next();
                if (ProcessaEspera.processaEspera(p)) {
                    p.setStatus(Status.Pronto);
                    processos.add(p);
                    itEspera.remove();
                    EscalonadorUtils.logProcessEvent("process_log.csv", p.getNome(), cicloAtual, "WAIT_FINISHED");
                }
            }

            // Select the next process to execute
            Processo next = processos.stream()
                    .filter(p -> p.getStatus() == Status.Pronto)
                    .min(Comparator.comparingInt(Processo::getTempoRestante))
                    .orElse(null);

            if (next != null) {
                next.setStatus(Status.Executando);
                System.out.println("Processo " + next.getNome() + " comecou a ser executado no ciclo " + cicloAtual);
                EscalonadorUtils.logProcessEvent("process_log.csv", next.getNome(), cicloAtual, "STARTED");

                if (next.getTipoEspera() == TipoEspera.Nenhum) {
                    next.atualizaTempoExecucao();
                } else {
                    next.setStatus(Status.Esperando);
                    esperando.add(next);
                    processos.remove(next);
                    System.out.println("Processo " + next.getNome() + " está esperando sua operacao de " + next.getTipoEspera() + " no ciclo " + cicloAtual);
                    EscalonadorUtils.logProcessEvent("process_log.csv", next.getNome(), cicloAtual, "WAITING_" + next.getTipoEspera());
                    cicloAtual++;
                    continue;
                }

                if (next.getTempoRestante() <= 0) {
                    next.setStatus(Status.Finalizado);
                    finalizados.add(next);
                    processos.remove(next);
                    System.out.println("Processo " + next.getNome() + " finalizou sua execucao no ciclo " + cicloAtual);
                    EscalonadorUtils.logProcessEvent("process_log.csv", next.getNome(), cicloAtual, "FINISHED");
                } else {
                    next.setStatus(Status.Pronto);
                }
            }
            cicloAtual++;
        }
        System.out.println("Todos os processos foram executados em SJF, tempo final: " + cicloAtual);
        EscalonadorUtils.logProcessEvent("process_log.csv", "todos", cicloAtual, "ALL_FINISHED");
    }
}