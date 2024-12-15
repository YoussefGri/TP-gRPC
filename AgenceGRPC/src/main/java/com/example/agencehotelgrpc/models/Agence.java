package com.example.agencehotelgrpc.models;

import lombok.Data;
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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "agence_hotel",
            joinColumns = @JoinColumn(name = "agence_id"),
            inverseJoinColumns = @JoinColumn(name = "hotel_id")
    )
    private List<Hotel> hotelsPartenaires = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "adresse_id", referencedColumnName = "id")
    private Adresse adresse;

    @ManyToMany(mappedBy = "agences")
    private List<Client> clients;

    @OneToMany(mappedBy = "agence")
    private List<Reservation> reservations;

    public boolean addHotelPartenaire(Hotel hotel) {
        if (hotelsPartenaires.contains(hotel)) {
            return false;
        }
        hotelsPartenaires.add(hotel);
        return true;
    }

    public boolean addClient(Client client) {
        if (clients.contains(client)) {
            return false;
        }
        clients.add(client);
        return true;
    }

    public boolean addReservation(Reservation reservation) {
        if (reservations.contains(reservation)) {
            return false;
        }
        reservations.add(reservation);
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

    public List<Client> getClients() {
        return clients;
    }

    public void setClients(List<Client> clients) {
        this.clients = clients;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }
}
