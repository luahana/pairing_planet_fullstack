import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:image_picker/image_picker.dart';
import 'package:pairing_planet2_frontend/core/widgets/image_source_sheet.dart';
import 'package:pairing_planet2_frontend/shared/data/model/upload_item_model.dart';
import '../../../../core/providers/image_providers.dart';
import 'minimal_header.dart';

class StepSection extends ConsumerStatefulWidget {
  final List<Map<String, dynamic>> steps;
  final VoidCallback onAddStep;
  final Function(int) onRemoveStep;
  final Function(int, int) onReorder;
  final VoidCallback onStateChanged;

  const StepSection({
    super.key,
    required this.steps,
    required this.onAddStep,
    required this.onRemoveStep,
    required this.onReorder,
    required this.onStateChanged,
  });

  @override
  ConsumerState<StepSection> createState() => _StepSectionState();
}

class _StepSectionState extends ConsumerState<StepSection> {
  Future<void> _pickStepImage(int index, ImageSource source) async {
    if (widget.steps[index]['isOriginal'] == true) return; // 💡 기존 이미지는 수정 불가

    final picker = ImagePicker();
    final XFile? image = await picker.pickImage(
      source: source,
      imageQuality: 70,
    );
    if (image != null) {
      final newItem = UploadItem(file: File(image.path));
      widget.steps[index]['uploadItem'] = newItem;
      _handleStepImageUpload(index, newItem);
    }
  }

  Future<void> _handleStepImageUpload(int index, UploadItem item) async {
    setState(() => item.status = UploadStatus.uploading);
    final result = await ref
        .read(uploadImageUseCaseProvider)
        .execute(file: item.file, type: "STEP");
    result.fold(
      (f) => setState(() => item.status = UploadStatus.error),
      (res) => setState(() {
        item.status = UploadStatus.success;
        widget.steps[index]['imageUrl'] = res.imageUrl;
        widget.steps[index]['imagePublicId'] = res.imagePublicId;
      }),
    );
    widget.onStateChanged();
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const MinimalHeader(
          icon: Icons.format_list_numbered,
          title: "요리 단계 (드래그하여 순서 변경)",
        ),
        const SizedBox(height: 12),
        // 💡 드래그 앤 드롭 리스트
        ReorderableListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: widget.steps.length,
          onReorder: widget.onReorder,
          itemBuilder: (context, index) {
            final step = widget.steps[index];
            final bool isOriginal = step['isOriginal'] ?? false;

            return Container(
              key: ValueKey("step_${step['stepNumber']}_$index"), // 💡 유니크 키 필수
              margin: const EdgeInsets.only(bottom: 20),
              decoration: BoxDecoration(
                color: isOriginal ? Colors.grey[50] : Colors.white,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  const Icon(
                    Icons.drag_handle,
                    color: Colors.grey,
                  ), // 💡 드래그 핸들
                  const SizedBox(width: 8),
                  _buildStepNumber(index + 1),
                  const SizedBox(width: 12),
                  _buildImageSlot(index, step),
                  const SizedBox(width: 12),
                  Expanded(child: _buildDescriptionField(step, isOriginal)),
                  IconButton(
                    onPressed: () => widget.onRemoveStep(index),
                    icon: const Icon(
                      Icons.remove_circle_outline,
                      color: Colors.redAccent,
                    ),
                  ),
                ],
              ),
            );
          },
        ),
        Center(
          child: TextButton.icon(
            onPressed: widget.onAddStep,
            icon: const Icon(Icons.add),
            label: const Text("단계 추가"),
          ),
        ),
      ],
    );
  }

  Widget _buildDescriptionField(Map<String, dynamic> step, bool isOriginal) {
    return TextField(
      readOnly: isOriginal, // 💡 기존 단계 텍스트 수정 불가
      onChanged: (v) => step["description"] = v,
      controller: TextEditingController(text: step["description"])
        ..selection = TextSelection.collapsed(
          offset: step["description"]?.length ?? 0,
        ),
      maxLines: null,
      decoration: InputDecoration(
        hintText: isOriginal ? "" : "과정 설명...",
        border: InputBorder.none,
        filled: isOriginal,
        fillColor: isOriginal ? Colors.grey[100] : Colors.transparent,
      ),
    );
  }

  Widget _buildImageSlot(int index, Map<String, dynamic> step) {
    final item = step['uploadItem'] as UploadItem?;
    final String? remoteUrl = step['imageUrl'];

    return GestureDetector(
      onTap: () => ImageSourceSheet.show(
        context: context,
        onSourceSelected: (s) => _pickStepImage(index, s),
      ),
      child: Container(
        width: 60,
        height: 60,
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey[300]!),
          image: (item?.file != null)
              ? DecorationImage(image: FileImage(item!.file), fit: BoxFit.cover)
              : (remoteUrl != null && remoteUrl.isNotEmpty)
              ? DecorationImage(
                  image: NetworkImage(remoteUrl),
                  fit: BoxFit.cover,
                )
              : null,
        ),
        child: (item == null && (remoteUrl == null || remoteUrl.isEmpty))
            ? const Icon(Icons.camera_alt, size: 20, color: Colors.grey)
            : null,
      ),
    );
  }

  Widget _buildStepNumber(int number) => Text(
    "$number",
    style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
  );
}
