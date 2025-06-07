public class ProcessaEspera {
    public static boolean processaEspera(Processo processo) {
        boolean finalizou = false;
        if(processo == null) {
            return finalizou;
        }
        if(processo.tipoEspera == TipoEspera.Processo_Filho) {
            //TODO
        }
        else {
            processo.atualizaTempoEspera();
        }

        if(processo.tempoEspera<= 0) {
            processo.setStatus(Status.Pronto);
            processo.setTipoEspera(TipoEspera.Nenhum);
            finalizou = true;
        }
        return finalizou;
    }

}
