package com.project.ArtRari.bid;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.artwork.WorkStatus;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionRepository;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.exhibition.ExhibitionRepository;
import com.project.ArtRari.exhibition.ExhibitionStatus;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataInitializer {
    private final UserRepository userRepository;
    private final AuctionRepository auctionRepository;
    private final ArtworkRepository artworkRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final LotRepository lotRepository;
    private final BidRepository bidRepository;

    public Pair<User, UserDetailsImpl> createUser(String unique, Role role) {
        User user = new User();
        user.setFirstName("User");
        user.setLastName("Test" + unique);
        user.setEmail(unique + "@gmail.com");
        user.setPhone(unique);
        user.setPassword("password");
        user.setRole(role);
        user.setBanned(false);
        User savedUser = userRepository.save(user);
        UserDetailsImpl udi = new UserDetailsImpl(savedUser.getId(), "User", "Test" + unique,
                unique, unique + "@gmail.com", "password", role, false);
        return new Pair<>(user, udi);
    }

    public Lot createAndSaveLot() {
        Pair<User, UserDetailsImpl> artworkOwner = createUser("owner", Role.user);

        Artwork artwork = new Artwork();;
        artwork.setTitle("Title");
        artwork.setOwner(artworkOwner.a);
        artwork.setDescription("Description");
        artwork.setTechnique("Technique");
        artwork.setStatus(WorkStatus.available);
        artwork.setAuthor("Author");
        artwork.setTags(List.of());
        artwork.setCreationDate(LocalDate.now());
        artwork.setPhotoUrl("https://photo.com");
        artwork.setStartPrice(new BigDecimal("1.1"));
        artworkRepository.save(artwork);

        Pair<User, UserDetailsImpl> curator = createUser("curator", Role.curator);

        Exhibition testExhibition = new Exhibition();
        testExhibition.setArtworks(List.of(artwork));
        testExhibition.setCurator(curator.a);
        testExhibition.setTitle("Exhibition");
        testExhibition.setTheme("Theme");
        testExhibition.setDescription("Description");
        testExhibition.setBackgroundUrl("https://photo.com");
        testExhibition.setStartDate(Instant.now());
        testExhibition.setStatus(ExhibitionStatus.converted_into_auction);
        testExhibition.setThumbnailUrl("https://photo.com");
        exhibitionRepository.save(testExhibition);

        Auction testAuction = new Auction();
        testAuction.setExhibition(testExhibition);
        Instant now = Instant.now();
        testAuction.setStartDate(now);
        testAuction.setEndDate(now.plusSeconds(600));
        testAuction.setStep(new BigDecimal("1.0"));
        testAuction.setStatus(AuctionStatus.active);
        Auction savedAuction = auctionRepository.save(testAuction);

        Lot testLot = new Lot();
        testLot.setArtwork(artwork);
        testLot.setCurrentPrice(new BigDecimal("1.1"));
        testLot.setEndDate(now.plusSeconds(600));
        testLot.setBids(List.of());
        testLot.setAuction(savedAuction);
        testLot.setStatus(LotStatus.available);
        Lot savedLot = lotRepository.save(testLot);

        Pair<User, UserDetailsImpl> bidder = createUser("bidder", Role.user);
        Bid bid1 = new Bid();
        bid1.setUser(bidder.a);
        bid1.setLot(testLot);
        bid1.setAmount(new BigDecimal("1.1"));
        bid1.setCreatedAt(Instant.now());
        bid1.setWin(false);
        bidRepository.save(bid1);

        return savedLot;
    }

    public void cleanUp() {
        bidRepository.deleteAllInBatch();
        lotRepository.deleteAllInBatch();
        auctionRepository.deleteAllInBatch();
        exhibitionRepository.deleteAllInBatch();
        artworkRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }
}
