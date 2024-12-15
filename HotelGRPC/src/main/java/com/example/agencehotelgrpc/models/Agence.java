package com.example.agencehotelgrpc.models;

import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agence")
@NoArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "password")
    private String password;

    @Column(name = "nom")
    private String nom;

    @ManyToMany
    @JoinTable(
            name = "agence_hotel",
            joinColumns = @JoinColumn(name = "agence_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private List<Hotel> hotelsPartenaires = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Adresse adresse;

    public boolean addHotelPartenaire(Hotel hotel) {
        if (hotelsPartenaires.contains(hotel)) {
            return false;
        }
        hotelsPartenaires.add(hotel);
        return true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public List<Hotel> getHotelsPartenaires() {
        return hotelsPartenaires;
    }

    public void setHotelsPartenaires(List<Hotel> hotelsPartenaires) {
        this.hotelsPartenaires = hotelsPartenaires;
    }

    public Adresse getAdresse() {
        return adresse;
    }

    public void setAdresse(Adresse adresse) {
        this.adresse = adresse;
    }
}
