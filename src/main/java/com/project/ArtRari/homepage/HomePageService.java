package com.project.ArtRari.homepage;

import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionService;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class HomePageService {
    private AuctionService auctionService;
    private ExhibitionService exhibitionService;

    public HomePage getHomePage() {
        List<Auction> auctions = auctionService.hashCode();
        List<Exhibition> exhibitions = exhibitionService.getTop6();
        return new HomePage(auctions, exhibitions);
    } //todo а если их меньше чем стандартное кол-во?
}
