package org.example.fitnesstracker.mapper;

import org.mapstruct.Mapper;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.mapstruct.Mapping;
import org.example.fitnesstracker.model.User;


@Mapper(componentModel = "spring")
public interface MediaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "note", source = "request.note")
    @Mapping(target = "createdAt", ignore = true)
    Media toEntity(MediaRequest request, User user, String path, Long fileSize, String mimeType);


    MediaResponse toResponse(Media media, String presignedUrl);

}
