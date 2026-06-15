package com.example.shippingmethod.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.shippingmethod.model.HolidayDate;

@Repository
public interface HolidayDateRepository extends JpaRepository<HolidayDate, Long> {

	List<HolidayDate> findByWarehouseId(Long warehouseId);
	List<HolidayDate> findByWarehouseIdAndIsRecurringTrue(Long warehouseId);
	boolean existsByWarehouseIdAndHolidayDate(Long warehouseId, LocalDate hollidayDate);
}
