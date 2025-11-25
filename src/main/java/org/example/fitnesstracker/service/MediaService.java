package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.example.fitnesstracker.repository.MediaRepository;
import org.example.fitnesstracker.repository.UserRepository;
import org.example.fitnesstracker.repository.specification.MediaSpecifications;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.model.User;
import org.example.fitnesstracker.dto.request.media.MediaFilterDto;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.exception.AccessDeniedException;
import org.example.fitnesstracker.exception.MediaNotFoundException;
import org.example.fitnesstracker.exception.UserNotFoundException;
import org.example.fitnesstracker.mapper.MediaMapper;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final UserRepository userRepository;
    private final MinioService minioService;

    @Transactional(readOnly = true)
    public Page<MediaResponse> getAllMedia(
        MediaFilterDto filter,
        Pageable pageable
    ) {
        Specification<Media> specification = buildSpecification(filter);

        Page<Media> mediaPage = mediaRepository.findAll(specification, pageable);
        return mediaPage.map(media -> {
            String url = minioService.generateFileUrl(media.getPath());
            return mediaMapper.toResponse(media, url);
        });
    }

    private Specification<Media> buildSpecification(MediaFilterDto filter) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Specification<Media> specification = MediaSpecifications.belongsToUser(currentUserId);
        
        if (filter != null && filter.dateFilter() != null) {
            specification = specification
                .and(MediaSpecifications.hasDateFrom(filter.dateFilter().dateFrom()))
                .and(MediaSpecifications.hasDateTo(filter.dateFilter().dateTo()));
        }
        return specification;
    }

    @Transactional(readOnly = true)
    public MediaResponse getMediaById(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Media media = mediaRepository.findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id + " and user id: " + currentUserId));
        String url = minioService.generateFileUrl(media.getPath());
        return mediaMapper.toResponse(media, url);
    }

    @Transactional
    public MediaResponse createMedia(MediaRequest request, MultipartFile file) {
        Long currentUserId = SecurityUtils.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("User not found with id: " + currentUserId));

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String path = minioService.uploadFile(file);

        Media media = mediaMapper.toEntity(request, user, path, file.getSize(), file.getContentType());

        Media savedMedia = mediaRepository.save(media);

        String url = minioService.generateFileUrl(savedMedia.getPath());
        return mediaMapper.toResponse(savedMedia, url);
    }

    @Transactional
    public void deleteMedia(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        Media media = mediaRepository.findById(id)
            .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));
        if (!media.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own media");
        }

        minioService.deleteFile(media.getPath());

        mediaRepository.delete(media);
    }

}
