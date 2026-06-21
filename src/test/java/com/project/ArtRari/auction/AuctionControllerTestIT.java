package com.project.ArtRari.auction;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.dto.AuctionCreateRequest;
import com.project.ArtRari.auction.dto.AuctionUpdateRequest;
import com.project.ArtRari.common.SkipBeforeEach;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class AuctionControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private AuctionRepository auctionRepository;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Auction savedAuction;
    private AuctionUpdateRequest auctionUpdateRequest;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        Pair<User,UserDetailsImpl> pair = testDataInitializer.createAndSaveUser("test", Role.curator);
        testUser = pair.a;
        testUdi = pair.b;

        boolean shouldSkip = testInfo.getTestMethod()
                .map(method -> method.isAnnotationPresent(SkipBeforeEach.class))
                .orElse(false);
        if (shouldSkip) {
            return;
        }

        User artworkOwner = testDataInitializer.createAndSaveUser("abc", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(artworkOwner);
        Exhibition exhibition = testDataInitializer.createAndSaveExhibition(artwork, testUser);
        savedAuction = testDataInitializer.createAndSaveAuction(exhibition);
        testDataInitializer.createAndSaveLot(artwork, savedAuction);

        auctionUpdateRequest = new AuctionUpdateRequest(Instant.now().plusSeconds(60), Instant.now().plusSeconds(660),
                new BigDecimal("2.0"));
    }

    @Test
    public void getAuctionsWithEmptyTags_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/auctions?page=0")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }

    @Test
    public void getAuction_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/auctions/{id}", savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Exhibition"))
                .andExpect(jsonPath("$.lotPreviews[0].title").value("Title"));
    }

    @Test
    @SkipBeforeEach
    public void createAuction_ShouldReturn201() throws Exception {
        User user = testDataInitializer.createAndSaveUser("abc", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(user);
        Exhibition exhibition = testDataInitializer.createAndSaveExhibition(artwork, testUser);
        AuctionCreateRequest auctionCreateRequest = new AuctionCreateRequest(exhibition.getId(),
                Instant.now().plusSeconds(60), new BigDecimal("1.0"), Instant.now().plusSeconds(660));
        String request = jsonMapper.writeValueAsString(auctionCreateRequest);

        mockMvc.perform(post("/auctions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Exhibition"))
                .andExpect(jsonPath("$.lotPreviews[0].title").value("Title"));
        List<Auction> auctions = auctionRepository.findAll();
        assertEquals(1, auctions.size());
        assertEquals("Exhibition", auctions.getFirst().getExhibition().getTitle());
    }

    @Test
    void createAuction_ShouldReturn404_WhenExhibitionDoesNotExist() throws Exception {
        AuctionCreateRequest wrongRequest = new AuctionCreateRequest(999L,
                Instant.now().plusSeconds(60), new BigDecimal("1.0"), Instant.now().plusSeconds(660));
        String request = jsonMapper.writeValueAsString(wrongRequest);

        mockMvc.perform(post("/auctions")
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAuction_ShouldReturn200() throws Exception {
        savedAuction.setStatus(AuctionStatus.scheduled);
        auctionRepository.save(savedAuction);
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);
        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(status().isOk());
        Auction auction = auctionRepository.findById(savedAuction.getId()).get();
        assertEquals(BigDecimal.valueOf(2.0), auction.getStep());
    }

    @Test
    void updateAuction_ShouldReturn401_WhenWrongCurator() throws Exception {
        UserDetailsImpl wrongCurator = testDataInitializer.createAndSaveUser("wrong", Role.curator).b;
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);

        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(wrongCurator))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateAuction_ShouldReturn409_WhenAuctionAlreadyRunning() throws Exception {
        String request = jsonMapper.writeValueAsString(auctionUpdateRequest);
        mockMvc.perform(put("/auctions/" + savedAuction.getId())
                        .with(user(testUdi))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteAuction_ShouldReturn204() throws Exception {
        savedAuction.setStatus(AuctionStatus.scheduled);
        auctionRepository.save(savedAuction);
        mockMvc.perform(delete("/auctions/" + savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuction_ShouldReturn409_WhenAuctionAlreadyRunning() throws Exception {
        mockMvc.perform(delete("/auctions/" + savedAuction.getId())
                        .with(user(testUdi)))
                .andExpect(status().isConflict());
    }

    @Test
    void getMyAuctions_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/auctions/my")
                        .with(user(testUdi)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.items[0].title").value("Exhibition"));
    }
}
