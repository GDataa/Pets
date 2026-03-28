package dev.gdataa.hyvel.pets;

public enum PetCategory {
    MINERACAO("Mineração"),
    ECONOMIA("Economia"),
    FARM("Farm"),
    PESCA("Pesca");

    private final String displayName;

    PetCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}