package com.project.ArtRari.lot;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class LotControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private UserDetailsImpl testUdi;

    @BeforeEach
    public void setUp() {
        Pair<User, UserDetailsImpl> pair = testDataInitializer.createAndSaveUser("abc", Role.user);
        testUdi = pair.b;
    }

    @Test
    public void getLot_ShouldReturn200() throws Exception {
        User owner = testDataInitializer.createAndSaveUser("owner", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(owner);
        User curator = testDataInitializer.createAndSaveUser("curator", Role.curator).a;
        Exhibition exhibition = testDataInitializer.createAndSaveExhibition(artwork, curator);
        Auction auction = testDataInitializer.createAndSaveAuction(exhibition);
        Lot savedLot = testDataInitializer.createAndSaveLot(artwork, auction);
        mockMvc.perform(get("/lots/" + savedLot.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artwork.title").value("Title"));
    }

    @Test
    public void getLot_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/lots/999")
                        .with(user(testUdi)))
                .andExpect(status().isNotFound());
    }

}
