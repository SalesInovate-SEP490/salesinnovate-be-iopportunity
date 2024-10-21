package fpt.capstone.iOpportunity.repositories;

import fpt.capstone.iOpportunity.model.*;
import fpt.capstone.iOpportunity.repositories.specification.SpecSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static fpt.capstone.iOpportunity.util.AppConst.*;


@Component
@Slf4j
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Page<Opportunity> searchUserByCriteriaWithJoin(String userId,List<SpecSearchCriteria> params, Pageable pageable) {
        List<Opportunity> users = getAllLeadsWithJoin(userId,params, pageable);
        Long totalElements = countAllLeadsWithJoin(userId,params);
        return new PageImpl<>(users, pageable, totalElements);
    }

    private List<Opportunity> getAllLeadsWithJoin(String userId,List<SpecSearchCriteria> params, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Opportunity> query = criteriaBuilder.createQuery(Opportunity.class);
        Root<Opportunity> leadsRoot = query.from(Opportunity.class);

        List<Predicate> predicateList = new ArrayList<>();

        for (SpecSearchCriteria criteria : params) {
            String key = criteria.getKey();
            if (key.contains(FORECAST_REGEX)) {
                Join<Forecast,Opportunity> industryRoot = leadsRoot.join("forecast");
                predicateList.add(toJoinPredicate(industryRoot, criteriaBuilder, criteria, FORECAST_REGEX));
            } else if (key.contains(STAGE_REGEX)) {
                Join<Stage,Opportunity> ratingRoot = leadsRoot.join("stage");
                predicateList.add(toJoinPredicate(ratingRoot, criteriaBuilder, criteria, STAGE_REGEX));
            }else if (key.contains(TYPE_REGEX)) {
                Join<Type,Opportunity> salutionRoot = leadsRoot.join("type");
                predicateList.add(toJoinPredicate(salutionRoot, criteriaBuilder, criteria, TYPE_REGEX));
            }else if (key.contains(SOURCE_REGEX)) {
                Join<LeadSource,Opportunity> sourceRoot = leadsRoot.join("leadSource");
                predicateList.add(toJoinPredicate(sourceRoot, criteriaBuilder, criteria, SOURCE_REGEX));
            }
            else {
                if (key.equals("createDate") || key.equals("editDate")) {
                    predicateList.add(toPredicateDateTime(leadsRoot, criteriaBuilder, criteria));
                } else
                    predicateList.add(toPredicate(leadsRoot, criteriaBuilder, criteria));
            }
        }
        Join<Opportunity, Users> join = leadsRoot.join("users", JoinType.INNER);
        predicateList.add(criteriaBuilder.equal(join.get("userId"), userId));
        predicateList.add(criteriaBuilder.equal(leadsRoot.get("isDeleted"),false));

        Predicate predicates = criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        query.where(predicates);

        // Sắp xếp theo trường createDate
        query.orderBy(criteriaBuilder.desc(leadsRoot.get("createDate")));

        return entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber()*pageable.getPageSize())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private Long countAllLeadsWithJoin(String userId,List<SpecSearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<Opportunity> leadsRoot = query.from(Opportunity.class);

        List<Predicate> predicateList = new ArrayList<>();

        for (SpecSearchCriteria criteria : params) {
            String key = criteria.getKey();
            if (key.contains(FORECAST_REGEX)) {
                Join<Forecast,Opportunity> industryRoot = leadsRoot.join("forecast");
                predicateList.add(toJoinPredicate(industryRoot, criteriaBuilder, criteria, FORECAST_REGEX));
            } else if (key.contains(STAGE_REGEX)) {
                Join<Stage,Opportunity> ratingRoot = leadsRoot.join("stage");
                predicateList.add(toJoinPredicate(ratingRoot, criteriaBuilder, criteria, STAGE_REGEX));
            }else if (key.contains(TYPE_REGEX)) {
                Join<Type,Opportunity> salutionRoot = leadsRoot.join("type");
                predicateList.add(toJoinPredicate(salutionRoot, criteriaBuilder, criteria, TYPE_REGEX));
            }else if (key.contains(SOURCE_REGEX)) {
                Join<LeadSource,Opportunity> sourceRoot = leadsRoot.join("leadSource");
                predicateList.add(toJoinPredicate(sourceRoot, criteriaBuilder, criteria, SOURCE_REGEX));
            }
            else {
                if (key.equals("createDate") || key.equals("editDate")) {
                    predicateList.add(toPredicateDateTime(leadsRoot, criteriaBuilder, criteria));
                } else
                    predicateList.add(toPredicate(leadsRoot, criteriaBuilder, criteria));
            }
        }
        Join<Opportunity, Users> join = leadsRoot.join("users", JoinType.INNER);
        predicateList.add(criteriaBuilder.equal(join.get("userId"), userId));
        predicateList.add(criteriaBuilder.equal(leadsRoot.get("isDeleted"),false));

        Predicate predicates = criteriaBuilder.and(predicateList.toArray(new Predicate[0]));

        query.select(criteriaBuilder.count(leadsRoot));
        query.where(predicates);

        // Sắp xếp theo trường createDate
        query.orderBy(criteriaBuilder.desc(leadsRoot.get("createDate")));

        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate toPredicate(@NonNull Root<?> root, @NonNull CriteriaBuilder builder,@NonNull  SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(criteria.getKey()),  criteria.getValue().toString() );
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    private Predicate toPredicateDateTime(@NonNull Root<?> root, @NonNull CriteriaBuilder builder, @NonNull SpecSearchCriteria criteria) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(criteria.getKey()), LocalDateTime.parse(criteria.getValue().toString(), formatter));
            case LESS_THAN -> builder.lessThan(root.get(criteria.getKey()), LocalDateTime.parse(criteria.getValue().toString(), formatter));
            case LIKE -> builder.like(root.get(criteria.getKey()),  criteria.getValue().toString() );
            case STARTS_WITH -> builder.like(root.get(criteria.getKey()), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }

    private Predicate toJoinPredicate(@NonNull Join<?,Opportunity> root,@NonNull  CriteriaBuilder builder,@NonNull  SpecSearchCriteria criteria, String regex) {
        String key = criteria.getKey();
        return switch (criteria.getOperation()) {
            case EQUALITY -> builder.equal(root.get(key.replace(regex, "")), criteria.getValue());
            case NEGATION -> builder.notEqual(root.get(key.replace(regex, "")), criteria.getValue());
            case GREATER_THAN -> builder.greaterThan(root.get(key.replace(regex, "")), criteria.getValue().toString());
            case LESS_THAN -> builder.lessThan(root.get(key.replace(regex, "")), criteria.getValue().toString());
            case LIKE -> builder.like(root.get(key.replace(regex, "")),   criteria.getValue().toString() );
            case STARTS_WITH -> builder.like(root.get(key.replace(regex, "")), criteria.getValue() + "%");
            case ENDS_WITH -> builder.like(root.get(key.replace(regex, "")), "%" + criteria.getValue());
            case CONTAINS -> builder.like(root.get(key.replace(regex, "")), "%" + criteria.getValue() + "%");
        };
    }

}
