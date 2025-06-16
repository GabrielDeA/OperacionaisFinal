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
    private int fila;
    private boolean isDaemon;

    public boolean isDaemon() {
        return isDaemon;
    }

    public void setDaemon(boolean daemon) {
        isDaemon = daemon;
    }

    public Processo(String nome, int id, int tempoParaExecucao, int quantum, TipoEspera tipo, int tempoEspera) {
        this.nome = nome;
        this.id = id;
        this.tempoParaExecucao = tempoParaExecucao;
        this.tempoRestante = tempoParaExecucao;
        this.quantum = quantum;
        this.tipoEspera = tipo;
        this.tempoEspera = tempoEspera;
        this.status = Status.Pronto;
        this.isDaemon = false;
    }

    public Processo(String nome, int id, int tempoParaExecucao, int quantum, TipoEspera tipo, int tempoEspera, boolean isDaemon) {
        this.nome = nome;
        this.id = id;
        this.tempoParaExecucao = tempoParaExecucao;
        this.tempoRestante = tempoParaExecucao;
        this.quantum = quantum;
        this.tipoEspera = tipo;
        this.tempoEspera = tempoEspera;
        this.status = Status.Pronto;
        this.isDaemon = isDaemon;
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

    public void atualizaQuantum() {
        this.quantum--;
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


    //#region getters e setters

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPaiId() {
        return paiId;
    }

    public void setPaiId(int paiId) {
        this.paiId = paiId;
    }

    public List<Processo> getFilhos() {
        return filhos;
    }

    public void setFilhos(List<Processo> filhos) {
        this.filhos = filhos;
    }

    public int getTempoParaExecucao() {
        return tempoParaExecucao;
    }

    public void setTempoParaExecucao(int tempoParaExecucao) {
        this.tempoParaExecucao = tempoParaExecucao;
    }

    public int getTempoRestante() {
        return tempoRestante;
    }

    public void setTempoRestante(int tempoRestante) {
        this.tempoRestante = tempoRestante;
    }

    public int getQuantum() {
        return quantum;
    }

    public void setQuantum(int quantum) {
        this.quantum = quantum;
    }

    public TipoEspera getTipoEspera() {
        return tipoEspera;
    }

    public void setTipoEspera(TipoEspera tipoEspera) {
        this.tipoEspera = tipoEspera;
    }

    public int getTempoEspera() {
        return tempoEspera;
    }

    public void setTempoEspera(int tempoEspera) {
        this.tempoEspera = tempoEspera;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getFila() {
        return fila;
    }

    public void setFila(int filaOrigem) {
        this.fila = filaOrigem;
    }

}
