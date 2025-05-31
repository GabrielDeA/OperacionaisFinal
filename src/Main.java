//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Processo processo1 = new Processo("Processo1",1, 1, 0, 5, 5);
        Processo processo2 = new Processo("Processo2",2, 2, 1, 7, 3);
        Processo processo3 = new Processo("Processo3",3, 3, 2,4, 4);
        Processo processo4 = new Processo("Processo4",4, 4, 3,1, 2);
        Processo processo5 = new Processo("Processo5",5, 5, 4,9, 1);

        List<Processo> processos = new java.util.ArrayList<>();
        processos.add(processo1);
        processos.add(processo2);
        processos.add(processo3);
        processos.add(processo4);
        processos.add(processo5);
       // FIFO.executarFIFO(processos);
        SJF.executarSJF(processos);
        }
    }
