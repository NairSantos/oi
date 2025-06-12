package com.example.excelprocessor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.excelprocessor.model.ComparativoHoras;
import com.example.excelprocessor.model.JiraItem;
import com.example.excelprocessor.model.LinhaTabelaDetalhada;
import com.example.excelprocessor.model.Usuario;

@Controller
public class ExcelController {
    
    @Autowired
    private ExcelProcessorService excelProcessorService;

    @Autowired
    private TabelaComparativaService comparativaService;
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    @PostMapping("/processar")
    public String processarPlanilhas(@RequestParam("planilhaJira") MultipartFile planilhaJira,
                                   @RequestParam("planilhaClaim") MultipartFile planilhaClaim,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            System.out.println("=== INICIANDO PROCESSAMENTO ===");
            
            // Processa planilha JIRA
            System.out.println("Processando planilha JIRA...");
            List<Usuario> usuarios = excelProcessorService.processarPlanilhaJira(planilhaJira);
            System.out.println("Usuários JIRA encontrados: " + usuarios.size());
            
            // Processa planilha CLAIM comparando com JIRA
            System.out.println("Processando planilha CLAIM...");
            List<JiraItem> claimItems = excelProcessorService.processarPlanilhaClaim(planilhaClaim, usuarios);
            System.out.println("Itens CLAIM encontrados: " + claimItems.size());
            
            // Adiciona dados ao modelo
            model.addAttribute("usuarios", usuarios);
            model.addAttribute("claimItems", claimItems);
            List<ComparativoHoras> resumoHoras = excelProcessorService.gerarResumoComparativo(usuarios, claimItems);
            model.addAttribute("tabelaResumoHoras", resumoHoras);

            model.addAttribute("sucesso", true);
            
            // Debug: mostra primeira linha de cada tipo
            if (!usuarios.isEmpty()) {
                System.out.println("=== PRIMEIRO USUÁRIO JIRA ===");
                Usuario primeiroUsuario = usuarios.get(0);
                System.out.println("Nome: " + primeiroUsuario.getNome());
                System.out.println("Campos: " + primeiroUsuario.getDados().keySet());
                model.addAttribute("usuariosCabecalhos", primeiroUsuario.getDados().keySet());
            }
            
            if (!claimItems.isEmpty()) {
                System.out.println("=== PRIMEIRO ITEM CLAIM ===");
                JiraItem primeiroItem = claimItems.get(0);
                System.out.println("Nome: " + primeiroItem.getNome());
                System.out.println("Campos: " + primeiroItem.getDados().keySet());
                model.addAttribute("claimCabecalhos", primeiroItem.getDados().keySet());
            }

            //  Adiciona tabela comparativa detalhada
            try {
                List<LinhaTabelaDetalhada> tabela = comparativaService.gerarTabelaComparativa();
                model.addAttribute("tabelaDetalhada", tabela);
            } catch (Exception e) {
                System.err.println("Erro ao gerar tabela comparativa: " + e.getMessage());
                model.addAttribute("erroComparativo", "Erro ao gerar tabela comparativa: " + e.getMessage());
            }

            System.out.println("=== PROCESSAMENTO CONCLUÍDO ===");
            return "resultados";
            
        } catch (Exception e) {
            System.err.println("ERRO no processamento: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("erro", "Erro ao processar planilhas: " + e.getMessage());
            return "redirect:/";
        }
    }
}
