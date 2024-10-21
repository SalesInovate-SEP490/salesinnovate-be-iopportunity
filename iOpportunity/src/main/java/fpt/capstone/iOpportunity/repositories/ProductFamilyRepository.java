package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.Opportunity;
import fpt.capstone.iOpportunity.model.Product;
import fpt.capstone.iOpportunity.model.ProductFamily;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductFamilyRepository extends JpaRepository<ProductFamily,Long>, JpaSpecificationExecutor<Product> {
}
