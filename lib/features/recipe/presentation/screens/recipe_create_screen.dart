import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/ingredient_dto.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/create_recipe_request_dtos.dart';
import 'package:pairing_planet2_frontend/data/models/recipe/step_dto.dart';
import 'package:pairing_planet2_frontend/features/recipe/providers/recipe_providers.dart';
import 'package:pairing_planet2_frontend/shared/data/model/upload_item_model.dart';
import '../widgets/hook_section.dart';
import '../widgets/ingredient_section.dart';
import '../widgets/step_section.dart';

class RecipeCreateScreen extends ConsumerStatefulWidget {
  const RecipeCreateScreen({super.key});

  @override
  ConsumerState<RecipeCreateScreen> createState() => _RecipeCreateScreenState();
}

class _RecipeCreateScreenState extends ConsumerState<RecipeCreateScreen> {
  final _titleController = TextEditingController();
  final _foodNameController = TextEditingController(); // 💡 추가
  final _descriptionController = TextEditingController();
  final _localeController = TextEditingController();

  final List<Map<String, dynamic>> _ingredients = [];
  final List<Map<String, dynamic>> _steps = [];
  final List<UploadItem> _finishedImages = []; // 💡 이미지 리스트 활성화

  int? _food1MasterId; // 💡 서버 전송용 음식 ID
  bool _isLoading = false;

  @override
  void initState() {
    super.initState();
    _addIngredient();
    _addStep();
  }

  void _addIngredient() {
    setState(() {
      _ingredients.add({'name': '', 'amount': '', 'type': IngredientType.MAIN});
    });
  }

  void _addStep() {
    setState(() {
      _steps.add({
        'stepNumber': _steps.length + 1,
        'description': '',
        'imageUrl': '',
      });
    });
  }

  Future<void> _handleSubmit() async {
    // 💡 업로드 중인 이미지가 있는지 확인
    if (_finishedImages.any((img) => img.status == UploadStatus.uploading)) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('이미지 업로드가 완료될 때까지 기다려주세요.')));
      return;
    }

    setState(() => _isLoading = true);
    try {
      final requestDto = CreateRecipeRequestDto(
        title: _titleController.text,
        description: _descriptionController.text,
        culinaryLocale: _localeController.text,
        food1MasterId: _food1MasterId, // 💡 자동완성으로 받은 ID 할당
        ingredients: _ingredients
            .map(
              (i) => IngredientDto(
                name: i['name'],
                amount: i['amount'],
                type: i['type'],
              ),
            )
            .toList(),
        steps: _steps
            .map(
              (s) => StepDto(
                stepNumber: s['stepNumber'],
                description: s['description'],
              ),
            )
            .toList(),
        // 💡 성공한 이미지들의 publicId만 추출하여 전송
        imagePublicIds: _finishedImages
            .where(
              (img) =>
                  img.status == UploadStatus.success && img.publicId != null,
            )
            .map((img) => img.publicId!)
            .toList(),
      );

      final result = await ref
          .read(recipeRepositoryProvider)
          .createRecipe(requestDto);

      if (mounted) {
        result.fold(
          (failure) => ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text('등록 실패: ${failure.toString()}')),
          ),
          (_) {
            ScaffoldMessenger.of(
              context,
            ).showSnackBar(const SnackBar(content: Text('✨ 레시피가 등록되었습니다!')));
            context.pop();
          },
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text('오류 발생: $e')));
      }
    } finally {
      if (mounted) setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () => FocusScope.of(context).unfocus(),
      child: Scaffold(
        backgroundColor: Colors.white,
        appBar: _buildAppBar(),
        body: Column(
          children: [
            Expanded(
              child: SingleChildScrollView(
                physics: const BouncingScrollPhysics(),
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    // 💡 수정된 HookSection 호출부
                    HookSection(
                      titleController: _titleController,
                      foodNameController: _foodNameController,
                      descriptionController: _descriptionController,
                      finishedImages: _finishedImages,
                      onFoodIdSelected: (id) => _food1MasterId = id,
                      onStateChanged: () => setState(() {}),
                    ),
                    const SizedBox(height: 32),
                    IngredientSection(
                      ingredients: _ingredients,
                      onAddIngredient: _addIngredient,
                      onRemoveIngredient: (i) =>
                          setState(() => _ingredients.removeAt(i)),
                    ),
                    const SizedBox(height: 32),
                    StepSection(
                      steps: _steps,
                      onAddStep: _addStep,
                      onRemoveStep: (i) => setState(() => _steps.removeAt(i)),
                      onReorder: (o, n) => setState(() {
                        if (n > o) n -= 1;
                        _steps.insert(n, _steps.removeAt(o));
                      }),
                    ),
                    const SizedBox(height: 120),
                  ],
                ),
              ),
            ),
            _buildSubmitButton(),
          ],
        ),
      ),
    );
  }

  PreferredSizeWidget _buildAppBar() => AppBar(
    backgroundColor: Colors.white,
    elevation: 0,
    centerTitle: true,
    leading: IconButton(
      icon: const Icon(Icons.close, color: Colors.black),
      onPressed: () => context.pop(),
    ),
    title: const Text(
      "새 레시피 등록",
      style: TextStyle(
        color: Colors.black,
        fontSize: 18,
        fontWeight: FontWeight.bold,
      ),
    ),
  );

  Widget _buildSubmitButton() {
    final bool isReady =
        _titleController.text.isNotEmpty && _ingredients.isNotEmpty;
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 10, 20, 30),
      child: SizedBox(
        width: double.infinity,
        height: 56,
        child: ElevatedButton(
          onPressed: isReady && !_isLoading ? _handleSubmit : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: const Color(0xFF1A237E),
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(16),
            ),
          ),
          child: Text(
            _isLoading ? "등록 중..." : "레시피 등록하기",
            style: const TextStyle(
              color: Colors.white,
              fontSize: 16,
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }
}
