public class ProcessaEspera {

    public static boolean processaEspera(Processo processo) {
        boolean finalizou = false;
        if (processo == null) {
            return finalizou;
        }
        if (processo.getTipoEspera() == TipoEspera.Processo_Filho) {
            //TODO
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

}
