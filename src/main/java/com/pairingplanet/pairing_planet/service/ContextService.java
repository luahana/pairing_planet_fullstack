package com.pairingplanet.pairing_planet.service;

import com.pairingplanet.pairing_planet.domain.entity.context.ContextDimension;
import com.pairingplanet.pairing_planet.domain.entity.context.ContextTag;
import com.pairingplanet.pairing_planet.dto.context.ContextDto.*;
import com.pairingplanet.pairing_planet.repository.context.ContextDimensionRepository;
import com.pairingplanet.pairing_planet.repository.context.ContextTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContextService {

    private final ContextDimensionRepository dimensionRepository;
    private final ContextTagRepository tagRepository;

    @Transactional
    public DimensionResponse createDimension(DimensionRequest request) {
        if (dimensionRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Dimension already exists");
        }
        ContextDimension saved = dimensionRepository.save(ContextDimension.builder().name(request.name()).build());
        return new DimensionResponse(saved.getPublicId(), saved.getName());
    }

    public List<DimensionResponse> getAllDimensions() {
        return dimensionRepository.findAll().stream()
                .map(d -> new DimensionResponse(d.getPublicId(), d.getName()))
                .toList();
    }

    @Transactional
    public TagResponse createTag(UUID dimensionPublicId, TagRequest request) {
        ContextDimension dimension = dimensionRepository.findByPublicId(dimensionPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Dimension not found"));

        ContextTag tag = ContextTag.builder()
                .dimension(dimension)
                .tagName(request.tagName())
                .displayNames(request.displayNames())
                .displayOrders(request.displayOrders())
                .build();

        ContextTag saved = tagRepository.save(tag);

        // 기본값으로 'en' 사용 시연
        return new TagResponse(
                saved.getPublicId(), saved.getTagName(),
                saved.getDisplayNameByLocale("en"),
                saved.getOrderByLocale("en"),
                dimension.getPublicId()
        );
    }

    public List<TagResponse> getTagsByLocale(String locale) {
        return tagRepository.findAll().stream()
                .sorted(Comparator.comparing(t -> t.getOrderByLocale(locale))) // 언어별 정렬 적용
                .map(t -> mapToResponse(t, locale))
                .collect(Collectors.toList());
    }

    public List<TagResponse> getTagsByDimensionAndLocale(UUID dimensionPublicId, String locale) {
        ContextDimension dimension = dimensionRepository.findByPublicId(dimensionPublicId)
                .orElseThrow(() -> new IllegalArgumentException("Dimension not found"));

        return tagRepository.findAllByDimensionId(dimension.getId()).stream()
                .sorted(Comparator.comparing(t -> t.getOrderByLocale(locale))) // 언어별 정렬 적용
                .map(t -> mapToResponse(t, locale))
                .collect(Collectors.toList());
    }

    private TagResponse mapToResponse(ContextTag tag, String locale) {
        return new TagResponse(
                tag.getPublicId(),
                tag.getTagName(),
                tag.getDisplayNameByLocale(locale),
                tag.getOrderByLocale(locale),
                tag.getDimension().getPublicId()
        );
    }
}