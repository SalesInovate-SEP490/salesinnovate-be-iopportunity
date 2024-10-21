package fpt.capstone.iOpportunity.repositories;


import fpt.capstone.iOpportunity.model.LeadSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadSourceRepository extends JpaRepository<LeadSource,Long> {
}
