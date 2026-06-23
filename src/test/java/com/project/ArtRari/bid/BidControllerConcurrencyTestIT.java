package com.project.ArtRari.bid;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.auction.Auction;
import com.project.ArtRari.bid.dto.BidPlaceRequest;
import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.exhibition.Exhibition;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class BidControllerConcurrencyTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private LotRepository lotRepository;
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
    public void placeBid_ShouldHandleConcurrentBidsCorrectly() throws Exception {
        int nbids = 10;
        Pair<User, UserDetailsImpl> bidder1 = testDataInitializer.createAndSaveUser("bidder1", Role.user);

        try (ExecutorService executorService = Executors.newFixedThreadPool(nbids)) {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(nbids);

            List<BigDecimal> amounts = List.of(new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"),
                    new BigDecimal("40"), new BigDecimal("50"), new BigDecimal("60"), new BigDecimal("70"),
                    new BigDecimal("80"), new BigDecimal("90"), new BigDecimal("100"));

            for (BigDecimal amount : amounts) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        BidPlaceRequest request = new BidPlaceRequest(amount);
                        String json = jsonMapper.writeValueAsString(request);
                        mockMvc.perform(post("/lots/" + savedLot.getId() + "/bids")
                                .with(user(bidder1.b))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json));
                    } catch (Exception e) {

                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
        }

        Lot lot = lotRepository.findById(savedLot.getId()).get();
        assertEquals(new BigDecimal("100.00"), lot.getCurrentPrice());
    }

    @Test
    public void placeBid_ShouldHandleConcurrentBidsCorrectly1() throws Exception {
        int nbids = 5;
        try (ExecutorService executorService = Executors.newFixedThreadPool(nbids)) {
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(nbids);
            AtomicInteger nsuccess = new AtomicInteger(0);
            AtomicInteger nfailure = new AtomicInteger(0);

            for (int i = 0; i < nbids; i++) {
                Pair<User, UserDetailsImpl> bidder = testDataInitializer.createAndSaveUser("bidder" + i, Role.user);
                BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("10.00"));
                String json = jsonMapper.writeValueAsString(request);
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        MvcResult res = mockMvc.perform(post("/lots/" + savedLot.getId() + "/bids")
                                        .with(user(bidder.b))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(json))
                                .andReturn();
                        if (res.getResponse().getStatus() == 201) {
                            nsuccess.incrementAndGet();
                        } else {
                            nfailure.incrementAndGet();
                        }
                    } catch (Exception e) {
                        nfailure.incrementAndGet();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
            assertEquals(1, nsuccess.get());
            assertEquals(4, nfailure.get());
        }

        Lot lot = lotRepository.findById(savedLot.getId()).get();
        assertEquals(new BigDecimal("10.00"), lot.getCurrentPrice());
    }

    @AfterEach
    public void tearDown() {
        testDataInitializer.cleanUp();
    }
}
