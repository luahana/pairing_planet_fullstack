import 'package:flutter/material.dart';
import 'minimal_header.dart';

class IngredientSection extends StatefulWidget {
  final List<Map<String, dynamic>> ingredients;
  final VoidCallback onAddIngredient;
  final Function(int) onRemoveIngredient;

  const IngredientSection({
    super.key,
    required this.ingredients,
    required this.onAddIngredient,
    required this.onRemoveIngredient,
  });

  @override
  State<IngredientSection> createState() => _IngredientSectionState();
}

class _IngredientSectionState extends State<IngredientSection> {
  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const MinimalHeader(
          icon: Icons.shopping_basket_outlined,
          title: "필요한 재료",
        ),
        const SizedBox(height: 12),
        ...widget.ingredients.asMap().entries.map(
          (e) => Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: Row(
              children: [
                Expanded(
                  flex: 2,
                  child: _smallField(
                    "재료명",
                    (v) => widget.ingredients[e.key]["name"] = v,
                    widget.ingredients[e.key]["name"],
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  flex: 1,
                  child: _smallField(
                    "양",
                    (v) => widget.ingredients[e.key]["amount"] = v,
                    widget.ingredients[e.key]["amount"],
                  ),
                ),
                const SizedBox(width: 4),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.grey, size: 18),
                  onPressed: () => widget.onRemoveIngredient(e.key),
                  padding: EdgeInsets.zero,
                  constraints: const BoxConstraints(),
                ),
              ],
            ),
          ),
        ),
        Center(
          child: TextButton.icon(
            onPressed: widget.onAddIngredient,
            icon: const Icon(Icons.add, size: 18),
            label: const Text(
              "재료 추가",
              style: TextStyle(fontWeight: FontWeight.w600),
            ),
            style: TextButton.styleFrom(foregroundColor: Colors.grey[600]),
          ),
        ),
      ],
    );
  }

  Widget _smallField(
    String hint,
    Function(String) onChanged,
    String? initialValue,
  ) {
    return Container(
      height: 44,
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(12),
      ),
      child: TextFormField(
        initialValue: initialValue,
        onChanged: onChanged,
        style: const TextStyle(fontSize: 13),
        decoration: InputDecoration(
          hintText: hint,
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(horizontal: 16),
          hintStyle: const TextStyle(fontSize: 12, color: Colors.grey),
        ),
      ),
    );
  }
}
