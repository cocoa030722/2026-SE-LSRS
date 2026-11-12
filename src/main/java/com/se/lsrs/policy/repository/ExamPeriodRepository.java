package com.se.lsrs.policy.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.se.lsrs.policy.domain.ExamPeriod;

public interface ExamPeriodRepository extends JpaRepository<ExamPeriod, Long> {

	/** date 가 어떤 시험 기간에 포함되는지 */
	@Query("select count(e) > 0 from ExamPeriod e where e.startDate <= :date and e.endDate >= :date")
	boolean existsCovering(LocalDate date);

	/** [startDate, endDate] 와 겹치는 시험 기간이 있는지 (양 끝 포함) */
	@Query("select count(e) > 0 from ExamPeriod e where e.startDate <= :endDate and e.endDate >= :startDate")
	boolean existsOverlapping(LocalDate startDate, LocalDate endDate);

	List<ExamPeriod> findAllByOrderByStartDateAsc();

}
