package com.project.ArtRari.artwork;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ArtworkSpecifications {
    public static Specification<Artwork> hasText(String search) {
        return (table, query, criteriaBuilder) -> {
            String pattern = "%" + search.toLowerCase() + "%";
            Predicate p1 = criteriaBuilder.like(criteriaBuilder.lower(table.get("title")), pattern);
            Predicate p2 = criteriaBuilder.like(criteriaBuilder.lower(table.get("description")), pattern);
            Predicate p3 = criteriaBuilder.like(criteriaBuilder.lower(table.get("author")), pattern);
            Predicate p4 = criteriaBuilder.like(criteriaBuilder.lower(table.get("technique")), pattern);
            return criteriaBuilder.or(p1, p2, p3, p4);
        };
    }

    public static Specification<Artwork> hasTags(List<String> tags) {
        return (table, query, criteriaBuilder) -> {
            Join<Object, Object> tagJoin = table.join("tags");
            return tagJoin.get("name").in(tags);
        };
    }
}
