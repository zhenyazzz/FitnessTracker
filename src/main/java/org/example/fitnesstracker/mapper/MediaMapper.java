package org.example.fitnesstracker.mapper;

import org.mapstruct.Mapper;
import java.util.List;
import org.example.fitnesstracker.model.Media;
import org.example.fitnesstracker.dto.request.media.MediaRequest;
import org.example.fitnesstracker.dto.response.MediaResponse;
import org.mapstruct.Mapping;
import org.example.fitnesstracker.model.User;


@Mapper(componentModel = "spring")
public interface MediaMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    Media toEntity(MediaRequest request, User user);

    MediaResponse toResponse(Media media);

    List<MediaResponse> toResponseList(List<Media> media);
}
