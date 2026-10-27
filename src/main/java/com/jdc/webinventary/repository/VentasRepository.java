package com.jdc.webinventary.repository;

import com.jdc.webinventary.entity.VentasEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VentasRepository extends JpaRepository<VentasEntity, Integer> {
}
