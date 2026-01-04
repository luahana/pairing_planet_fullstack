import 'dart:io';

import 'package:dartz/dartz.dart';
import 'package:pairing_planet2_frontend/core/error/failures.dart';
import 'package:pairing_planet2_frontend/data/models/image/image_upload_response_dto.dart';
import 'package:pairing_planet2_frontend/domain/repositories/image_repository.dart';

class UploadImageUseCase {
  final ImageRepository _repository;

  UploadImageUseCase(this._repository);

  Future<Either<Failure, ImageUploadResponseDto>> execute({
    required File file,
    required String type,
  }) async {
    return await _repository.uploadImage(file: file, type: type);
  }
}
