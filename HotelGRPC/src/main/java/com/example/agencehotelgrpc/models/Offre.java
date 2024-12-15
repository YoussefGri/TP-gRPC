package com.example.agencehotelgrpc.models;


import lombok.NoArgsConstructor;

import jakarta.persistence.*;


@Entity
@Table(name = "offre")
@NoArgsConstructor
public class Offre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "date_debut_offre")
    private String dateDebutOffre;

    @Column(name = "date_fin_offre")
    private String dateFinOffre;

    @Column(name = "type_chambre")
    private int typeChambre; // 1: simple, 2: double, 3: triple

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "agence_id", nullable = false)
    private Agence agence;

    private double pourcentageReduction;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(int typeChambre) {
        this.typeChambre = typeChambre;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public double getPourcentageReduction() {
        return pourcentageReduction;
    }

    public void setPourcentageReduction(double pourcentageReduction) {
        this.pourcentageReduction = pourcentageReduction;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }
}
