package com.project.ArtRari.homepage;

import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.exhibition.Exhibition;

import java.util.List;

public record HomePage(
        List<Auction> currentAuctions,
        List<Exhibition> topExhibitions
) {}
