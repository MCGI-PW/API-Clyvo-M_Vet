package com.clyvo.veterinary.pet.domain.model;

public enum Species {
    DOG("Cachorro"), CAT("Gato"), BIRD("Pássaro"), RABBIT("Coelho"), 
    HAMSTER("Hamster"), REPTILE("Réptil"), FISH("Peixe"), OTHER("Outro");

    private final String displayName;

    Species(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
