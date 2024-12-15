package com.example.agencehotelgrpc.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ReservationDTO {

    private int idReservation;
    private String nomHotel;
    private String ville;
    private String dateDebut;
    private String dateFin;
    private String nomClient;
    private String prenomClient;
    private String emailClient;
    private double prix;

    public ReservationDTO(int idReservation, String nomHotel, String ville, String dateDebut, String dateFin, String nomClient, String prenomClient, String emailClient, double prix) {
        this.idReservation = idReservation;
        this.nomHotel = nomHotel;
        this.ville = ville;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.emailClient = emailClient;
        this.prix = prix;
    }

    public String getEmailClient() {
        return emailClient;
    }

    public void setEmailClient(String emailClient) {
        this.emailClient = emailClient;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public String getNomHotel() {
        return nomHotel;
    }

    public void setNomHotel(String nomHotel) {
        this.nomHotel = nomHotel;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(String dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getDateFin() {
        return dateFin;
    }

    public void setDateFin(String dateFin) {
        this.dateFin = dateFin;
    }

    public String getNomClient() {
        return nomClient;
    }

    public void setNomClient(String nomClient) {
        this.nomClient = nomClient;
    }

    public String getPrenomClient() {
        return prenomClient;
    }

    public void setPrenomClient(String prenomClient) {
        this.prenomClient = prenomClient;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}
