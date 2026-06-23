package com.project.ArtRari.artwork;

import com.project.ArtRari.artwork.dto.*;
import com.project.ArtRari.artwork.tag.TagRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArtworkServiceTest {
    @Mock
    private ArtworkRepository artworkRepository;
    @Mock
    private ArtworkMapper artworkMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private LotRepository lotRepository;

    @InjectMocks
    private ArtworkService artworkService;

    private UserDetailsImpl udiMock;
    private User userMock;
    private Artwork someonesArtwork;

    @BeforeEach
    void setUp() {
        udiMock = new UserDetailsImpl(1L, "John", "Doe", null, null,
                null, Role.user, false);
        userMock = new User();
        userMock.setId(1L);
        userMock.setFirstName("John");
        userMock.setLastName("Doe");
        someonesArtwork = new Artwork();
        someonesArtwork.setId(2L);
        someonesArtwork.setTitle("Mona Lisa");
        someonesArtwork.setOwner(new User(2L, null, null, null, null, null,
                null, false));
    }

    @Test
    void getById_ShouldThrowNotFound_WhenArtworkDoesNotExist() {
        Long fakeArtworkId = 999L;
        when(artworkRepository.findById(fakeArtworkId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> artworkService.getById(fakeArtworkId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getById_ShouldReturnArtworkResponse_WhenArtworkExists() {
        Long artworkId = 1L;
        Artwork mockArtwork = new Artwork();
        mockArtwork.setId(artworkId);
        mockArtwork.setTitle("Mona Lisa");
        ArtworkResponse mockResponse = new ArtworkResponse(
                artworkId, null, "Mona Lisa", null, null, null, null,
                null, null, null);
        when(artworkRepository.findById(artworkId)).thenReturn(Optional.of(mockArtwork));
        when(artworkMapper.toArtworkResponse(mockArtwork)).thenReturn(mockResponse);
        ArtworkResponse actualResponse = artworkService.getById(artworkId);
        assertNotNull(actualResponse);
        assertEquals(artworkId, actualResponse.id());
        assertEquals("Mona Lisa", actualResponse.title());
    }

    @Test
    void getAvailableArtworks_ShouldReturnPageResponse_WhenCalled() {
        ReflectionTestUtils.setField(artworkService, "pageSize", 10);
        int page = 0;
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Mona Lisa");
        Page<Artwork> artworkPage = new PageImpl<>(List.of(artwork));
        ArtworkAdvancedPreviewResponse dto = new ArtworkAdvancedPreviewResponse(
                1L, "Mona Lisa", null, null, null, null, null
        );
        when(artworkRepository.findByStatusAndExhibitionIsNull(
                Mockito.eq(WorkStatus.available),
                any(Pageable.class)
        )).thenReturn(artworkPage);
        when(artworkMapper.toArtworkAdvancedPreviewResponse(any(Artwork.class)))
                .thenReturn(dto);
        PageResponse<ArtworkAdvancedPreviewResponse> response = artworkService.getAvailableArtworks(page);
        assertNotNull(response);
        assertFalse(response.getItems().isEmpty());
        assertEquals(1L, response.getItems().getFirst().id());
        assertEquals("Mona Lisa", response.getItems().getFirst().title());
    }

    @Test
    void addArtwork_ShouldThrowNotFound_WhenUserDoesNotExist() {
        Long userId = 5L;
        UserDetailsImpl udiAny = mock(UserDetailsImpl.class);
        ArtworkCreateRequest request = mock(ArtworkCreateRequest.class);
        when(udiAny.getId()).thenReturn(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> artworkService.addArtwork(request, udiAny)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void addArtwork_ShouldReturnArtworkResponse() {
        ArtworkCreateRequest request = new ArtworkCreateRequest("Mona Lisa", null, null,
                null, List.of(), null, null, null);
        Artwork savedArtwork = new Artwork();
        savedArtwork.setId(1L);
        savedArtwork.setTitle("Mona Lisa");
        ArtworkResponse savedArtworkResponse = new ArtworkResponse(1L, null, "Mona Lisa", null,
                null, null, List.of(), null, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(userMock));
        when(artworkRepository.save(any(Artwork.class))).thenReturn(savedArtwork);
        when(artworkMapper.toArtworkResponse(savedArtwork)).thenReturn(savedArtworkResponse);
        ArtworkResponse actualResponse = artworkService.addArtwork(request, udiMock);
        verify(artworkRepository, times(1)).save(any(Artwork.class));
        assertNotNull(actualResponse);
        assertEquals(1L, actualResponse.id());
        assertEquals("Mona Lisa", actualResponse.title());
    }

    @Test
    void updateArtwork_ShouldThrowNotFound_WhenWorkDoesNotExist() {
        ArtworkUpdateRequest request = mock(ArtworkUpdateRequest.class);
        when(artworkRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> artworkService.updateArtwork(1L, request, udiMock)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateArtwork_ShouldThrowForbidden_WhenUserIsNotOwner() {
        ArtworkUpdateRequest request = mock(ArtworkUpdateRequest.class);
        when(artworkRepository.findById(2L)).thenReturn(Optional.of(someonesArtwork));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> artworkService.updateArtwork(2L, request, udiMock)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void updateArtwork_ShouldThrowConflict_WhenWorkIsOnAuction() {
        ArtworkUpdateRequest request = mock(ArtworkUpdateRequest.class);
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setOwner(userMock);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(1L, LotStatus.unsold)).thenReturn(true);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> artworkService.updateArtwork(1L, request, udiMock)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void updateArtwork_ShouldReturnArtworkResponse() {
        ArtworkUpdateRequest request = new ArtworkUpdateRequest("New title", null, null,
                null, List.of(), null, null, null);
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Old title");
        artwork.setOwner(userMock);
        ArtworkResponse savedArtworkResponse = new ArtworkResponse(1L, null, "New title", null,
                null, null, List.of(), null, null, null);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(1L, LotStatus.unsold)).thenReturn(false);
        when(artworkRepository.save(any(Artwork.class))).thenReturn(artwork);
        when(artworkMapper.toArtworkResponse(artwork)).thenReturn(savedArtworkResponse);
        ArtworkResponse actualResponse = artworkService.updateArtwork(1L, request, udiMock);
        verify(artworkRepository, times(1)).save(any(Artwork.class));
        assertNotNull(actualResponse);
        assertEquals("New title", artwork.getTitle());
    }

    @Test
    void deleteArtwork_ShouldThrowNotFound_WhenWorkDoesNotExist() {
        when(artworkRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> artworkService.deleteArtwork(1L, udiMock)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void deleteArtwork_ShouldThrowForbidden_WhenUserIsNotOwner() {
        when(artworkRepository.findById(2L)).thenReturn(Optional.of(someonesArtwork));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> artworkService.deleteArtwork(2L, udiMock)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void deleteArtwork_ShouldThrowConflict_WhenWorkIsOnAuction() {
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setOwner(userMock);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(1L, LotStatus.unsold)).thenReturn(true);
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> artworkService.deleteArtwork(1L, udiMock)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
    }

    @Test
    void deleteArtwork_ShouldDeleteSuccessfully() {
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setOwner(userMock);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(lotRepository.existsByArtworkIdAndStatusNot(1L, LotStatus.unsold)).thenReturn(false);
        artworkService.deleteArtwork(1L, udiMock);
        verify(artworkRepository, times(1)).delete(artwork);
    }

    @Test
    void getMyArtworks_ShouldReturnPageResponse() {
        ReflectionTestUtils.setField(artworkService, "pageSize", 10);
        int page = 0;
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Mona Lisa");
        Page<Artwork> artworkPage = new PageImpl<>(List.of(artwork));
        ArtworkPreviewResponse dto = new ArtworkPreviewResponse(
                1L, "Mona Lisa", List.of(), null);
        when(artworkRepository.findByOwnerId(eq(1L), any(Pageable.class) )).thenReturn(artworkPage);
        when(artworkMapper.toArtworkPreviewResponse(artwork)).thenReturn(dto);
        PageResponse<ArtworkPreviewResponse> response = artworkService.getMyArtworks(0, udiMock);
        assertNotNull(response);
        assertFalse(response.getItems().isEmpty());
        assertEquals(1L, response.getItems().getFirst().id());
        assertEquals("Mona Lisa", response.getItems().getFirst().title());
    }

    @Test
    void getMyArtwork_ShouldThrowNotFound_WhenArtworkDoesNotExist() {
        when(artworkRepository.findById(1L)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> artworkService.getMyArtwork(1L, udiMock)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getMyArtwork_ShouldThrowForbidden_WhenUserIsNotOwner() {
        when(artworkRepository.findById(2L)).thenReturn(Optional.of(someonesArtwork));
        ArtrariException exception = assertThrows(
                ArtrariException.class,
                () -> artworkService.getMyArtwork(2L, udiMock)
        );
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void getMyArtwork_ShouldReturnArtworkResponse() {
        Artwork artwork = new Artwork();
        artwork.setId(1L);
        artwork.setTitle("Mona Lisa");
        artwork.setOwner(userMock);
        MyArtworkResponse response = new MyArtworkResponse(1L, null, "Mona Lisa", null,
                null, null, List.of(), null, null, null, null);
        when(artworkRepository.findById(1L)).thenReturn(Optional.of(artwork));
        when(artworkMapper.toMyArtworkResponse(artwork)).thenReturn(response);
        MyArtworkResponse actualResponse = artworkService.getMyArtwork(1L, udiMock);
        assertNotNull(actualResponse);
        assertEquals("Mona Lisa", actualResponse.title());
    }
}
