package com.zhituxing.repository;

import com.zhituxing.entity.Spot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpotRepository extends JpaRepository<Spot, Long> {

    @Query("""
            select s from Spot s
            where (:keyword is null or s.name like concat('%', :keyword, '%'))
              and (:city is null or s.city = :city)
              and (:category is null or s.category = :category)
              and (:status is null or s.status = :status)
            """)
    Page<Spot> search(@Param("keyword") String keyword,
                      @Param("city") String city,
                      @Param("category") String category,
                      @Param("status") String status,
                      Pageable pageable);

    @Query("""
            select s from Spot s
            where (:keyword is null or s.name like concat('%', :keyword, '%'))
              and (:city is null or s.city = :city)
              and (:category is null or s.category = :category)
              and (:auditStatus is null or s.auditStatus = :auditStatus)
            """)
    Page<Spot> adminSearch(@Param("keyword") String keyword,
                           @Param("city") String city,
                           @Param("category") String category,
                           @Param("auditStatus") String auditStatus,
                           Pageable pageable);
}
