public class ProcessaEspera {

    public static boolean processaEspera(Processo processo) {
        boolean finalizou = false;
        if (processo == null) {
            return finalizou;
        }
        if (processo.getTipoEspera() == TipoEspera.Processo_Filho) {
        } else {
            processo.atualizaTempoEspera();
        }

        if (processo.getTempoEspera() <= 0) {

            switch (processo.getTipoEspera()) {
                case IO:
                    finalizou = processo.processarIO();
                    break;
                case Memoria:
                    finalizou = processo.processarMemoria();
                    break;
                default:
                    processo.setStatus(Status.Pronto);
                    processo.setTipoEspera(TipoEspera.Nenhum);
                    finalizou = true;
                    break;
            }
        }
        return finalizou;
    }

    public static boolean processaEsperaThread(Processo processo, int cicloAtual, String coreLabel) {
        boolean finalizou = false;
        if (processo == null) {
            return finalizou;
        }

        EscalonadorUtils.logProcessEvent("process_log.csv", processo.getNome(), cicloAtual, "WAITING_" + processo.getTipoEspera() + (coreLabel != null ? ("," + coreLabel) : ""));

        if (processo.getTipoEspera() == TipoEspera.Processo_Filho) {
        } else {
            processo.atualizaTempoEspera();
        }

        if (processo.getTempoEspera() <= 0) {
            switch (processo.getTipoEspera()) {
                case IO:
                    finalizou = processo.processarIO();
                    EscalonadorUtils.logProcessEvent("process_log.csv", processo.getNome(), cicloAtual, "WAIT_FINISHED_IO" + (coreLabel != null ? ("," + coreLabel) : ""));
                    break;
                case Memoria:
                    finalizou = processo.processarMemoria();
                    EscalonadorUtils.logProcessEvent("process_log.csv", processo.getNome(), cicloAtual, "WAIT_FINISHED_Memoria" + (coreLabel != null ? ("," + coreLabel) : ""));
                    break;
                case Processo_Filho:
                    break;
                default:
                    processo.setStatus(Status.Pronto);
                    processo.setTipoEspera(TipoEspera.Nenhum);
                    finalizou = true;
                    EscalonadorUtils.logProcessEvent("process_log.csv", processo.getNome(), cicloAtual, "WAIT_FINISHED" + (coreLabel != null ? ("," + coreLabel) : ""));
                    break;
            }
        }
        return finalizou;
    }

}
