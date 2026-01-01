package com.pairingplanet.pairing_planet.dto.post.recipe;

import com.pairingplanet.pairing_planet.domain.enums.IngredientType;

public record IngredientRequestDto(String name, String amount, IngredientType type) {}