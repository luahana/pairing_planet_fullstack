import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

class MainScreen extends StatelessWidget {
  const MainScreen({super.key, required this.navigationShell});

  final StatefulNavigationShell navigationShell;

  void _onTap(int index) {
    navigationShell.goBranch(
      index,
      initialLocation: index == navigationShell.currentIndex,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: navigationShell,
      // 💡 중앙 등록 버튼
      floatingActionButton: FloatingActionButton(
        onPressed: () => context.push('/recipe/create'),
        backgroundColor: Theme.of(context).primaryColor,
        shape: const CircleBorder(),
        child: const Icon(Icons.add, color: Colors.white, size: 30),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      bottomNavigationBar: BottomAppBar(
        shape: const CircularNotchedRectangle(),
        notchMargin: 8.0,
        clipBehavior: Clip.antiAlias,
        child: SizedBox(
          height: 60,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildNavItem(0, Icons.home, '홈'),
              _buildNavItem(1, Icons.restaurant, '레시피'),
              const SizedBox(width: 40), // 중앙 버튼 공간
              _buildNavItem(2, Icons.search, '검색'),
              _buildNavItem(3, Icons.person, '마이'),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(int index, IconData icon, String label) {
    final isSelected = navigationShell.currentIndex == index;
    return InkWell(
      onTap: () => _onTap(index),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: isSelected ? Colors.orange : Colors.grey),
          Text(
            label,
            style: TextStyle(
              color: isSelected ? Colors.orange : Colors.grey,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}
