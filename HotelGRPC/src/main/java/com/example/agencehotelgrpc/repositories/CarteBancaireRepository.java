package com.example.agencehotelgrpc.repositories;

import com.example.agencehotelgrpc.models.CarteBancaire;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarteBancaireRepository extends JpaRepository<CarteBancaire, Integer> {
}
