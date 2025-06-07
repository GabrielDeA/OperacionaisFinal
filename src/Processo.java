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

    public String nome;
    public int id;
    public int paiId = -1; // -1 indica que o processo não tem pai
    private List<Processo> filhos = new ArrayList<>();
    public int tempoParaExecucao;
    public int tempoRestante;
    public int quantum; 
    public TipoEspera tipoEspera;
    public int tempoEspera = 0;
    public Status status;


    public Processo(String nome, int id, int tempoParaExecucao, int quantum, TipoEspera tipo, int tempoEspera) {
        this.nome = nome;
        this.id = id;
        this.tempoParaExecucao = tempoParaExecucao;
        this.tempoRestante = tempoParaExecucao;
        this.quantum = quantum;
        this.tipoEspera = tipo;
        this.tempoEspera = tempoEspera;
        this.status = status.Pronto;
    }

    public void addFilho(Processo filho) {
        filho.setPaiId(this.id);
        filhos.add(filho);
    }

    public boolean hasFilhosInacabados() {
        for (Processo filho : filhos) {
            if (filho.getStatus() != Status.Finalizado) {
                return true;
            }
        }
        return false;
    }

    public void esperaPrFilhos() {
        if(hasFilhosInacabados()) {
            this.tipoEspera = TipoEspera.Processo_Filho;
            this.status = Status.Esperando;
        }
    }

    public void atualizaTempoEspera(int tempo) {
        //switch()
    }

    public void atualizaTempoExecucao() {
        this.tempoEspera = this.tempoEspera--;
    }






    //#region Getters e Setters
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

    public void setTipoEspera(TipoEspera tipo) {
        this.tipoEspera = tipo;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
    //#endregion getters e setters
}
