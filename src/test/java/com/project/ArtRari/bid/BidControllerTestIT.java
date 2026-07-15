package com.project.ArtRari.bid;

import com.project.ArtRari.AbstractIntegrationTest;
import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.bid.dto.BidPlaceRequest;
import com.project.ArtRari.TestDataInitializer;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class BidControllerTestIT extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private Lot savedLot;

    @BeforeEach
    public void setUp() {
        User owner = testDataInitializer.createAndSaveUser("owner", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(owner);
        User curator = testDataInitializer.createAndSaveUser("curator", Role.admin).a;
        Exhibition exhibition = testDataInitializer.createAndSaveExhibition(artwork, curator);
        Auction auction = testDataInitializer.createAndSaveAuction(exhibition);
        savedLot= testDataInitializer.createAndSaveLot(artwork, auction);
    }

    @Test
    public void getBids_ShouldReturn200() throws Exception {
        User guest = testDataInitializer.createAndSaveUser("guest", Role.user).a;
        testDataInitializer.createAndSaveBid(savedLot, guest);
        mockMvc.perform(get("/lots/" + savedLot.getId() + "/bids"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal("1.1")));
    }

    @Test
    public void placeBid_ShouldReturn201() throws Exception {
        Pair<User, UserDetailsImpl> bidderA = testDataInitializer.createAndSaveUser("bidderA", Role.user);
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("2.2"));
        String json = jsonMapper.writeValueAsString(request);
        mockMvc.perform(post("/lots/" + savedLot.getId() + "/bids")
                        .with(user(bidderA.b))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(new BigDecimal("2.2")));
    }

    @Test
    public void placeBid_ShouldReturn400_WhenAmountIsSmall() throws Exception {
        Pair<User, UserDetailsImpl> bidderB = testDataInitializer.createAndSaveUser("bidderB", Role.user);
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("1.2"));
        String json = jsonMapper.writeValueAsString(request);
        mockMvc.perform(post("/lots/" + savedLot.getId() + "/bids")
                        .with(user(bidderB.b))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
