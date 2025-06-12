import java.util.*;

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
}
