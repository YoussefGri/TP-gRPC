package com.example.agencehotelgrpc.repositories;

import com.example.agencehotelgrpc.models.Chambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Integer> {

}
