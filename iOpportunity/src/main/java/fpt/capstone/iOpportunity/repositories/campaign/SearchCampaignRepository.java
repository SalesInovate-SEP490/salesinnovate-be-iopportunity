package fpt.capstone.iOpportunity.repositories.campaign;


import fpt.capstone.iOpportunity.model.campaign.Campaign;
import fpt.capstone.iOpportunity.model.campaign.CampaignStatus;
import fpt.capstone.iOpportunity.model.campaign.CampaignType;
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

import java.util.ArrayList;
import java.util.List;

import static fpt.capstone.iOpportunity.util.AppConst.STATUS_REGEX;
import static fpt.capstone.iOpportunity.util.AppConst.TYPE_REGEX;


@Component
@Slf4j
public class SearchCampaignRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public Page<Campaign> searchUserByCriteriaWithJoin(List<SpecSearchCriteria> params, Pageable pageable) {
        List<Campaign> campaigns = getAllCampaignWithJoin(params, pageable);
        Long totalElements = countAllCampaignWithJoin(params);
        return new PageImpl<>(campaigns, pageable, totalElements);
    }

    private List<Campaign> getAllCampaignWithJoin(List<SpecSearchCriteria> params, Pageable pageable) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Campaign> query = criteriaBuilder.createQuery(Campaign.class);
        Root<Campaign> root = query.from(Campaign.class);

        List<Predicate> predicateList = new ArrayList<>();

        for (SpecSearchCriteria criteria : params) {
            String key = criteria.getKey();
            if (key.contains(STATUS_REGEX)) {
                Join<CampaignStatus,Campaign> statusRoot = root.join("campaignStatus");
                predicateList.add(toJoinPredicate(statusRoot, criteriaBuilder, criteria, STATUS_REGEX));
            } else if (key.contains(TYPE_REGEX)) {
                Join<CampaignType,Campaign> typeRoot = root.join("campaignType");
                predicateList.add(toJoinPredicate(typeRoot, criteriaBuilder, criteria, TYPE_REGEX));
            }
            else {
                predicateList.add(toPredicate(root, criteriaBuilder, criteria));
            }
        }

        Predicate predicates = criteriaBuilder.and(predicateList.toArray(new Predicate[0]));
        query.where(predicates);

        return entityManager.createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    private Long countAllCampaignWithJoin(List<SpecSearchCriteria> params) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = criteriaBuilder.createQuery(Long.class);
        Root<Campaign> root = query.from(Campaign.class);

        List<Predicate> predicateList = new ArrayList<>();

        for (SpecSearchCriteria criteria : params) {
            String key = criteria.getKey();
            if (key.contains(STATUS_REGEX)) {
                Join<CampaignStatus,Campaign> statusRoot = root.join("campaignStatus");
                predicateList.add(toJoinPredicate(statusRoot, criteriaBuilder, criteria, STATUS_REGEX));
            } else if (key.contains(TYPE_REGEX)) {
                Join<CampaignType,Campaign> typeRoot = root.join("campaignType");
                predicateList.add(toJoinPredicate(typeRoot, criteriaBuilder, criteria, TYPE_REGEX));
            }
            else {
                predicateList.add(toPredicate(root, criteriaBuilder, criteria));
            }
        }

        Predicate predicates = criteriaBuilder.and(predicateList.toArray(new Predicate[0]));

        query.select(criteriaBuilder.count(root));
        query.where(predicates);

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

    private Predicate toJoinPredicate(@NonNull Join<?,Campaign> root,@NonNull  CriteriaBuilder builder,@NonNull  SpecSearchCriteria criteria, String regex) {
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
