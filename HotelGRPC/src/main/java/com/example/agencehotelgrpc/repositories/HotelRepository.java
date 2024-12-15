package com.example.agencehotelgrpc.repositories;

import com.example.agencehotelgrpc.models.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface HotelRepository extends JpaRepository<Hotel, Integer> {


}