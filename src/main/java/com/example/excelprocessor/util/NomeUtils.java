package com.example.excelprocessor.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Classe utilitária para manipulação de nomes
 */
public class NomeUtils {
    
    /**
     * Remove acentos, converte para maiúsculo e remove espaços extras
     * @param nome O nome a ser normalizado
     * @return Nome normalizado sem acentos e em maiúsculo
     */
    public static String normalizarNome(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "";
        }
        
        // Remove acentos
        String normalizado = Normalizer.normalize(nome, Normalizer.Form.NFKD);
        // Remove caracteres não-ASCII
        normalizado = normalizado.replaceAll("[^\\p{ASCII}]", "");
        // Converte para maiúsculo e remove espaços extras
        return normalizado.toUpperCase().trim();
    }
    
    /**
     * Extrai os dois primeiros nomes do nome completo
     * @param nome O nome completo
     * @return Os dois primeiros nomes concatenados
     */
    public static String extrairNomeParcial(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "";
        }
        
        String[] partes = nome.trim().split("\\s+");
        if (partes.length >= 2) {
            return partes[0] + " " + partes[1];
        }
        return partes.length > 0 ? partes[0] : "";
    }
    
    /**
     * Remove qualquer informação após "/"
     * @param nome O nome a ser limpo
     * @return Nome limpo sem informações após "/"
     */
    public static String limparNomePlanilha(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "";
        }
        
        String[] partes = nome.split("/");
        return partes[0].trim();
    }
    
    /**
     * Aplica todas as normalizações necessárias para comparação de nomes
     * @param nome O nome original
     * @return Nome normalizado para comparação
     */
    public static String prepararNomeParaComparacao(String nome) {
        if (nome == null || nome.isEmpty()) {
            return "";
        }
        
        // Primeiro limpa qualquer coisa após /
        String nomeLimpo = limparNomePlanilha(nome);
        // Depois normaliza (remove acentos, deixa maiúsculo)
        String nomeNormalizado = normalizarNome(nomeLimpo);
        
        return nomeNormalizado;
    }
}