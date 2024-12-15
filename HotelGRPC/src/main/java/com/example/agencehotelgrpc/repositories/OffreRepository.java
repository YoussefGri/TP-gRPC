package com.example.agencehotelgrpc.repositories;

import com.example.agencehotelgrpc.models.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface OffreRepository extends JpaRepository<Offre, Integer> {

}
