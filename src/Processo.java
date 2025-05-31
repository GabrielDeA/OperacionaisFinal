public class Processo {
    public String nome;
    public int id;
    public int prioridade;
    public int tempoChegada;
    public int quantum; 
    public int tempoParaExecucao;
    public Boolean executado;

    public Processo(String nome, int id, int prioridade, int tempoChegada, int tempoParaExecucao, int quantum) {
        this.nome = nome;
        this.id = id;
        this.prioridade = prioridade;
        this.tempoChegada = tempoChegada;
        this.quantum = quantum;
        this.tempoParaExecucao = tempoParaExecucao;
        this.executado = false;
    }

}
