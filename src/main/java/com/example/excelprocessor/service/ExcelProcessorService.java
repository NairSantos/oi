package com.example.excelprocessor.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.excelprocessor.model.ComparativoHoras;
import com.example.excelprocessor.model.JiraItem;
import com.example.excelprocessor.model.Usuario;
import com.example.excelprocessor.util.NomeUtils;


@Service
public class ExcelProcessorService {
    
     private final Set<String> COLUNAS_REMOVER_PLANILHA1 = Set.of(
        "Nome de Usuário", "Faturáveis", "Planejado", 
        "Obrigatório", "Progresso", "Período"
    );
    
        public List<Usuario> processarPlanilhaJira(MultipartFile file) throws IOException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Usuários");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'Usuários' não encontrada na planilha JIRA");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return usuarios;

            Map<Integer, String> indicesCabecalho = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerValue = getCellValueAsString(cell);
                if (!COLUNAS_REMOVER_PLANILHA1.contains(headerValue)) {
                    indicesCabecalho.put(cell.getColumnIndex(), headerValue);
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Map<String, String> dadosUsuario = new LinkedHashMap<>();

                for (Map.Entry<Integer, String> entry : indicesCabecalho.entrySet()) {
                    Cell cell = row.getCell(entry.getKey());
                    String valor = getCellValueAsString(cell);

                    if (entry.getValue().equals("Nome") || entry.getValue().toLowerCase().contains("nome")) {
                        String nomeOriginal = valor;
                        String nomeNormalizado = NomeUtils.prepararNomeParaComparacao(valor);
                        dadosUsuario.put(entry.getValue(), nomeOriginal);
                        dadosUsuario.put("Nome_Normalizado", nomeNormalizado);
                    } else {
                        dadosUsuario.put(entry.getValue(), valor);
                    }
                }

                if (!dadosUsuario.isEmpty()) {
                    usuarios.add(new Usuario(dadosUsuario));
                }
            }
        }

        return usuarios;
    }

    public List<JiraItem> processarPlanilhaClaim(MultipartFile file, List<Usuario> usuariosJira) throws IOException {
        List<JiraItem> claimItems = new ArrayList<>();

        Set<String> nomesUsuariosNormalizados = usuariosJira.stream()
                .map(usuario -> NomeUtils.prepararNomeParaComparacao(usuario.getNome()))
                .filter(nome -> !nome.isEmpty())
                .collect(Collectors.toSet());

        Set<String> nomesUsuariosParciais = usuariosJira.stream()
                .map(usuario -> NomeUtils.extrairNomeParcial(
                        NomeUtils.prepararNomeParaComparacao(usuario.getNome())))
                .filter(nome -> !nome.isEmpty())
                .collect(Collectors.toSet());

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("ClaimBase");
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet 'ClaimBase' não encontrada na planilha CLAIM");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return claimItems;

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }

            int nomeIdx = -1;
            int statusIdx = -1;

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i).toLowerCase();
                if (header.contains("nome") || header.contains("name") || header.contains("usuário") || header.contains("user")) {
                    nomeIdx = i;
                    break;
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                String header = headers.get(i).toLowerCase();
                if (header.contains("status") || header.contains("situação") || header.contains("situacao")) {
                    statusIdx = i;
                    break;
                }
            }

            if (nomeIdx == -1 && !headers.isEmpty()) {
                nomeIdx = 0;
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nomeOriginal = getCellValueAsString(row.getCell(nomeIdx));
                String nome = NomeUtils.prepararNomeParaComparacao(nomeOriginal);
                String nomeParcial = NomeUtils.extrairNomeParcial(nome);

                Map<String, String> dadosClaim = new LinkedHashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    if (j < headers.size()) {
                        String valor = getCellValueAsString(row.getCell(j));
                        dadosClaim.put(headers.get(j), valor);
                    }
                }

                dadosClaim.put("Nome_Normalizado", nome);

                boolean incluir = true;

                if (!nome.trim().isEmpty()) {
                    boolean nomeEncontrado = nomesUsuariosNormalizados.contains(nome) ||
                            nomesUsuariosParciais.contains(nomeParcial);

                    if (!nomeEncontrado) {
                        incluir = false;
                    }
                }

                if (statusIdx != -1) {
                    String status = getCellValueAsString(row.getCell(statusIdx)).trim();
                    if (!status.equalsIgnoreCase("ACTIVE")) {
                        incluir = false;
                    }
                }

                if (incluir) {
                    claimItems.add(new JiraItem(dadosClaim));
                }
            }
        }

        return claimItems;
    }

    public List<ComparativoHoras> gerarResumoComparativo(List<Usuario> usuarios, List<JiraItem> claimItems) {
        Map<String, Double> mapaJira = new HashMap<>();
        Map<String, String> nomeExibicao = new HashMap<>();

        for (Usuario usuario : usuarios) {
            String chave = usuario.getNomeParcial();
            String nome = usuario.getNome();
            String valorHorasStr = usuario.getDados().getOrDefault("Trabalhou", "0").replace(",", ".");
            double horas = valorHorasStr.isEmpty() ? 0 : Double.parseDouble(valorHorasStr);
            mapaJira.merge(chave, horas, Double::sum);
            nomeExibicao.put(chave, nome);
        }

        Map<String, Double> mapaClaim = new HashMap<>();
        for (JiraItem item : claimItems) {
            String chave = item.getNomeParcial();
            String valorHorasStr = item.getDados().getOrDefault("TOTAL_HRS_EXPENDED", "0").replace(",", ".");
            double horas = valorHorasStr.isEmpty() ? 0 : Double.parseDouble(valorHorasStr);
            mapaClaim.merge(chave, horas, Double::sum);
        }

        Set<String> todos = new HashSet<>();
        todos.addAll(mapaJira.keySet());
        todos.addAll(mapaClaim.keySet());

        List<ComparativoHoras> lista = new ArrayList<>();
        for (String chave : todos) {
            double jira = mapaJira.getOrDefault(chave, 0.0);
            double claim = mapaClaim.getOrDefault(chave, 0.0);
            String nome = nomeExibicao.getOrDefault(chave, chave);
            lista.add(new ComparativoHoras(nome, chave, jira, claim));
        }

        return lista;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double valor = cell.getNumericCellValue();
                    if (valor == Math.floor(valor)) {
                        return String.valueOf((long) valor);
                    } else {
                        return String.valueOf(valor);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue cellValue = evaluator.evaluate(cell);

                    switch (cellValue.getCellType()) {
                        case STRING:
                            return cellValue.getStringValue().trim();
                        case NUMERIC:
                            double numValue = cellValue.getNumberValue();
                            if (numValue == Math.floor(numValue)) {
                                return String.valueOf((long) numValue);
                            } else {
                                return String.valueOf(numValue);
                            }
                        case BOOLEAN:
                            return String.valueOf(cellValue.getBooleanValue());
                        default:
                            return cell.getCellFormula();
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
                return "";
            case ERROR:
                return "#ERROR";
            default:
                return "";
        }
    }
}