package com.example.excelprocessor.model;

public class ComparativoHoras {
    private String nome;
    private String nomeNormalizado;
    private double horasJira;
    private double horasClaim;
    private double diferenca;
    
    // Construtor padrão
    public ComparativoHoras() {
    }
    
    // Construtor com parâmetros
    public ComparativoHoras(String nome, String nomeNormalizado, double horasJira, double horasClaim) {
        this.nome = nome;
        this.nomeNormalizado = nomeNormalizado;
        this.horasJira = horasJira;
        this.horasClaim = horasClaim;
        this.diferenca = horasClaim - horasJira; // Calcula automaticamente a diferença
    }
    
    // Construtor completo
    public ComparativoHoras(String nome, String nomeNormalizado, double horasJira, double horasClaim, double diferenca) {
        this.nome = nome;
        this.nomeNormalizado = nomeNormalizado;
        this.horasJira = horasJira;
        this.horasClaim = horasClaim;
        this.diferenca = diferenca;
    }
    
    // Getters
    public String getNome() {
        return nome;
    }
    
    public String getNomeNormalizado() {
        return nomeNormalizado;
    }
    
    public double getHorasJira() {
        return horasJira;
    }
    
    public double getHorasClaim() {
        return horasClaim;
    }
    
    public double getDiferenca() {
        return diferenca;
    }
    
    // Setters
    public void setNome(String nome) {
        this.nome = nome;
    }
    
    public void setNomeNormalizado(String nomeNormalizado) {
        this.nomeNormalizado = nomeNormalizado;
    }
    
    public void setHorasJira(double horasJira) {
        this.horasJira = horasJira;
    }
    
    public void setHorasClaim(double horasClaim) {
        this.horasClaim = horasClaim;
    }
    
    public void setDiferenca(double diferenca) {
        this.diferenca = diferenca;
    }
    
    // Método utilitário para recalcular a diferença
    public void calcularDiferenca() {
        this.diferenca = this.horasClaim - this.horasJira;
    }
    
    // toString para debug
    @Override
    public String toString() {
        return "ComparativoHoras{" +
                "nome='" + nome + '\'' +
                ", nomeNormalizado='" + nomeNormalizado + '\'' +
                ", horasJira=" + horasJira +
                ", horasClaim=" + horasClaim +
                ", diferenca=" + diferenca +
                '}';
    }
    
    // equals e hashCode baseados no nome normalizado
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComparativoHoras that = (ComparativoHoras) o;
        return nomeNormalizado != null ? nomeNormalizado.equals(that.nomeNormalizado) : that.nomeNormalizado == null;
    }
    
    @Override
    public int hashCode() {
        return nomeNormalizado != null ? nomeNormalizado.hashCode() : 0;
    }
}