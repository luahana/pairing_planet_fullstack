import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/data/repositories/image_repository_impl.dart';
import 'package:pairing_planet2_frontend/domain/repositories/image_repository.dart';
import '../network/dio_provider.dart';
import '../../data/datasources/image/image_remote_data_source.dart';
import '../../domain/usecases/image/upload_image_usecase.dart';

// 1. Data Source
final imageRemoteDataSourceProvider = Provider((ref) {
  return ImageRemoteDataSource(ref.watch(dioProvider));
});

// 2. Repository
final imageRepositoryProvider = Provider<ImageRepository>((ref) {
  return ImageRepositoryImpl(ref.watch(imageRemoteDataSourceProvider));
});

// 3. UseCase
final uploadImageUseCaseProvider = Provider((ref) {
  return UploadImageUseCase(ref.watch(imageRepositoryProvider));
});
