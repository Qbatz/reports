package com.smartstay.reports.repositories;

import com.smartstay.reports.dao.Beds;
import com.smartstay.reports.dto.beds.BedDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BedsRepository extends JpaRepository<Beds, Integer> {
    @Query(value = """
            SELECT bed.bed_id as bedId, bed.bed_name as bedName, flrs.floor_id as floorId, 
            flrs.floor_name as floorName, rms.room_id as roomId, rms.room_name as roomName 
            FROM beds bed left outer JOIN rooms rms on rms.room_id=bed.room_id LEFT OUTER JOIN 
            floors flrs on flrs.floor_id=rms.floor_id where bed.bed_id=:bedId;
            """, nativeQuery = true)
    BedDetails findByBedId(@Param("bedId") Integer bedId);
}
