import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

enum TipoEspera {
    Nenhum,
    IO,
    Processo_Filho,
    Memoria
}

enum Status {
    Pronto,
    Executando,
    Esperando,
    Finalizado
}

@Getter
@Setter
public class Processo {
    private String nome;
    private int id;
    private int paiId = -1; // -1 indica que o processo não tem pai
    private List<Processo> filhos = new ArrayList<>();
    private int tempoParaExecucao;
    private int tempoRestante;
    private int quantum;
    private TipoEspera tipoEspera;
    private int tempoEspera = 0;
    private Status status;

    public Processo(String nome, int id, int tempoParaExecucao, int quantum, TipoEspera tipo, int tempoEspera) {
        this.nome = nome;
        this.id = id;
        this.tempoParaExecucao = tempoParaExecucao;
        this.tempoRestante = tempoParaExecucao;
        this.quantum = quantum;
        this.tipoEspera = tipo;
        this.tempoEspera = tempoEspera;
        this.status = Status.Pronto;
    }

    public void addFilho(Processo filho) {
        filho.setPaiId(this.id);
        filhos.add(filho);
    }

    public boolean hasFilhosInacabados() {
        return filhos.stream().anyMatch(filho -> filho.getStatus() != Status.Finalizado);
    }

    public void esperaPrFilhos() {
        if (hasFilhosInacabados()) {
            this.tipoEspera = TipoEspera.Processo_Filho;
            this.status = Status.Esperando;
        }
    }

    public void atualizaTempoEspera() {
        this.tempoEspera--;
    }

    public void atualizaTempoExecucao() {
        this.tempoRestante--;
    }

    public boolean processarIO() {
        System.out.println("[" + nome + "] Iniciando operação de IO...");

        // Simula leitura de dados: criar e percorrer um buffer
        byte[] buffer = new byte[1024 * 50]; // 50 KB
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = (byte) (i % 256);
        }
        this.setStatus(Status.Pronto);
        this.setTipoEspera(TipoEspera.Nenhum);
        return true;
    }


    public boolean processarMemoria() {
        System.out.println("[" + nome + "] Iniciando operação de uso de memória...");

        // Simula uma carga de memória
        int[] memoria = new int[100_000];
        long soma = 0;
        for (int i = 0; i < memoria.length; i++) {
            memoria[i] = i;
            soma += memoria[i]; // Simula uso de CPU junto
        }

        this.setStatus(Status.Pronto);
        this.setTipoEspera(TipoEspera.Nenhum);
        return true;
    }

}
