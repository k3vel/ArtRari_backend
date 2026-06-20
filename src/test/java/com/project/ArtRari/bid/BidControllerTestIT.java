package com.project.ArtRari.bid;

import com.project.ArtRari.bid.dto.BidPlaceRequest;
import com.project.ArtRari.lot.Lot;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        Pair<User, UserDetailsImpl> guest = testDataInitializer.createUser("guest", Role.user);
        mockMvc.perform(get("/lots/" + savedLot.getId() + "/bids")
                        .with(user(guest.b)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].amount").value(new BigDecimal("1.1")));
    }

    @Test
    public void placeBid_ShouldReturn201() throws Exception {
        Pair<User, UserDetailsImpl> bidderA = testDataInitializer.createUser("bidderA", Role.user);
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
        Pair<User, UserDetailsImpl> bidderB = testDataInitializer.createUser("bidderB", Role.user);
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("1.2"));
        String json = jsonMapper.writeValueAsString(request);
        mockMvc.perform(post("/lots/" + savedLot.getId() + "/bids")
                        .with(user(bidderB.b))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
