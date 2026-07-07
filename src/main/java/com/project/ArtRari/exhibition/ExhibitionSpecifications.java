package com.project.ArtRari.exhibition;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ExhibitionSpecifications {
    public static Predicate buildSearchPredicate(From<?, ?> from, CriteriaBuilder cb, String search) {
        String pattern = "%" + search.toLowerCase() + "%";
        Predicate p1 = cb.like(cb.lower(from.get("title")), pattern);
        Predicate p2 = cb.like(cb.lower(from.get("theme")), pattern);
        Predicate p3 = cb.like(cb.lower(from.get("description")), pattern);
        return cb.or(p1, p2, p3);
    }

    public static Specification<Exhibition> hasText(String search) {
        return (table, query, criteriaBuilder) ->
            buildSearchPredicate(table, criteriaBuilder, search);
    }

    public static Specification<Exhibition> hasTags(List<String> tags) {
        return (table, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Object, Object> artworkJoin = table.join("artworks");
            Join<Object, Object> atworkAndTagJoin = artworkJoin.join("tags");
            return atworkAndTagJoin.get("name").in(tags);
        };
    }
}
