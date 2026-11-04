package com.zhituxing.repository;

import com.zhituxing.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    List<Destination> findAllByOrderByIdAsc();
}
