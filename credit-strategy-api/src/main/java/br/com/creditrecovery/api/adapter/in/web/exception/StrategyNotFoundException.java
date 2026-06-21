package br.com.creditrecovery.api.adapter.in.web.exception;

public class StrategyNotFoundException extends RuntimeException {

    public StrategyNotFoundException(String customerHash) {
        super("Estrategia de recuperacao nao encontrada para customerHash=" + customerHash);
    }
}
