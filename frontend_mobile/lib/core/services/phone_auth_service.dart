import 'package:firebase_auth/firebase_auth.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:pairing_planet2_frontend/core/utils/logger.dart';

/// Phone auth service provider
final phoneAuthServiceProvider = Provider((ref) => PhoneAuthService());

/// State for phone verification
enum PhoneVerificationState {
  initial,
  codeSent,
  verifying,
  verified,
  error,
}

/// Service for Firebase Phone Authentication
/// Used for Korean PIPA compliance - phone number verification
class PhoneAuthService {
  final FirebaseAuth _auth = FirebaseAuth.instance;

  String? _verificationId;
  int? _resendToken;

  /// Send verification code to phone number
  /// [phoneNumber] should be in E.164 format (e.g., +821012345678)
  Future<PhoneVerificationResult> sendVerificationCode({
    required String phoneNumber,
    required void Function(String verificationId) onCodeSent,
    required void Function(String errorMessage) onError,
    int? resendToken,
  }) async {
    try {
      talker.info('Sending verification code to $phoneNumber');

      await _auth.verifyPhoneNumber(
        phoneNumber: phoneNumber,
        timeout: const Duration(seconds: 60),
        forceResendingToken: resendToken ?? _resendToken,
        verificationCompleted: (PhoneAuthCredential credential) async {
          // Auto-verification on Android
          talker.info('Phone auto-verified');
          try {
            await _linkOrSignInWithCredential(credential);
          } catch (e) {
            talker.error('Auto-verification sign-in failed: $e');
            onError(e.toString());
          }
        },
        verificationFailed: (FirebaseAuthException e) {
          talker.error('Phone verification failed: ${e.code} - ${e.message}');
          talker.error('Stack trace: ${e.stackTrace}');

          String errorMessage;
          switch (e.code) {
            case 'invalid-phone-number':
              errorMessage = 'phoneVerification.invalidPhoneNumber';
              break;
            case 'too-many-requests':
              errorMessage = 'phoneVerification.tooManyRequests';
              break;
            case 'quota-exceeded':
              errorMessage = 'phoneVerification.quotaExceeded';
              break;
            case 'app-not-authorized':
              talker.error('SHA-1 fingerprint may not be configured in Firebase Console');
              errorMessage = 'phoneVerification.sendFailed';
              break;
            case 'missing-client-identifier':
              talker.error('reCAPTCHA or SafetyNet verification failed');
              errorMessage = 'phoneVerification.sendFailed';
              break;
            default:
              talker.error('Unhandled error code: ${e.code}');
              errorMessage = 'phoneVerification.sendFailed';
          }
          onError(errorMessage);
        },
        codeSent: (String verificationId, int? resendToken) {
          talker.info('Verification code sent. VerificationId: ${verificationId.substring(0, 10)}...');
          _verificationId = verificationId;
          _resendToken = resendToken;
          onCodeSent(verificationId);
        },
        codeAutoRetrievalTimeout: (String verificationId) {
          talker.info('Auto retrieval timeout');
          _verificationId = verificationId;
        },
      );

      return PhoneVerificationResult(success: true);
    } catch (e) {
      talker.error('Failed to send verification code: $e');
      return PhoneVerificationResult(
        success: false,
        errorMessage: 'phoneVerification.sendFailed',
      );
    }
  }

  /// Verify the SMS code and link to current user or sign in
  Future<PhoneVerificationResult> verifySmsCode(String smsCode) async {
    if (_verificationId == null) {
      return PhoneVerificationResult(
        success: false,
        errorMessage: 'phoneVerification.sessionExpired',
      );
    }

    try {
      talker.info('Verifying SMS code...');

      final credential = PhoneAuthProvider.credential(
        verificationId: _verificationId!,
        smsCode: smsCode,
      );

      await _linkOrSignInWithCredential(credential);

      talker.info('Phone verification successful');
      return PhoneVerificationResult(success: true);
    } on FirebaseAuthException catch (e) {
      talker.error('SMS verification failed: ${e.code}');

      String errorMessage;
      switch (e.code) {
        case 'invalid-verification-code':
          errorMessage = 'phoneVerification.invalidCode';
          break;
        case 'session-expired':
          errorMessage = 'phoneVerification.sessionExpired';
          break;
        case 'credential-already-in-use':
          errorMessage = 'phoneVerification.phoneAlreadyInUse';
          break;
        default:
          errorMessage = 'phoneVerification.verifyFailed';
      }
      return PhoneVerificationResult(
        success: false,
        errorMessage: errorMessage,
      );
    } catch (e) {
      talker.error('Phone verification error: $e');
      return PhoneVerificationResult(
        success: false,
        errorMessage: 'phoneVerification.verifyFailed',
      );
    }
  }

  /// Link phone credential to existing user or sign in
  Future<void> _linkOrSignInWithCredential(PhoneAuthCredential credential) async {
    final currentUser = _auth.currentUser;

    if (currentUser != null) {
      // Link to existing account
      try {
        await currentUser.linkWithCredential(credential);
        talker.info('Phone linked to existing account');
      } on FirebaseAuthException catch (e) {
        if (e.code == 'credential-already-in-use') {
          // Phone already linked to another account - just verify
          talker.info('Phone already verified with another account');
        } else {
          rethrow;
        }
      }
    } else {
      // No user logged in - sign in with phone
      await _auth.signInWithCredential(credential);
      talker.info('Signed in with phone number');
    }
  }

  /// Resend verification code
  Future<PhoneVerificationResult> resendCode({
    required String phoneNumber,
    required void Function(String verificationId) onCodeSent,
    required void Function(String errorMessage) onError,
  }) async {
    return sendVerificationCode(
      phoneNumber: phoneNumber,
      onCodeSent: onCodeSent,
      onError: onError,
      resendToken: _resendToken,
    );
  }

  /// Clear verification state
  void clearState() {
    _verificationId = null;
    _resendToken = null;
  }

  /// Check if phone is already verified for current user
  bool get isPhoneVerified {
    final user = _auth.currentUser;
    if (user == null) return false;
    return user.phoneNumber != null && user.phoneNumber!.isNotEmpty;
  }

  /// Get current user's phone number (if verified)
  String? get verifiedPhoneNumber => _auth.currentUser?.phoneNumber;
}

/// Result of phone verification operations
class PhoneVerificationResult {
  final bool success;
  final String? errorMessage;

  PhoneVerificationResult({
    required this.success,
    this.errorMessage,
  });
}
