package com.project.ArtRari.exhibition;

import com.project.ArtRari.artwork.Artwork;
import com.project.ArtRari.artwork.ArtworkRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.dto.*;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExhibitionServiceTest {
    @Mock
    private ExhibitionRepository exhibitionRepository;
    @Mock
    private ExhibitionMapper exhibitionMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArtworkRepository artworkRepository;
    @InjectMocks
    private ExhibitionService exhibitionService;

    private User testUser;
    private UserDetailsImpl testUdi;
    private Exhibition exhibition;
    private Artwork artwork;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(exhibitionService, "pageSize", 10);

        testUdi = new UserDetailsImpl(1L, "John", "Doe", "mail", "pass",
                "123", Role.curator, false);

        testUser = new User();
        testUser.setId(1L);

        artwork = new Artwork();
        artwork.setId(10L);
        artwork.setStartPrice(new BigDecimal("100.00"));

        exhibition = new Exhibition();
        exhibition.setId(100L);
        exhibition.setCurator(testUser);
        exhibition.setStatus(ExhibitionStatus.running);
        exhibition.setArtworks(List.of(artwork));
    }

    @Test
    void getById_ShouldReturnExhibition() {
        when(exhibitionRepository.findByIdAndStatus(eq(100L), eq(ExhibitionStatus.running)))
                .thenReturn(Optional.of(exhibition));
        ExhibitionResponse mockResponse = Mockito.mock(ExhibitionResponse.class);
        when(exhibitionMapper.toExhibitionResponse(exhibition)).thenReturn(mockResponse);
        ExhibitionResponse res = exhibitionService.getById(100L);

        assertNotNull(res);
        verify(exhibitionMapper).toExhibitionResponse(exhibition);
    }

    @Test
    void getById_ShouldReturn404_WhenNotFound() {
        when(exhibitionRepository.findByIdAndStatus(eq(100L), eq(ExhibitionStatus.running)))
                .thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.getById(100L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getExhibitions_ShouldReturnExhibitions_WhenTagsAreEmpty() {
        Page<Exhibition> exhibitionPage = new PageImpl<>(List.of(exhibition));
        when(exhibitionRepository.findByStatus(eq(ExhibitionStatus.running), any(Pageable.class)))
                .thenReturn(exhibitionPage);
        ExhibitionPreviewResponse mockResponse = Mockito.mock(ExhibitionPreviewResponse.class);
        when(exhibitionMapper.toExhibitionPreviewResponse(exhibition)).thenReturn(mockResponse);
        PageResponse<ExhibitionPreviewResponse> res = exhibitionService.getExhibitions(0, List.of());

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        verify(exhibitionMapper).toExhibitionPreviewResponse(exhibition);
    }

    @Test
    void createExhibition_ShouldCreate() {
        Artwork anotherArtwork = new Artwork();
        anotherArtwork.setId(11L);
        anotherArtwork.setTitle("Mona Lisa");
        ExhibitionCreateRequest request = new ExhibitionCreateRequest(List.of(11L), "Exhibition", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(userRepository.getReferenceById(1L)).thenReturn(testUser);
        when(artworkRepository.findAllById(List.of(11L))).thenReturn(List.of(anotherArtwork));
        when(exhibitionRepository.save(any(Exhibition.class))).thenAnswer(invocation -> {
            Exhibition saved = invocation.getArgument(0);
            saved.setId(999L);
            return saved;
                });
        ExhibitionResponse mockResponse = Mockito.mock(ExhibitionResponse.class);
        when(exhibitionMapper.toExhibitionResponse(any(Exhibition.class))).thenReturn(mockResponse);
        ExhibitionResponse res = exhibitionService.createExhibition(request, testUdi);

        assertNotNull(res);
        ArgumentCaptor<Exhibition> saved = ArgumentCaptor.forClass(Exhibition.class);
        verify(exhibitionRepository).save(saved.capture());
        Exhibition exhibition = saved.getValue();
        assertEquals("Exhibition", exhibition.getTitle());
        assertEquals("Mona Lisa", exhibition.getArtworks().get(0).getTitle());
    }

    @Test
    void createExhibition_ShouldThrow409_WhenWorkAlreadyOnExhibition() {
        ExhibitionCreateRequest request = new ExhibitionCreateRequest(List.of(10L), "Exhibition", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(userRepository.getReferenceById(1L)).thenReturn(testUser);
        when(artworkRepository.findAllById(List.of(10L))).thenReturn(List.of(artwork));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> exhibitionService.createExhibition(request, testUdi)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void updateExhibition_ShouldUpdate() {
        ExhibitionUpdateRequest request = new ExhibitionUpdateRequest(List.of(10L), "New title", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        when(artworkRepository.findAllById(List.of(10L))).thenReturn(List.of(artwork));
        when(exhibitionRepository.save(any(Exhibition.class))).thenAnswer(invocation ->
                invocation.getArgument(0));
        ExhibitionResponse mockResponse = Mockito.mock(ExhibitionResponse.class);
        when(exhibitionMapper.toExhibitionResponse(exhibition)).thenReturn(mockResponse);
        ExhibitionResponse res = exhibitionService.updateExhibition(100L, request, testUdi);

        assertNotNull(res);
        ArgumentCaptor<Exhibition> saved = ArgumentCaptor.forClass(Exhibition.class);
        verify(exhibitionRepository).save(saved.capture());
        Exhibition exhibition = saved.getValue();
        assertEquals("New title", exhibition.getTitle());
        assertEquals(new BigDecimal("100.00"), exhibition.getArtworks().get(0).getStartPrice());
    }

    @Test
    void updateExhibition_ShouldThrow404_WhenNotFound() {
        ExhibitionUpdateRequest request = new ExhibitionUpdateRequest(List.of(10L), "New title", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.updateExhibition(100L, request, testUdi)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateExhibition_ShouldThrow403_WhenWrongCurator() {
        ExhibitionUpdateRequest request = new ExhibitionUpdateRequest(List.of(100L), "New title", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        UserDetailsImpl wrongUdi = new UserDetailsImpl(2L, "a", "b", "c", "d",
                "e", Role.curator, false);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.updateExhibition(100L, request, wrongUdi)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void updateExhibition_ShouldThrow409_WhenConvertedIntoAuction() {
        ExhibitionUpdateRequest request = new ExhibitionUpdateRequest(List.of(10L), "New title", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        exhibition.setStatus(ExhibitionStatus.converted_into_auction);
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> exhibitionService.updateExhibition(100L, request, testUdi)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void updateExhibition_ShouldThrow409_WhenWorkAlreadyOnExhibition() {
        ExhibitionUpdateRequest request = new ExhibitionUpdateRequest(List.of(11L), "New title", "Theme",
                "Description", "http://photo.com", "http://photo.com");
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        Exhibition exhibition1 = new Exhibition();
        Artwork newArtwork = new Artwork();
        newArtwork.setId(11L);
        newArtwork.setExhibition(exhibition1);
        when(artworkRepository.findAllById(List.of(11L))).thenReturn(List.of(newArtwork));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> exhibitionService.updateExhibition(100L, request, testUdi)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void deleteExhibition_ShouldDelete() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        exhibitionService.deleteExhibition(100L, testUdi);
        verify(exhibitionRepository).delete(exhibition);
    }

    @Test
    void deleteExhibition_ShouldThrow404_WhenNotFound() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.deleteExhibition(100L, testUdi)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteExhibition_ShouldThrow403_WhenWrongCurator() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        UserDetailsImpl wrongUdi = new UserDetailsImpl(2L, "a", "b", "c", "d", "e", Role.curator, false);
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.deleteExhibition(100L, wrongUdi)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void deleteExhibition_ShouldThrow409_WhenConvertedIntoAuction() {
        exhibition.setStatus(ExhibitionStatus.converted_into_auction);
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> exhibitionService.deleteExhibition(100L, testUdi)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void getMyExhibitions_ShouldReturnPage() {
        Page<Exhibition> exhibitionPage = new PageImpl<>(List.of(exhibition));
        when(exhibitionRepository.findByCuratorId(eq(1L), any(Pageable.class))).thenReturn(exhibitionPage);
        ExhibitionPreviewResponse mockResponse = Mockito.mock(ExhibitionPreviewResponse.class);
        when(exhibitionMapper.toExhibitionPreviewResponse(exhibition)).thenReturn(mockResponse);
        PageResponse<ExhibitionPreviewResponse> res = exhibitionService.getMyExhibitions(0, testUdi);

        assertNotNull(res);
        assertEquals(1, res.getTotalElements());
        verify(exhibitionRepository).findByCuratorId(eq(1L), any(Pageable.class));
    }

    @Test
    void getMyExhibitionAdvanced_ShouldReturnAdvancedResponse() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        ExhibitionAdvancedResponse mockResponse = Mockito.mock(ExhibitionAdvancedResponse.class);
        when(exhibitionMapper.toExhibitionAdvancedResponse(exhibition)).thenReturn(mockResponse);
        ExhibitionAdvancedResponse res = exhibitionService.getMyExhibitionAdvanced(100L, testUdi);

        assertNotNull(res);
        verify(exhibitionMapper).toExhibitionAdvancedResponse(exhibition);
    }

    @Test
    void getMyExhibitionAdvanced_ShouldThrow404_WhenNotFound() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> exhibitionService.getMyExhibitionAdvanced(100L, testUdi)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getMyExhibitionAdvanced_ShouldThrow403_WhenWrongCurator() {
        when(exhibitionRepository.findById(100L)).thenReturn(Optional.of(exhibition));
        UserDetailsImpl wrongUdi = new UserDetailsImpl(2L, "a", "b", "c", "d",
                "e", Role.curator, false);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> exhibitionService.getMyExhibitionAdvanced(100L, wrongUdi)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }


}
