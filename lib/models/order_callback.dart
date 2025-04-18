

import 'package:paypal_web_payments/models/approval_data.dart';
import 'package:paypal_web_payments/models/error_info.dart';

class FPayPalOrderCallback {
  void Function(FPayPalApprovalData success) onSuccess;
  void Function(FPayPalErrorInfo error)? onError;
  void Function(String orderId)? onCancel;
  void Function()? onNoResult;

  String onSuccessMessage;
  String onErrorMessage;
  String onCancelMessage;

  FPayPalOrderCallback({
    required this.onSuccess,
    this.onError,
    this.onCancel,
    this.onNoResult,
    this.onSuccessMessage = "Order approved",
    this.onErrorMessage = "Error creating order",
    this.onCancelMessage = "Order cancelled",
  });
}
