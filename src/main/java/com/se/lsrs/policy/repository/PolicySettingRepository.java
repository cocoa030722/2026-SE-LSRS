package com.se.lsrs.policy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se.lsrs.policy.domain.PolicySetting;

public interface PolicySettingRepository extends JpaRepository<PolicySetting, String> {

	List<PolicySetting> findAllByOrderByPolicyKeyAsc();

}
