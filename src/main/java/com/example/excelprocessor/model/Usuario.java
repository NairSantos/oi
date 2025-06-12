package com.example.excelprocessor.model;

import java.util.Map;
import com.example.excelprocessor.util.NomeUtils;

public class Usuario {
     private Map<String, String> dados;
    
    public Usuario(Map<String, String> dados) {
        this.dados = dados;
    }
    
    public Map<String, String> getDados() {
        return dados;
    }
    
    public void setDados(Map<String, String> dados) {
        this.dados = dados;
    }
    
    public String getNome() {
        // Tenta encontrar o nome pela coluna "Nome"
        if (dados.containsKey("Nome")) {
            return dados.get("Nome");
        }
        
        // Caso não exista coluna específica, procura por colunas que contenham "nome"
        for (Map.Entry<String, String> entry : dados.entrySet()) {
            if (entry.getKey().toLowerCase().contains("nome")) {
                return entry.getValue();
            }
        }
        
        // Se não encontrar nada, retorna vazio
        return "";
    }
    
    /**
     * Retorna o nome normalizado para comparações
     */
    public String getNomeNormalizado() {
        // Primeiro tenta pela coluna explícita que adicionamos
        if (dados.containsKey("Nome_Normalizado")) {
            return dados.get("Nome_Normalizado");
        }
        
        // Se não existir, normaliza o nome obtido
        return NomeUtils.prepararNomeParaComparacao(getNome());
    }
    
    /**
     * Retorna apenas os dois primeiros nomes normalizados
     */
    public String getNomeParcial() {
        return NomeUtils.extrairNomeParcial(getNomeNormalizado());
    }
}