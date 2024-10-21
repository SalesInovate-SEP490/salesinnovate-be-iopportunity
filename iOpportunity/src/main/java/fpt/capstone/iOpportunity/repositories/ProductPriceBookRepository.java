package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.PriceBook;
import fpt.capstone.iOpportunity.model.ProductPriceBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPriceBookRepository extends JpaRepository<ProductPriceBook,Long>, JpaSpecificationExecutor<ProductPriceBook> {
}
