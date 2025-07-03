package com.prodash.infrastructure.adapter.out.persistence;

// Esta classe pode ser embutida, não precisa da anotação @Document
public class ThemeDocument {
    private Integer cod;
    private String nome;

    // Getters e Setters
    public Integer getCod() { return cod; }
    public void setCod(Integer cod) { this.cod = cod; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}