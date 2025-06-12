package com.example.excelprocessor.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.example.excelprocessor.model.LinhaTabelaDetalhada;
import com.example.excelprocessor.util.NomeUtils;

@Service
public class TabelaComparativaService {

    public List<LinhaTabelaDetalhada> gerarTabelaComparativa() throws Exception {
        Map<String, LinhaTabelaDetalhada> linhasClaim = processarClaim();
        Map<String, LinhaTabelaDetalhada> linhasJira = processarJira();

        // Junta as chaves (nomes únicos)
        Set<String> nomes = new HashSet<>();
        nomes.addAll(linhasClaim.keySet());
        nomes.addAll(linhasJira.keySet());

        List<LinhaTabelaDetalhada> resultado = new ArrayList<>();

        for (String nome : nomes) {
            LinhaTabelaDetalhada claim = linhasClaim.get(nome);
            LinhaTabelaDetalhada jira = linhasJira.get(nome);

            if (claim != null) resultado.add(claim);
            if (jira != null) resultado.add(jira);

            if (claim != null && jira != null) {
                for (String data : claim.getHorasPorData().keySet()) {
                    String vClaim = claim.getHorasPorData().getOrDefault(data, "0");
                    String vJira = jira.getHorasPorData().getOrDefault(data, "0");

                    if (!vClaim.equals(vJira)) {
                        claim.getHorasPorData().put(data, wrapDiferenca(vClaim));
                        jira.getHorasPorData().put(data, wrapDiferenca(vJira));
                    }
                }
            }
        }

        return resultado;
    }

    private String wrapDiferenca(String valor) {
        return "<span style='background-color:red;color:white'>" + valor + "</span>";
    }

    private Map<String, LinhaTabelaDetalhada> processarClaim() throws Exception {
        Map<String, LinhaTabelaDetalhada> mapa = new HashMap<>();
        File pasta = new File("src/main/resources/static/data_claim");
        File[] arquivos = pasta.listFiles((dir, name) -> name.endsWith(".xls") || name.endsWith(".xlsx"));

        if (arquivos == null || arquivos.length == 0) throw new RuntimeException("Arquivo CLAIM não encontrado");

        FileInputStream fis = new FileInputStream(arquivos[0]);
        Workbook workbook = new XSSFWorkbook(fis);
        Sheet sheet = workbook.getSheet("ClaimBase");

        int rowCount = sheet.getPhysicalNumberOfRows();
        Map<String, Integer> colunas = getCabecalhos(sheet.getRow(0));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        Map<String, Integer> diasMap = Map.of(
                "SAT_HOURS_EXPENDED", -6,
                "SUN_HOURS_EXPENDED", -5,
                "MON_HOURS_EXPENDED", -4,
                "TUE_HOURS_EXPENDED", -3,
                "WED_HOURS_EXPENDED", -2,
                "THU_HOURS_EXPENDED", -1,
                "FRI_HOURS_EXPENDED", 0
        );

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String status = getString(row, colunas.get("LAB_STATUS")).trim();
            if (!status.equals("ACTIVE")) continue;

            String nome = NomeUtils.normalizarNome(getString(row, colunas.get("NOME")));
            String projeto = getString(row, colunas.get("ACCOUNT_ID"));
            LocalDate sexta = row.getCell(colunas.get("WEEK_ENDING_DATE")).getLocalDateTimeCellValue().toLocalDate();

            LinhaTabelaDetalhada linha = mapa.getOrDefault(nome, new LinhaTabelaDetalhada());
            linha.setTipo("Claim");
            linha.setNome(nome);
            linha.setProjeto(projeto);

            for (String col : diasMap.keySet()) {
                double horas = row.getCell(colunas.get(col)).getNumericCellValue();
                LocalDate dia = sexta.plusDays(diasMap.get(col));
                if (dia.getMonthValue() == 4 && dia.getYear() == 2025) {
                    linha.getHorasPorData().put(dia.format(fmt), String.valueOf(horas));
                }
            }

            mapa.put(nome, linha);
        }

        workbook.close();
        return mapa;
    }

    private Map<String, LinhaTabelaDetalhada> processarJira() throws Exception {
        // semelhante ao CLAIM, mas somando valores dos dias por nome
        // pode-se usar o mesmo padrão de leitura por Workbook, Sheet etc.
        // nesse exemplo estou resumindo para simplificar a visualização da estrutura.
        return new HashMap<>();
    }

    private Map<String, Integer> getCabecalhos(Row headerRow) {
        Map<String, Integer> mapa = new HashMap<>();
        for (Cell cell : headerRow) {
            mapa.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
        }
        return mapa;
    }

    private String getString(Row row, Integer col) {
        Cell cell = row.getCell(col);
        return cell != null ? cell.toString() : "";
    }
}
