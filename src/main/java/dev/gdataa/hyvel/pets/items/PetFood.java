package dev.gdataa.hyvel.pets.items;

import java.util.List;

public class PetFood {
    private final String id;
    private final String nome;
    private final String texturaBase64;
    private final String raridade;
    private final List<String> bonusTemporario;
    private final int duracaoMinutos; // Quanto tempo o bónus dura
    private final double preco; // Preço na loja

    public PetFood(String id, String nome, String texturaBase64, String raridade, List<String> bonusTemporario, int duracaoMinutos, double preco) {
        this.id = id;
        this.nome = nome;
        this.texturaBase64 = texturaBase64;
        this.raridade = raridade;
        this.bonusTemporario = bonusTemporario;
        this.duracaoMinutos = duracaoMinutos;
        this.preco = preco;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getTexturaBase64() { return texturaBase64; }
    public String getRaridade() { return raridade; }
    public List<String> getBonusTemporario() { return bonusTemporario; }
    public int getDuracaoMinutos() { return duracaoMinutos; }
    public double getPreco() { return preco; }
}