package dev.gdataa.hyvel.pets.items;

import java.util.List;

public class PetClothing {
    private final String id;
    private final String nome;
    private final String texturaBase64;
    private final String raridade;
    private final List<String> bonusExtra;
    private final double preco; // <- NOVO: Preço na loja

    public PetClothing(String id, String nome, String texturaBase64, String raridade, List<String> bonusExtra, double preco) {
        this.id = id;
        this.nome = nome;
        this.texturaBase64 = texturaBase64;
        this.raridade = raridade;
        this.bonusExtra = bonusExtra;
        this.preco = preco;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getTexturaBase64() { return texturaBase64; }
    public String getRaridade() { return raridade; }
    public List<String> getBonusExtra() { return bonusExtra; }
    public double getPreco() { return preco; }
}