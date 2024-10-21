package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, JpaSpecificationExecutor<Product> {
}
