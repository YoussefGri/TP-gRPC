package com.example.agencehotelgrpc.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Converters {

    public static Date stringToDate(String dateStr) {
        // Spécifie le format de date attendu
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date date = null;

        try {
            // Convertit la chaîne de date en objet Date
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            // Gérer l'erreur si nécessaire
        }

        return date;
    }

    public static byte[] loadImageBytes(String imagePath) {
        try {
            Path path = Paths.get(imagePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Erreur lors du chargement de l'image: " + e.getMessage());
            return new byte[0]; // Retourner un tableau vide si l'image n'est pas trouvée
        }
    }



}