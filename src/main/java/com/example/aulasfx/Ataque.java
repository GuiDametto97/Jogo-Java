package com.example.aulasfx;

// Enum simples para representar os tipos de golpes
public enum Ataque {
    SOCO("Soco", 10),
    CHUTE("Chute", 15),
    ESPECIAL("Especial", 30);

    private final String nome;
    private final int danoBase;

    Ataque(String nome, int danoBase) {
        this.nome = nome;
        this.danoBase = danoBase;
    }

    public String getNome() {
        return nome;
    }

    public int getDanoBase() {
        return danoBase;
    }
}