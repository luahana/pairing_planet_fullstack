import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
// ReorderableImageUploadSlotGroup과 UploadItem은 추후 이미지 기능 구현 시 활성화
// import 'reorderable_image_upload_slot_group.dart';
// import '../../../post/data/models/upload_item_model.dart';
import 'minimal_header.dart';

class HookSection extends ConsumerWidget {
  final TextEditingController titleController;
  final TextEditingController descriptionController;
  final TextEditingController localeController;
  // final List<UploadItem> finishedImages; // 추후 이미지 기능 구현 시 사용
  // final VoidCallback onAddImage;

  const HookSection({
    super.key,
    required this.titleController,
    required this.descriptionController,
    required this.localeController,
    // required this.finishedImages,
    // required this.onAddImage,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        // 추후 이미지 업로드 기능 구현 시 주석 해제 및 연결
        // ReorderableImageUploadSlotGroup(
        //   items: finishedImages,
        //   onAddPressed: onAddImage,
        //   onRemovePressed: (i) {}, // 구현 필요
        //   onRetryPressed: (i) {}, // 구현 필요
        //   onReorder: (o, n) {}, // 구현 필요
        // ),
        // const SizedBox(height: 24),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(16),
          ),
          child: Column(
            children: [
              TextField(
                controller: titleController,
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
              TextField(
                controller: localeController,
                decoration: const InputDecoration(
                  hintText: "국가/지역 (예: ko-KR)",
                  prefixIcon: Icon(Icons.public),
                  border: InputBorder.none,
                ),
              ),
            ],
          ),
        ),
        const SizedBox(height: 24),
        const MinimalHeader(icon: Icons.notes, title: "요리 설명"),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(16),
          ),
          child: TextField(
            controller: descriptionController,
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
