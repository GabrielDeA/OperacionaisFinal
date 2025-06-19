import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.function.Supplier;

public class Main {
    private static volatile boolean shouldStop = false;

    public static void main(String[] args) throws IOException {
        new java.io.FileWriter("process_log.csv", false).close();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Scheduler Simulator");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            JTextArea consoleArea = new JTextArea();
            consoleArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(consoleArea);

            // Redirect System.out to JTextArea
            PrintStream printStream = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    SwingUtilities.invokeLater(() -> consoleArea.append(String.valueOf((char) b)));
                }
            }, true);
            System.setOut(printStream);
            System.setErr(printStream);

            String[] options = {"FIFO", "SJF", "Round Robin", "Filas Realimentacao", "FIFO com 1 thread", "FIFO com 3 threads", "FIFO com 4 threads", "FIFO com 5 threads", "round robin com 3 cores"};
            JComboBox<String> comboBox = new JComboBox<>(options);
            JButton runButton = new JButton("Run");
            JButton stopButton = new JButton("Stop");
            stopButton.setEnabled(false);

            JPanel topPanel = new JPanel();
            topPanel.add(new JLabel("Choose scheduling algorithm:"));
            topPanel.add(comboBox);
            topPanel.add(runButton);
            topPanel.add(stopButton);

            frame.getContentPane().add(topPanel, BorderLayout.NORTH);
            frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

            runButton.addActionListener(e -> {
                runButton.setEnabled(false);
                stopButton.setEnabled(true);
                shouldStop = false;
                new Thread(() -> {
                    try {
                        new java.io.FileWriter("process_log.csv", false).close();
                        int contadorId = 0;
                        Processo processo1 = new Processo("P1", contadorId++, 2, 1, TipoEspera.IO, 3);
                        Processo processo2 = new Processo("P2", contadorId++, 5, 2, TipoEspera.Nenhum, 0);
                        Processo processo3 = new Processo("P3", contadorId++, 8, 3, TipoEspera.Memoria, 2);
                        Processo processo4 = new Processo("P4", contadorId++, 6, 4, TipoEspera.IO, 1);
                        Processo processo5 = new Processo("P5", contadorId++, 12, 5, TipoEspera.Memoria, 4);
                        Processo processo6 = new Processo("P6", contadorId++, 12, 5, TipoEspera.Memoria, 4, true);

                        Queue<Processo> processos = new LinkedList<>();
                        processos.add(processo1);
                        processos.add(processo2);
                        processos.add(processo3);
                        processos.add(processo4);
                        processos.add(processo5);
                        processos.add(processo6);

                        int choice = comboBox.getSelectedIndex();
                        switch (choice) {
                            case 0 -> FIFO.executarFIFO(new LinkedList<>(processos), 1000);
                            case 1 -> SJF.executarSJF(new ArrayList<>(processos));
                            case 2 -> RoundRobin.executarRoundRobin(new ArrayList<>(processos), 6);
                            case 3 -> FilasRealimentacao.executarFilasRealimentacao(new LinkedList<>(processos), 1000);
                            case 4 -> FIFO.executarFIFOComCores(new ArrayList<>(processos), 1, () -> shouldStop);
                            case 5 -> FIFO.executarFIFOComCores(new ArrayList<>(processos), 3, () -> shouldStop);
                            case 6 -> FIFO.executarFIFOComCores(new ArrayList<>(processos), 4, () -> shouldStop);
                            case 7 -> FIFO.executarFIFOComCores(new ArrayList<>(processos), 5, () -> shouldStop);
                            case 8 -> RoundRobin.executarRoundRobinComCores(new ArrayList<>(processos), 5, 3);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            runButton.setEnabled(true);
                            stopButton.setEnabled(false);
                        });
                    }
                }).start();
            });

            stopButton.addActionListener(e -> shouldStop = true);

            frame.setVisible(true);
        });
    }
}