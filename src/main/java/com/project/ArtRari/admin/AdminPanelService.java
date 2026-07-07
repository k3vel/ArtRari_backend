package com.project.ArtRari.admin;

import com.project.ArtRari.artwork.*;
import com.project.ArtRari.artwork.dto.ArtworkAdvancedPreviewResponse;
import com.project.ArtRari.auction.AuctionStatus;
import com.project.ArtRari.bid.Bid;
import com.project.ArtRari.bid.BidRepository;
import com.project.ArtRari.common.PageResponse;
import com.project.ArtRari.exception.ArtrariException;
import com.project.ArtRari.exhibition.*;
import com.project.ArtRari.exhibition.dto.ExhibitionPreviewResponse;
import com.project.ArtRari.lot.Lot;
import com.project.ArtRari.lot.LotRepository;
import com.project.ArtRari.lot.LotStatus;
import com.project.ArtRari.security.UserDetailsImpl;
import com.project.ArtRari.user.Role;
import com.project.ArtRari.user.User;
import com.project.ArtRari.user.UserMapper;
import com.project.ArtRari.user.UserRepository;
import com.project.ArtRari.user.dto.ProfileResponse;
import com.project.ArtRari.user.dto.UserPreviewResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminPanelService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ArtworkRepository artworkRepository;
    private final ArtworkMapper artworkMapper;
    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionMapper exhibitionMapper;
    private final LotRepository lotRepository;
    private final BidRepository bidRepository;

    @Value("${app.pagination.default-size}")
    private int pageSize;

    public PageResponse<UserPreviewResponse> getAllUsers(String search, int page, UserDetailsImpl udi) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("lastName"));
        Page<User> users;
        if (search == null || search.isBlank()) {
            users = userRepository.findByIdNot(udi.getId(), pageable);
        } else {
            users = userRepository.findAllBySearchAndIdNot(search, udi.getId(), pageable);
        }
        Page<UserPreviewResponse> response = users.map(u -> userMapper.toUserPreviewResponse(u));
        return new PageResponse<>(response);
    }

    public ProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public void setBanned(Long userId, boolean isBanned, UserDetailsImpl udi) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (user.getId().equals(udi.getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете банити самого себе");
        }
        if (user.getRole().equals(Role.admin)) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете банити адміністратора");
        }
        user.setBanned(isBanned);
    }

    @Transactional
    public void changeRole(Long userId, String role, UserDetailsImpl udi) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (user.getId().equals(udi.getId())) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете змінювати роль самому собі");
        }
        if (user.getRole().equals(Role.admin)) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете змінювати роль адміністратора");
        }
        switch (role) {
            case "user": user.setRole(Role.user); break;
            case "curator": user.setRole(Role.curator); break;
            case "admin": user.setRole(Role.admin); break;
            default: throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    public PageResponse<ArtworkAdvancedPreviewResponse> getAllArtworks(String search, List<String> tags, int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "status")
                .and(Sort.by(Sort.Direction.DESC, "creationDate"));
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Specification<Artwork> spec = (table, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (search != null && !search.isBlank()) {
            spec = spec.and(ArtworkSpecifications.hasText(search));
        }
        if (tags != null && !tags.isEmpty()) {
            spec = spec.and(ArtworkSpecifications.hasTags(tags));
        }

        Page<Artwork> artworks = artworkRepository.findAll(spec, pageable);
        Page<ArtworkAdvancedPreviewResponse> aprs = artworks.map(
                a -> artworkMapper.toArtworkAdvancedPreviewResponse(a)
        );
        return new PageResponse<>(aprs);
    }

    @Transactional
    public void deleteArtwork(Long id) {
        Artwork artwork = artworkRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (artwork.getStatus().equals(WorkStatus.sold)) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете видалити роботу, яка була продана");
        }
        if (lotRepository.existsByArtworkIdAndStatusNot(artwork.getId(), LotStatus.unsold)) {
            throw new ArtrariException(HttpStatus.CONFLICT, "Ви не можете видалити роботу, якщо вона на аукціоні");
        }
        lotRepository.deleteByArtworkId(artwork.getId());
        artworkRepository.delete(artwork);
    }

    public PageResponse<ExhibitionPreviewResponse> getAllExhibitions(String search, List<String> tags, int page) {
        Sort sort = Sort.by(Sort.Direction.ASC, "status")
                .and(Sort.by(Sort.Direction.DESC, "startDate"));
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Specification<Exhibition> spec = (table, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        if (search != null && !search.isBlank()) {
            spec = spec.and(ExhibitionSpecifications.hasText(search));
        }
        if (tags != null && !tags.isEmpty()) {
            spec = spec.and(ExhibitionSpecifications.hasTags(tags));
        }
        Page<Exhibition> exhibitions = exhibitionRepository.findAll(spec, pageable);
        Page<ExhibitionPreviewResponse> eprs = exhibitions.map(exhibitionMapper::toExhibitionPreviewResponse);
        return new PageResponse<>(eprs);
    }

    @Transactional
    public void deleteExhibition(Long id) {
        Exhibition exhibition = exhibitionRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (exhibition.getStatus() == ExhibitionStatus.converted_into_auction) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете видалити виставку, яку перетворено на аукціон");
        }
        for (Artwork artwork : exhibition.getArtworks()) {
            artwork.setExhibition(null);
        }
        exhibitionRepository.delete(exhibition);
    }

    @Transactional
    public void cancelLot(Long id) {
        Lot lot = lotRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (lot.getStatus() != LotStatus.available) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете скасувати неактивний лот");
        }
        lot.setStatus(LotStatus.cancelled);
        lot.getArtwork().setExhibition(null);
    }

    @Transactional
    public void deleteBid(Long id) {
        Bid bid = bidRepository.findWithLotAndAuctionById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );
        if (bid.getLot().getAuction().getStatus() != AuctionStatus.active) {
            throw new ArtrariException(HttpStatus.FORBIDDEN, "Ви не можете видалити ставку, якщо аукціон неактивний");
        }
        Lot lot = bid.getLot();
        bidRepository.delete(bid);
        bidRepository.flush();

        Optional<Bid> maxBid = bidRepository.findTopByLotIdOrderByAmountDesc(lot.getId());
        if (maxBid.isPresent()) {
            lot.setCurrentPrice(maxBid.get().getAmount());
        } else {
            lot.setCurrentPrice(lot.getArtwork().getStartPrice());
        }
    }
}
