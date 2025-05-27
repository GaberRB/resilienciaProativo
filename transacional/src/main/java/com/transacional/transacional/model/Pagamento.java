package com.transacional.transacional.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Pagamento {

    private UUID id;
    private Double valor;
    private String documentoPagador;
    private String documentoRecebedor;
    private String status;
    private LocalDateTime dataPagamento;

    public Pagamento() {
    }

    public Pagamento(UUID id, Double valor, String documentoPagador, String documentoRecebedor, String status, LocalDateTime dataPagamento) {
        this.id = id;
        this.valor = valor;
        this.documentoPagador = documentoPagador;
        this.documentoRecebedor = documentoRecebedor;
        this.status = status;
        this.dataPagamento = dataPagamento;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDateTime dataPagamento) {
        this.dataPagamento = dataPagamento;
    }

    @Override
    public String toString() {
        return "Pagamento{" +
                "id=" + id +
                ", valor=" + valor +
                ", documentoPagador='" + documentoPagador + '\'' +
                ", documentoRecebedor='" + documentoRecebedor + '\'' +
                ", status='" + status + '\'' +
                ", dataPagamento=" + dataPagamento +
                '}';
    }
}
