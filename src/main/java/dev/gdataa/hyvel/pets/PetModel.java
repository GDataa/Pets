package dev.gdataa.hyvel.pets;

import java.util.List;

public class PetModel {
    private final String id;
    private final String nome;
    private final String textura;
    private final PetCategory categoria;
    private final String raridade;
    private final List<String> bonus;

    public PetModel(String id, String nome, String textura, PetCategory categoria, String raridade, List<String> bonus) {
        this.id = id;
        this.nome = nome;
        this.textura = textura;
        this.categoria = categoria;
        this.raridade = raridade;
        this.bonus = bonus;
    }

    // Getters essenciais para o funcionamento do Banco de Dados e Menus
    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTextura() {
        return textura;
    }

    public PetCategory getCategoria() {
        return categoria;
    }

    public String getRaridade() {
        return raridade;
    }

    public List<String> getBonus() {
        return bonus;
    }
}