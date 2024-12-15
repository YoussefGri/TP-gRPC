package com.example.agencehotelgrpc.repositories;


import com.example.agencehotelgrpc.models.Agence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface AgenceRepository extends JpaRepository<Agence, Integer> {

}
