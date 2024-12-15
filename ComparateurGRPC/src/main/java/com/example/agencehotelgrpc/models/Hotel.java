package com.example.agencehotelgrpc.models;

import lombok.NoArgsConstructor;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "hotel")
@NoArgsConstructor
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "nb_etoiles")
    private int nbEtoiles;

    @Column(name = "nb_lits_disponibles")
    private int nbLitsDisponibles;


    @OneToOne
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Adresse adresse;

    @ManyToMany
    @LazyCollection(LazyCollectionOption.FALSE)
    @JoinTable(
            name = "hotel_agence",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "agence_id")
    )
    private List<Agence> agencesPartenaires;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getNbEtoiles() {
        return nbEtoiles;
    }

    public void setNbEtoiles(int nbEtoiles) {
        this.nbEtoiles = nbEtoiles;
    }

    public int getNbLitsDisponibles() {
        return nbLitsDisponibles;
    }

    public void setNbLitsDisponibles(int nbLitsDisponibles) {
        this.nbLitsDisponibles = nbLitsDisponibles;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }

    public List<Agence> getAgencesPartenaires() {
        return agencesPartenaires;
    }

    public void setAgencesPartenaires(List<Agence> agencesPartenaires) {
        this.agencesPartenaires = agencesPartenaires;
    }
}
