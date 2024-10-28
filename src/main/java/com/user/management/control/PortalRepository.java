package com.user.management.control;

import com.user.management.entity.Portal;
import com.user.management.entity.PortalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PortalRepository extends JpaRepository<Portal, PortalStatus> {

}
