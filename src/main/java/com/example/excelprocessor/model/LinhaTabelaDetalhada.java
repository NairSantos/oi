package com.example.excelprocessor.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class LinhaTabelaDetalhada {
    private String tipo; // Claim ou Jira
    private String nome;
    private String projeto;
    private Map<String, String> horasPorData = new LinkedHashMap<>();

    // Getters e Setters
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getProjeto() { return projeto; }
    public void setProjeto(String projeto) { this.projeto = projeto; }

    public Map<String, String> getHorasPorData() { return horasPorData; }
    public void setHorasPorData(Map<String, String> horasPorData) { this.horasPorData = horasPorData; }
}
