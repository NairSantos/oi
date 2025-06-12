package com.example.excelprocessor.model;

import java.util.Map;
import com.example.excelprocessor.util.NomeUtils;

public class JiraItem {
  private Map<String, String> dados;
    
    public JiraItem(Map<String, String> dados) {
        this.dados = dados;
    }
    
    public Map<String, String> getDados() {
        return dados;
    }
    
    public void setDados(Map<String, String> dados) {
        this.dados = dados;
    }
    
    public String getStatus() {
        return dados.getOrDefault("STATUS", "");
    }
    
    public String getNome() {
        // Primeiro tenta pela coluna "Nome" exata
        if (dados.containsKey("Nome")) {
            return dados.get("Nome");
        }
        
        // Procura por colunas que possam conter o nome
        for (String key : dados.keySet()) {
            if (key.toLowerCase().contains("nome") || 
                key.toLowerCase().contains("name") ||
                key.toLowerCase().contains("user")) {
                return dados.get(key);
            }
        }
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