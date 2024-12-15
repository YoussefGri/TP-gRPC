package com.example.agencehotelgrpc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

public class OffreDTO {
    private int idOffre;
    private String dateDebutOffre;
    private String dateFinOffre;
    private int nbPersonnes; // 1: simple, 2: double, 3: triple
    //private String imageUrl;
    private String ville;
    byte[] image;
    private String nomHotel;
    private int idHotel;
    private int nbEtoiles;
    private double prix;
    private double pourcentageReduction;

    public OffreDTO(){}

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public double getPourcentageReduction() {
        return pourcentageReduction;
    }

    public void setPourcentageReduction(double pourcentageReduction) {
        this.pourcentageReduction = pourcentageReduction;
    }

    public int getNbEtoiles() {
        return nbEtoiles;
    }

    public void setNbEtoiles(int nbEtoiles) {
        this.nbEtoiles = nbEtoiles;
    }

    public int getIdOffre() {
        return idOffre;
    }

    public void setIdOffre(int idOffre) {
        this.idOffre = idOffre;
    }

    public String getDateDebutOffre() {
        return dateDebutOffre;
    }

    public void setDateDebutOffre(String dateDebutOffre) {
        this.dateDebutOffre = dateDebutOffre;
    }

    public String getDateFinOffre() {
        return dateFinOffre;
    }

    public void setDateFinOffre(String dateFinOffre) {
        this.dateFinOffre = dateFinOffre;
    }

    public int getNbPersonnes() {
        return nbPersonnes;
    }

    public void setNbPersonnes(int nbPersonnes) {
        this.nbPersonnes = nbPersonnes;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    public int getIdHotel() {
        return idHotel;
    }

    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}

