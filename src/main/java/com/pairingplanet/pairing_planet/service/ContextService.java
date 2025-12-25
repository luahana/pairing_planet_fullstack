package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextDimension;
import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import com.pairingplanet.pairing_planet.dto.context.ContextDto.*;
import com.pairingplanet.pairing_planet.repository.context.ContextDimensionRepository;
import com.pairingplanet.pairing_planet.repository.context.ContextTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContextService {

    private final ContextDimensionRepository dimensionRepository;
    private final ContextTagRepository tagRepository;

    // --- Dimension Logic ---

    @Transactional
    public DimensionResponse createDimension(DimensionRequest request) {
        if (dimensionRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Dimension already exists: " + request.name());
        }

        ContextDimension dimension = ContextDimension.builder()
                .name(request.name())
                .build();

        ContextDimension saved = dimensionRepository.save(dimension);
        return new DimensionResponse(saved.getPublicId(), saved.getName());
    }

    public List<DimensionResponse> getAllDimensions() {
        return dimensionRepository.findAll().stream()
                .map(d -> new DimensionResponse(d.getPublicId(), d.getName()))
                .collect(Collectors.toList());
    }

    // --- Tag Logic ---

    @Transactional
    public TagResponse createTag(UUID dimensionPublicId, TagRequest request) {
        ContextDimension dimension = dimensionRepository.findByPublicId(dimensionPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Dimension not found"));

        ContextTag tag = ContextTag.builder()
                .dimension(dimension)
                .tagName(request.tagName())
                .displayName(request.displayName())
                .locale(request.locale())
                .displayOrder(request.displayOrder() != null ? request.displayOrder() : 0)
                .build();

        ContextTag saved = tagRepository.save(tag);

        return new TagResponse(
                saved.getPublicId(),
                saved.getTagName(),
                saved.getDisplayName(),
                saved.getLocale(),
                saved.getDisplayOrder(),
                dimension.getPublicId()
        );
    }

    public List<TagResponse> getTagsByLocale(String locale) {
        return tagRepository.findAllByLocaleOrderByDisplayOrderAsc(locale).stream()
                .map(t -> new TagResponse(
                        t.getPublicId(),
                        t.getTagName(),
                        t.getDisplayName(),
                        t.getLocale(),
                        t.getDisplayOrder(),
                        t.getDimension().getPublicId()))
                .collect(Collectors.toList());
    }

    public List<TagResponse> getTagsByDimensionAndLocale(UUID dimensionPublicId, String locale) {
        ContextDimension dimension = dimensionRepository.findByPublicId(dimensionPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Dimension not found"));

        return tagRepository.findAllByDimensionIdAndLocaleOrderByDisplayOrderAsc(dimension.getId(), locale).stream()
                .map(t -> new TagResponse(
                        t.getPublicId(),
                        t.getTagName(),
                        t.getDisplayName(),
                        t.getLocale(),
                        t.getDisplayOrder(),
                        dimension.getPublicId()))
                .collect(Collectors.toList());
    }
}