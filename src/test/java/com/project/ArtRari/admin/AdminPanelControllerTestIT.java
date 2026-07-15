package com.project.ArtRari.admin;

import com.project.ArtRari.AbstractIntegrationTest;
import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.auction.AuctionRepository;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.Bid;
import com.project.ArtRari.TestDataInitializer;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AdminPanelControllerTestIT extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataInitializer initializer;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private LotRepository lotRepository;
    @Autowired
    private AuctionRepository auctionRepository;

    private UserDetailsImpl adminUdi;
    private User targetUser;
    private Lot testLot;
    private Artwork testArtwork;

    @BeforeEach
    void setUp() {
        Pair<User, UserDetailsImpl> adminPair = initializer.createAndSaveUser("admin", Role.admin);
        adminUdi = adminPair.b;
        Pair<User, UserDetailsImpl> userPair = initializer.createAndSaveUser("user1", Role.user);
        targetUser = userPair.a;

        testArtwork = initializer.createAndSaveArtwork(targetUser);
        Exhibition exhibition = initializer.createAndSaveExhibition(testArtwork, targetUser);
        Auction auction = initializer.createAndSaveAuction(exhibition);
        testLot = initializer.createAndSaveLot(testArtwork, auction);
    }

    @Test
    void getAllUsers_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("page", "0")
                        .with(user(adminUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].firstName").value("User"));
    }

    @Test
    void getAllUsers_ShouldReturn403_WhenNotAdmin() throws Exception {
        UserDetailsImpl simpleUserPair = initializer.createAndSaveUser("simple", Role.user).b;
        mockMvc.perform(get("/admin/users")
                        .with(user(simpleUserPair)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUser_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/admin/users/{id}", targetUser.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(targetUser.getId()))
                .andExpect(jsonPath("$.email").value(targetUser.getEmail()));
    }

    @Test
    void getUser_ShouldReturn404_WhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/admin/users/{id}", 999999L)
                        .with(user(adminUdi)))
                .andExpect(status().isNotFound());
    }

    @Test
    void changeRole_ShouldReturn200() throws Exception {
        String requestBody = """
                {
                  "role": "curator"
                }
                """;
        mockMvc.perform(patch("/admin/users/{id}/role", targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void changeRole_ShouldReturn403_WhenAdminChangesSelf() throws Exception {
        String requestBody = """
                {
                  "role": "user"
                }
                """;
        mockMvc.perform(patch("/admin/users/{id}/role", adminUdi.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(adminUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    void banUser_ShouldReturn204() throws Exception {
        String requestBody = """
                {
                  "isBanned": true
                }
                """;
        mockMvc.perform(patch("/admin/users/{id}/ban", targetUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void banUser_ShouldReturn403_WhenAdminBansAnotherAdmin() throws Exception {
        UserDetailsImpl secondAdmin = initializer.createAndSaveUser("admin2", Role.admin).b;
        String requestBody = """
                {
                  "isBanned": true
                }
                """;
        mockMvc.perform(patch("/admin/users/{id}/ban", secondAdmin.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(user(adminUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteArtwork_ShouldReturn204() throws Exception {
        Artwork freeArtwork = initializer.createAndSaveArtwork(targetUser);
        mockMvc.perform(delete("/admin/artworks/{id}", freeArtwork.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteArtwork_ShouldReturn409_WhenArtworkIsOnAuction() throws Exception {
        mockMvc.perform(delete("/admin/artworks/{id}", testArtwork.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteExhibition_ShouldReturn204() throws Exception {
        Artwork artwork = initializer.createAndSaveArtwork(targetUser);
        Exhibition runningExhibition = initializer.createAndSaveExhibition(artwork, targetUser);
        mockMvc.perform(delete("/admin/exhibitions/{id}", runningExhibition.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteExhibition_ShouldReturn403_WhenConvertedToAuction() throws Exception {
        Artwork artwork = initializer.createAndSaveArtwork(targetUser);
        Exhibition exhibition = initializer.createAndSaveExhibition(artwork, targetUser);
        initializer.createAndSaveAuction(exhibition);
        mockMvc.perform(delete("/admin/exhibitions/{id}", exhibition.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    void switchLot_ShouldReturn204() throws Exception {
        mockMvc.perform(patch("/admin/lots/{id}", testLot.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void switchLot_ShouldReturn403_WhenLotIsAlreadyCancelled() throws Exception {
        testLot.setStatus(LotStatus.cancelled);
        lotRepository.save(testLot);

        mockMvc.perform(patch("/admin/lots/{id}", testLot.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteBid_ShouldReturn204() throws Exception {
        Bid bid = initializer.createAndSaveBid(testLot, targetUser);
        mockMvc.perform(delete("/admin/bids/{id}", bid.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteBid_ShouldReturn403_WhenAuctionIsFinished() throws Exception {
        Bid bid = initializer.createAndSaveBid(testLot, targetUser);
        Auction auction = testLot.getAuction();
        auction.setStatus(AuctionStatus.finished);
        auctionRepository.save(auction);

        mockMvc.perform(delete("/admin/bids/{id}", bid.getId())
                        .with(user(adminUdi)))
                .andExpect(status().isForbidden());
    }
}
