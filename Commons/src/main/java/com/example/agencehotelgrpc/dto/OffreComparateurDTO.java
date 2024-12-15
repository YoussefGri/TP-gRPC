package com.example.agencehotelgrpc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OffreComparateurDTO {

    private String nomHotel;
    private String nomAgence;
    private String adresseAgence;
    private String adresseHotel;
    private int nbEtoiles;
    private int nbLitsDisponibles;
    private double prix;
    private double pourcentageReduction;

    public OffreComparateurDTO(String nomHotel, String nomAgence, String adresseAgence, String adresseHotel, int nbEtoiles, int nbLitsDisponibles, double prix, double pourcentageReduction) {
        this.nomHotel = nomHotel;
        this.nomAgence = nomAgence;
        this.adresseAgence = adresseAgence;
        this.adresseHotel = adresseHotel;
        this.nbEtoiles = nbEtoiles;
        this.nbLitsDisponibles = nbLitsDisponibles;
        this.prix = prix;
        this.pourcentageReduction = pourcentageReduction;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    public String getNomAgence() {
        return nomAgence;
    }

    public void setNomAgence(String nomAgence) {
        this.nomAgence = nomAgence;
    }

    public String getAdresseAgence() {
        return adresseAgence;
    }

    public void setAdresseAgence(String adresseAgence) {
        this.adresseAgence = adresseAgence;
    }

    public String getAdresseHotel() {
        return adresseHotel;
    }

    public void setAdresseHotel(String adresseHotel) {
        this.adresseHotel = adresseHotel;
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

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public double getPourcentageReduction() {
        return pourcentageReduction;
    }

    public void setPourcentageReduction(double pourcentageReduction) {
        this.pourcentageReduction = pourcentageReduction;
    }
}
