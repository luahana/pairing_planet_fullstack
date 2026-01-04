import 'package:flutter/material.dart';
import 'minimal_header.dart';

class StepSection extends StatefulWidget {
  final List<Map<String, dynamic>> steps;
  final VoidCallback onAddStep;
  final Function(int) onRemoveStep;
  final Function(int, int) onReorder;

  const StepSection({
    super.key,
    required this.steps,
    required this.onAddStep,
    required this.onRemoveStep,
    required this.onReorder,
  });

  @override
  State<StepSection> createState() => _StepSectionState();
}

class _StepSectionState extends State<StepSection> {
  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const MinimalHeader(icon: Icons.format_list_numbered, title: "요리 단계"),
        const SizedBox(height: 12),
        ReorderableListView.builder(
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          itemCount: widget.steps.length,
          onReorder: widget.onReorder,
          itemBuilder: (context, index) {
            final step = widget.steps[index];
            return Container(
              key: ValueKey("step_$index"),
              margin: const EdgeInsets.only(bottom: 16),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  _buildStepNumber(index + 1),
                  const SizedBox(width: 12),
                  // 추후 이미지 기능 구현 시 _buildStepImageSlot 추가
                  Expanded(
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 12),
                      decoration: BoxDecoration(
                        color: Colors.grey[50],
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: TextField(
                        onChanged: (v) => step["description"] = v,
                        maxLines: null,
                        style: const TextStyle(fontSize: 14),
                        decoration: const InputDecoration(
                          hintText: "설명...",
                          border: InputBorder.none,
                          isDense: true,
                          contentPadding: EdgeInsets.symmetric(vertical: 12),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 8),
                  IconButton(
                    onPressed: () => widget.onRemoveStep(index),
                    icon: const Icon(Icons.close, size: 18, color: Colors.grey),
                    padding: EdgeInsets.zero,
                    constraints: const BoxConstraints(),
                  ),
                ],
              ),
            );
          },
        ),
        Center(
          child: TextButton.icon(
            onPressed: widget.onAddStep,
            icon: const Icon(Icons.add, size: 18),
            label: const Text(
              "단계 추가",
              style: TextStyle(fontWeight: FontWeight.w600),
            ),
            style: TextButton.styleFrom(foregroundColor: Colors.grey[600]),
          ),
        ),
      ],
    );
  }

  Widget _buildStepNumber(int number) => Padding(
    padding: const EdgeInsets.only(top: 4),
    child: Text(
      "$number",
      style: TextStyle(
        fontSize: 16,
        fontWeight: FontWeight.w800,
        color: const Color(0xFF1A237E).withOpacity(0.3),
      ),
    ),
  );
}
