package com.example.excelprocessor.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Tamanho do arquivo excede o limite permitido (10MB)");
        return "redirect:/";
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException exc, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erro", "Erro de formato: " + exc.getMessage());
        return "redirect:/";
    }
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception exc, RedirectAttributes redirectAttributes) {
        String message = exc.getMessage();
        if (message != null && message.length() > 100) {
            message = message.substring(0, 100) + "...";
        }
        redirectAttributes.addFlashAttribute("erro", "Erro ao processar: " + message);
        return "redirect:/";
    }
}