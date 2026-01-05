import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/core/constants/api_constants.dart';
import 'package:pairing_planet2_frontend/core/widgets/app_cached_image.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/ingredient_dto.dart';
import 'package:pairing_planet2_frontend/domain/entities/recipe/recipe_detail.dart';
import '../../providers/recipe_providers.dart';

class RecipeDetailScreen extends ConsumerWidget {
  final String recipeId;
  const RecipeDetailScreen({super.key, required this.recipeId});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final recipeAsync = ref.watch(recipeDetailProvider(recipeId));

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        title: const Text("레시피 상세"),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          recipeAsync.when(
            data: (recipe) => TextButton(
              onPressed: () => context.push(
                // 💡 id뿐만 아니라 recipe 객체 전체를 전달합니다.
                RouteConstants.recipeCreate,
                extra: recipe,
              ),
              child: const Text(
                "변형하기",
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
            ),
            loading: () => const SizedBox.shrink(),
            error: (_, __) => const SizedBox.shrink(),
          ),
        ],
      ),
      body: recipeAsync.when(
        data: (recipe) => SingleChildScrollView(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              _buildImageHeader(recipe),
              Padding(
                padding: const EdgeInsets.all(20.0),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // 💡 1. 계보 정보 (요구사항 B-3)
                    _buildLineageTag(recipe),
                    const SizedBox(height: 12),
                    Text(
                      "[${recipe.foodName}]",
                      style: TextStyle(
                        fontSize: 18,
                        color: Colors.indigo[900],
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    Text(
                      recipe.title,
                      style: const TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      recipe.description ?? "",
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.grey[700],
                        height: 1.5,
                      ),
                    ),
                    const SizedBox(height: 24),

                    // 💡 2. 요리 시간 및 난이도 (요구사항 B-1)
                    // _buildRecipeStats(recipe),
                    const Divider(height: 48),

                    // 💡 3. 재료 목록 (MAIN / SECONDARY / SEASONING 분류)
                    const Text(
                      "준비 재료",
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    _buildIngredientSection(
                      "주재료",
                      recipe.ingredients,
                      IngredientType.MAIN,
                    ),
                    _buildIngredientSection(
                      "부재료",
                      recipe.ingredients,
                      IngredientType.SECONDARY,
                    ),
                    _buildIngredientSection(
                      "양념",
                      recipe.ingredients,
                      IngredientType.SEASONING,
                    ),

                    const Divider(height: 48),

                    // 💡 4. 조리 단계
                    const Text(
                      "조리 순서",
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 16),
                    ...recipe.steps.map((step) => _buildStepItem(step)),
                    const SizedBox(height: 50),
                  ],
                ),
              ),
            ],
          ),
        ),
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (err, stack) => Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text("데이터를 가져오지 못했습니다: $err"),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => ref.refresh(recipeDetailProvider(recipeId)),
                child: const Text("다시 시도"),
              ),
            ],
          ),
        ),
      ),
    );
  }

  // 💡 계보 태그 UI (B-3 반영)
  Widget _buildLineageTag(RecipeDetail recipe) {
    final isVariant = recipe.publicId != null;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: isVariant ? Colors.orange[50] : Colors.indigo[50],
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        isVariant ? "변형 레시피" : "오리지널 레시피",
        style: TextStyle(
          color: isVariant ? Colors.orange[800] : Colors.indigo[800],
          fontSize: 12,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  // 💡 요리 정보 통계 UI (B-1 반영)
  // Widget _buildRecipeStats(RecipeDetail recipe) {
  //   return Row(
  //     mainAxisAlignment: MainAxisAlignment.spaceAround,
  //     children: [
  //       _statItem(
  //         Icons.timer_outlined,
  //         "${recipe.cookingTime ?? '-'}분",
  //         "요리 시간",
  //       ),
  //       _statItem(Icons.bar_chart_outlined, recipe.difficulty ?? "미설정", "난이도"),
  //       _statItem(Icons.language_outlined, recipe.culinaryLocale, "국가"),
  //     ],
  //   );
  // }

  Widget _statItem(IconData icon, String value, String label) {
    return Column(
      children: [
        Icon(icon, color: Colors.indigo[900], size: 28),
        const SizedBox(height: 8),
        Text(
          value,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
        ),
        Text(label, style: TextStyle(color: Colors.grey[600], fontSize: 12)),
      ],
    );
  }

  // 💡 재료 섹션 UI
  Widget _buildIngredientSection(
    String title,
    List<dynamic> allIngredients,
    IngredientType type,
  ) {
    final items = allIngredients.where((i) => i.type == type).toList();
    if (items.isEmpty) return const SizedBox.shrink();

    return Padding(
      padding: const EdgeInsets.only(bottom: 20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.indigo[900],
            ),
          ),
          const SizedBox(height: 8),
          ...items.map(
            (i) => Padding(
              padding: const EdgeInsets.symmetric(vertical: 4),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(i.name, style: const TextStyle(fontSize: 15)),
                  Text(
                    i.amount ?? "",
                    style: TextStyle(color: Colors.grey[600]),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  // 💡 조리 단계 UI
  Widget _buildStepItem(dynamic step) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              CircleAvatar(
                radius: 12,
                backgroundColor: Colors.indigo[900],
                child: Text(
                  "${step.stepNumber}",
                  style: const TextStyle(color: Colors.white, fontSize: 12),
                ),
              ),
              const SizedBox(width: 12),
              const Text("단계", style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          const SizedBox(height: 12),
          if (step.imageUrl != null && step.imageUrl!.isNotEmpty)
            Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: AppCachedImage(
                imageUrl: step.imageUrl!,
                width: double.infinity,
                height: 200,
                borderRadius: 12,
              ),
            ),
          Text(
            step.description,
            style: const TextStyle(fontSize: 15, height: 1.6),
          ),
        ],
      ),
    );
  }

  Widget _buildImageHeader(RecipeDetail recipe) {
    return AppCachedImage(
      imageUrl: recipe.imageUrls.isNotEmpty
          ? recipe.imageUrls.first
          : 'https://via.placeholder.com/400x250',
      width: double.infinity,
      height: 300,
      borderRadius: 0,
    );
  }
}
