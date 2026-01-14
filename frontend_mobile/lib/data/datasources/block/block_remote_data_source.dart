import 'package:dio/dio.dart';
import 'package:pairing_planet2_frontend/core/constants/constants.dart';
import 'package:pairing_planet2_frontend/data/models/block/block_status_dto.dart';
import 'package:pairing_planet2_frontend/data/models/block/blocked_user_dto.dart';
import 'package:pairing_planet2_frontend/data/models/report/report_reason.dart';

class BlockRemoteDataSource {
  final Dio _dio;

  BlockRemoteDataSource(this._dio);

  /// Block a user
  Future<void> blockUser(String userId) async {
    await _dio.post(ApiEndpoints.block(userId));
  }

  /// Unblock a user
  Future<void> unblockUser(String userId) async {
    await _dio.delete(ApiEndpoints.block(userId));
  }

  /// Get block status for a user
  Future<BlockStatusDto> getBlockStatus(String userId) async {
    final response = await _dio.get(ApiEndpoints.blockStatus(userId));
    return BlockStatusDto.fromJson(response.data);
  }

  /// Get list of blocked users
  Future<BlockedUsersListResponse> getBlockedUsers({
    required int page,
    int size = 20,
  }) async {
    final response = await _dio.get(
      ApiEndpoints.blockedUsers,
      queryParameters: {'page': page, 'size': size},
    );
    return BlockedUsersListResponse.fromJson(response.data);
  }

  /// Report a user
  Future<void> reportUser(
    String userId,
    ReportReason reason, {
    String? description,
  }) async {
    await _dio.post(
      ApiEndpoints.report(userId),
      data: {
        'reason': reason.value,
        if (description != null) 'description': description,
      },
    );
  }
}
