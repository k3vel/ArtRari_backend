package com.project.ArtRari.bid;

import com.project.ArtRari.bid.dto.BidPlaceRequest;
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
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        savedLot = testDataInitializer.createAndSaveLot();
    }

    @Test
    public void placeBid_ShouldHandleConcurrentBidsCorrectly() throws Exception {
        int nbids = 10;
        Pair<User, UserDetailsImpl> bidder1 = testDataInitializer.createUser("bidder1", Role.user);

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

    @AfterEach
    public void tearDown() {
        testDataInitializer.cleanUp();
    }
}
