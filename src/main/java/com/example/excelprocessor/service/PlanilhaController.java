package com.example.excelprocessor.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.excelprocessor.util.NomeUtils;

@Controller
public class PlanilhaController {

    private static final List<String> COLUNAS_A_REMOVER = Arrays.asList(
            "Nome de Usuário", "Faturáveis", "Planejado", "Obrigatório", "Progresso", "Período"
    );

    @GetMapping("/planilhas")
public String mostrarPlanilhas(Model model) throws IOException {

    // 1. Ler e processar Jira no sheet "Usuários"
    String jiraPath = "data_jira/Equipe_ SAU VSRF - Acompanhar Qualidade_2025-04-01_2025-04-30 (versao MVP estagio).xls"; // ajuste o caminho real
    Workbook jiraWorkbook = new XSSFWorkbook(new FileInputStream(jiraPath));
    Sheet jiraSheet = jiraWorkbook.getSheet("Usuários");
    if (jiraSheet == null) {
        jiraWorkbook.close();
        throw new RuntimeException("Sheet 'Usuários' não encontrado na planilha Jira");
    }

    Map<String, Integer> jiraHeaderMap = getHeaderMap(jiraSheet.getRow(0));
    Set<Integer> indicesParaRemover = new HashSet<>();
    for (String col : COLUNAS_A_REMOVER) {
        Integer idx = jiraHeaderMap.get(col);
        if (idx != null) indicesParaRemover.add(idx);
    }

    Integer jiraNomeIdx = jiraHeaderMap.get("Nome"); // Ajuste se o nome da coluna for diferente
    Integer jiraStatusIdx = jiraHeaderMap.get("STATUS");
    if (jiraNomeIdx == null || jiraStatusIdx == null) {
        jiraWorkbook.close();
        throw new RuntimeException("Colunas Nome ou STATUS não encontradas na Jira");
    }

    Set<String> nomesNormalizados = new HashSet<>();
    Set<String> nomesParciais = new HashSet<>();
    List<Map<String, String>> jiraDadosFiltrados = new ArrayList<>();

    for (int i = 1; i <= jiraSheet.getLastRowNum(); i++) {
        Row row = jiraSheet.getRow(i);
        if (row == null) continue;

        String status = getCellString(row.getCell(jiraStatusIdx));
        String nomeOriginal = getCellString(row.getCell(jiraNomeIdx));
        
        // Aplica normalização de nomes
        String nomeNormalizado = NomeUtils.prepararNomeParaComparacao(nomeOriginal);
        String nomeParcial = NomeUtils.extrairNomeParcial(nomeNormalizado);

        if ("ACTIVE".equalsIgnoreCase(status.trim())) {
            nomesNormalizados.add(nomeNormalizado);
            nomesParciais.add(nomeParcial);

            Map<String, String> linhaMap = new LinkedHashMap<>();
            for (Cell cell : jiraSheet.getRow(0)) {
                int idx = cell.getColumnIndex();
                if (indicesParaRemover.contains(idx)) continue;
                String header = cell.getStringCellValue();
                
                // Se for a coluna de nome, adicionamos também o nome normalizado
                if (idx == jiraNomeIdx) {
                    linhaMap.put(header, nomeOriginal);
                    linhaMap.put("Nome_Normalizado", nomeNormalizado);
                    linhaMap.put("Nome_Parcial", nomeParcial);
                } else {
                    linhaMap.put(header, getCellString(row.getCell(idx)));
                }
            }
            jiraDadosFiltrados.add(linhaMap);
        }
    }
    jiraWorkbook.close();

    // 2. Ler e filtrar Claim no sheet "ClaimBase"
    String claimPath = "/data_claim/Claim Bradesco - Abr25 v6.xlsx"; // ajuste o caminho real
    Workbook claimWorkbook = new XSSFWorkbook(new FileInputStream(claimPath));
    Sheet claimSheet = claimWorkbook.getSheet("ClaimBase");
    if (claimSheet == null) {
        claimWorkbook.close();
        throw new RuntimeException("Sheet 'ClaimBase' não encontrado na planilha Claim");
    }

    Map<String, Integer> claimHeaderMap = getHeaderMap(claimSheet.getRow(0));
    Integer claimNomeIdx = claimHeaderMap.get("Nome");
    Integer claimStatusIdx = claimHeaderMap.get("STATUS");

    if (claimNomeIdx == null) {
        claimWorkbook.close();
        throw new RuntimeException("Coluna Nome não encontrada na Claim");
    }

    List<Map<String, String>> claimDadosFiltrados = new ArrayList<>();
    for (int i = 1; i <= claimSheet.getLastRowNum(); i++) {
        Row row = claimSheet.getRow(i);
        if (row == null) continue;

        String nomeOriginal = getCellString(row.getCell(claimNomeIdx));
        
        // Aplica normalização de nomes
        String nomeNormalizado = NomeUtils.prepararNomeParaComparacao(nomeOriginal);
        String nomeParcial = NomeUtils.extrairNomeParcial(nomeNormalizado);
        
        // Verifica correspondência tanto pelo nome completo normalizado quanto pelo nome parcial
        boolean nomeEncontrado = nomesNormalizados.contains(nomeNormalizado) || 
                                nomesParciais.contains(nomeParcial);
                                
        if (!nomeEncontrado) continue;

        if (claimStatusIdx != null) {
            String statusClaim = getCellString(row.getCell(claimStatusIdx));
            if (!"ACTIVE".equalsIgnoreCase(statusClaim.trim())) continue;
        }

        Map<String, String> linhaMap = new LinkedHashMap<>();
        for (Cell cell : claimSheet.getRow(0)) {
            int idx = cell.getColumnIndex();
            String header = cell.getStringCellValue();
            
            // Se for a coluna de nome, adicionamos também o nome normalizado
            if (idx == claimNomeIdx) {
                linhaMap.put(header, nomeOriginal);
                linhaMap.put("Nome_Normalizado", nomeNormalizado);
                linhaMap.put("Nome_Parcial", nomeParcial);
            } else {
                linhaMap.put(header, getCellString(row.getCell(idx)));
            }
        }
        claimDadosFiltrados.add(linhaMap);
    }
    claimWorkbook.close();

    model.addAttribute("jiraDados", jiraDadosFiltrados);
    model.addAttribute("jiraHeaders", jiraDadosFiltrados.isEmpty() ? Collections.emptyList() : new ArrayList<>(jiraDadosFiltrados.get(0).keySet()));
    model.addAttribute("claimDados", claimDadosFiltrados);
    model.addAttribute("claimHeaders", claimDadosFiltrados.isEmpty() ? Collections.emptyList() : new ArrayList<>(claimDadosFiltrados.get(0).keySet()));

    return "planilhas";
}
    private Map<String, Integer> getHeaderMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        for (Cell cell : headerRow) {
            map.put(cell.getStringCellValue(), cell.getColumnIndex());
        }
        return map;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        if (null != cell.getCellType()) switch (cell.getCellType()) {
            case STRING -> {
                return cell.getStringCellValue();
            }
            case NUMERIC -> {
                return String.valueOf(cell.getNumericCellValue());
            }
            case BOOLEAN -> {
                return String.valueOf(cell.getBooleanCellValue());
            }
            case FORMULA -> {
                return cell.getCellFormula();
            }
            default -> {
            }
        }
        return "";
    }
}

