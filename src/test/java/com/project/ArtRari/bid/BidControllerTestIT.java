package com.project.ArtRari.bid;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
/*
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
public class BidControllerTestIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JsonMapper jsonMapper;
    @Autowired
    private TestDataInitializer testDataInitializer;

    private Lot savedLot;

    @BeforeEach
    public void setUp() {
        savedLot = testDataInitializer.createAndSaveLot();
    }

    @Test
    public void getBids_ShouldReturn200() throws Exception {
        Pair<User, UserDetailsImpl> guest = testDataInitializer.createAndSaveUser("guest", Role.user);
        mockMvc.perform(get("/lots/" + savedLot.getId() + "/bids")
                        .with(user(guest.b)))
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
*/