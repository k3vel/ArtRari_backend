package com.project.ArtRari.auction;

import com.project.ArtRari.exhibition.ExhibitionSpecifications;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class AuctionSpecifications {
    public static Specification<Auction> hasStatuses(List<AuctionStatus> statuses) {
        return (table, query, criteriaBuilder) ->
                table.get("status").in(statuses);
    }

    public static Specification<Auction> isNotEnded() {
        return (table, query, criteriaBuilder) ->
                criteriaBuilder.greaterThan(table.get("endDate"), Instant.now());
    }

    public static Specification<Auction> hasText(String search) {
        return (table, query, criteriaBuilder) -> {
            Join<Object, Object> exhibitionJoin = table.join("exhibition");
            return ExhibitionSpecifications.buildSearchPredicate(exhibitionJoin, criteriaBuilder, search);
        };
    }

    public static Specification<Auction> hasTags(List<String> tags) {
        return (table, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<Object, Object> exhibitionJoin = table.join("exhibition");
            Join<Object, Object> artworkExhibitionJoin = exhibitionJoin.join("artworks");
            Join<Object, Object> atworkExhibitionTagJoin = artworkExhibitionJoin.join("tags");
            return atworkExhibitionTagJoin.get("name").in(tags);
        };
    }
}
