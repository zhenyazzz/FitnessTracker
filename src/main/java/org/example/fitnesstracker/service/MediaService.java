package org.example.fitnesstracker.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;
import org.example.fitnesstracker.repository.MediaRepository;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.example.fitnesstracker.exception.AccessDeniedException;
import org.example.fitnesstracker.exception.MediaNotFoundException;
import org.example.fitnesstracker.mapper.MediaMapper;
import org.example.fitnesstracker.security.UserDetailsImpl;
import org.example.fitnesstracker.security.SecurityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService {
    private final MediaRepository mediaRepository;
    private final MediaMapper mediaMapper;
    private final SecurityUtils securityUtils;

    public List<MediaResponse> getAllMedia() {

        return mediaRepository.findAll().stream()
            .map(mediaMapper::toResponse)
            .collect(Collectors.toList());
    }

    public MediaResponse getMediaById(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Media media = mediaRepository.findByIdAndUserId(id, currentUserId)
            .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id + " and user id: " + currentUserId));
        return mediaMapper.toResponse(media);
    }

    public MediaResponse createMedia(MediaRequest request) {
        return null;
    }

    @Transactional
    public void deleteMedia(Long id) {
        Long currentUserId = securityUtils.getCurrentUserId();
        Media media = mediaRepository.findById(id)
            .orElseThrow(() -> new MediaNotFoundException("Media not found with id: " + id));
        if (!media.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own media");
        }
        mediaRepository.delete(media);
    }


}
