import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:pairing_planet2_frontend/shared/data/model/upload_item_model.dart';
import 'package:pairing_planet2_frontend/shared/data/model/widgets/reorderable_image_upload_slot_group.dart';
import '../../../../core/providers/autocomplete_providers.dart';
import '../../../../core/providers/image_providers.dart';
import '../../../../core/providers/locale_provider.dart';
import 'minimal_header.dart';
import '../../../../domain/entities/autocomplete/autocomplete_result.dart';

class HookSection extends ConsumerStatefulWidget {
  final TextEditingController titleController;
  final TextEditingController foodNameController; // 음식 이름용 추가
  final TextEditingController descriptionController;
  final List<UploadItem> finishedImages;
  final Function(int? foodId) onFoodIdSelected; // food1MasterId 전달용
  final VoidCallback onStateChanged;

  const HookSection({
    super.key,
    required this.titleController,
    required this.foodNameController,
    required this.descriptionController,
    required this.finishedImages,
    required this.onFoodIdSelected,
    required this.onStateChanged,
  });

  @override
  ConsumerState<HookSection> createState() => _HookSectionState();
}

class _HookSectionState extends ConsumerState<HookSection> {
  // 💡 이미지 업로드 비즈니스 로직 실행
  Future<void> _handleImageUpload(UploadItem item) async {
    item.status = UploadStatus.uploading;
    widget.onStateChanged();

    final result = await ref
        .read(uploadImageUseCaseProvider)
        .execute(
          file: item.file,
          type: "POST_RECIPE", // 백엔드 ImageType enum 대응
        );

    result.fold((failure) => item.status = UploadStatus.error, (response) {
      item.status = UploadStatus.success;
      item.serverUrl = response.imageUrl;
      item.publicId = response.imagePublicId; // UUID 저장
    });
    widget.onStateChanged();
  }

  @override
  Widget build(BuildContext context) {
    final currentLocale = ref.watch(localeProvider); // 전역 로케일 사용

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // 1. 이미지 업로드 영역 (최대 3장)
        ReorderableImageUploadSlotGroup(
          items: widget.finishedImages,
          onAddPressed: () async {
            final picker = ImagePicker();
            final XFile? image = await picker.pickImage(
              source: ImageSource.gallery,
            );
            if (image != null) {
              final newItem = UploadItem(file: File(image.path));
              widget.finishedImages.add(newItem);
              _handleImageUpload(newItem);
            }
          },
          onRemovePressed: (i) {
            setState(() => widget.finishedImages.removeAt(i));
            widget.onStateChanged();
          },
          onRetryPressed: (i) => _handleImageUpload(widget.finishedImages[i]),
          onReorder: (oldIdx, newIdx) {
            setState(() {
              if (newIdx > oldIdx) newIdx -= 1;
              final item = widget.finishedImages.removeAt(oldIdx);
              widget.finishedImages.insert(newIdx, item);
            });
            widget.onStateChanged();
          },
        ),
        const SizedBox(height: 24),

        // 2. 제목 및 음식 이름(자동완성) 입력창
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            children: [
              TextField(
                controller: widget.titleController,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w600,
                ),
                decoration: const InputDecoration(
                  hintText: "레시피 제목",
                  prefixIcon: Icon(Icons.title),
                  border: InputBorder.none,
                ),
              ),
              const Divider(height: 1),

              // 💡 자동완성 적용된 음식 이름 필드
              Autocomplete<AutocompleteResult>(
                displayStringForOption: (option) => option.name,
                optionsBuilder: (TextEditingValue textEditingValue) async {
                  if (textEditingValue.text.isEmpty)
                    return const Iterable.empty();

                  // 서버 자동완성 API 호출
                  final result = await ref
                      .read(getAutocompleteUseCaseProvider)
                      .execute(textEditingValue.text, currentLocale);

                  return result.fold(
                    (_) => const Iterable.empty(),
                    (list) => list,
                  );
                },
                onSelected: (AutocompleteResult selection) {
                  widget.foodNameController.text = selection.name;
                  widget.onFoodIdSelected(selection.id); // food1MasterId 설정
                },
                fieldViewBuilder:
                    (context, controller, focusNode, onFieldSubmitted) {
                      return TextField(
                        controller: controller,
                        focusNode: focusNode,
                        decoration: const InputDecoration(
                          hintText: "어떤 요리인가요? (예: 김치찌개)",
                          prefixIcon: Icon(Icons.restaurant),
                          border: InputBorder.none,
                        ),
                      );
                    },
              ),
            ],
          ),
        ),
        const SizedBox(height: 24),

        // 3. 요리 설명 영역
        const MinimalHeader(icon: Icons.notes, title: "요리 설명"),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(16),
          ),
          child: TextField(
            controller: widget.descriptionController,
            maxLines: 4,
            style: const TextStyle(fontSize: 15),
            decoration: const InputDecoration(
              hintText: "레시피에 대한 이야기를 들려주세요...",
              border: InputBorder.none,
            ),
          ),
        ),
      ],
    );
  }
}
