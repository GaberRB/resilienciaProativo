package com.intermitente.intermitente.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PagamentoRequest {

    private Double valor;
    @JsonProperty("pagador")
    private String documentoPagador;
    @JsonProperty("beneficiario")
    private String documentoRecebedor;

    public PagamentoRequest(Double valor, String documentoPagador, String documentoRecebedor) {
        this.valor = valor;
        this.documentoPagador = documentoPagador;
        this.documentoRecebedor = documentoRecebedor;

    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }

    public String getDocumentoPagador() {
        return documentoPagador;
    }

    public void setDocumentoPagador(String documentoPagador) {
        this.documentoPagador = documentoPagador;
    }

    public String getDocumentoRecebedor() {
        return documentoRecebedor;
    }

    public void setDocumentoRecebedor(String documentoRecebedor) {
        this.documentoRecebedor = documentoRecebedor;
    }

}
