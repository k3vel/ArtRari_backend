package com.project.ArtRari.bid;

import com.project.ArtRari.common.TestDataInitializer;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

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
/*
    @BeforeEach
    public void setUp() {
        User user = testDataInitializer.createAndSaveUser("user", Role.user).a;
        Artwork artwork = testDataInitializer.createAndSaveArtwork(user);
        Exhibition exhibiton = testDataInitializer.createAndSaveExhibition(artwork);
        Auction auction = testDataInitializer.createAndSaveAuction(exhibiton);
        savedLot = testDataInitializer.createAndSaveLot(artwork, auction);
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
    }*/
}
