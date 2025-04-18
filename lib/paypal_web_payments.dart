import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:paypal_web_payments/models/approval_data.dart';
import 'package:paypal_web_payments/models/error_info.dart';
import 'package:paypal_web_payments/models/order_callback.dart';

import 'paypal_web_payments_platform_interface.dart';

class PaypalWebPayments {
  static PaypalWebPayments? _instance;
  PaypalWebPayments._();
  static PaypalWebPayments get instance {
    _instance ??= PaypalWebPayments._();
    return _instance!;
  }

  final _methodChannel = const MethodChannel('paypal_web_payments');

  FPayPalOrderCallback _callback = FPayPalOrderCallback(
    onCancel: (_) {},
    onSuccess: (_) {},
    onError: (_) {},
    onNoResult: () {},
  );

  static bool isDebugMode = false;

  // Private function that gets called by ObjC/Java
  Future<void> _handleMethod(MethodCall call) async {
    debugPrint("支付结果回调: ${call.method}, 参数: ${call.arguments}");
    if (call.method == 'FlutterPaypal#onSuccess') {
      _onPayPalOrderSuccess(call.arguments.cast<String, dynamic>());
    } else if (call.method == 'FlutterPaypal#onCancel') {
      _onCancelPayPalOrder(call.arguments.cast<String, dynamic>());
    } else if (call.method == 'FlutterPaypal#onError') {
      _onPayPalOrderError(call.arguments.cast<String, dynamic>());
    } else if (call.method == 'FlutterPaypal#onNoResult') {
      _onPayPalOrderNoResult();
    }
  }

  Future<String?> getPlatformVersion() {
    return PaypalWebPaymentsPlatform.instance.getPlatformVersion();
  }

  void init(FPayPalOrderCallback callback) {
    debugPrint("设置回调");
    _callback = callback;
    _methodChannel.setMethodCallHandler(_handleMethod);
    debugPrint("设置回调结束");
  }

  Future<void> startWebPayments({
    required String clientId,
    required String orderId,
    required String urlScheme,
  }) async {
    try {
      final result = await _methodChannel.invokeMethod(
        'FlutterPaypal#startWebCheckout',
        {'client_id': clientId, 'order_id': orderId, 'url_scheme': urlScheme},
      );
      debugPrint("开始支付结果: $result");
    } catch (e) {
      debugPrint("调用支付方法出错: $e");
    }
  }

   Future<void> startNativePayments({
    required String clientId,
    required String orderId,
    required String urlScheme,
  }) async {
    try {
      final result = await _methodChannel.invokeMethod(
        'FlutterPaypal#startNativeCheckout',
        {'client_id': clientId, 'order_id': orderId, 'url_scheme': urlScheme},
      );
      debugPrint("开始支付结果: $result");
    } catch (e) {
      debugPrint("调用支付方法出错: $e");
    }
  }

  void _onPayPalOrderSuccess(Map<String, dynamic> data) {
    debugPrint("支付成功回调: $data");
    FPayPalApprovalData success = FPayPalApprovalData();
    try {
      success = FPayPalApprovalData.fromJson(data);
    } catch (e) {
      if (isDebugMode) debugPrint("解析支付成功数据出错: $e");
    }
    _callback.onSuccess(success);
  }

  void _onCancelPayPalOrder(Map<String, dynamic> data) {
    debugPrint("支付取消回调: $data");
    if (_callback.onCancel != null) {
      _callback.onCancel!(data['order_id']);
    }
  }

  void _onPayPalOrderError(Map<String, dynamic> data) {
    debugPrint("支付错误回调: $data");
    FPayPalErrorInfo error = FPayPalErrorInfo();
    try {
      error = error.fromJson(data);
    } catch (e) {
      if (isDebugMode) debugPrint("解析支付错误数据出错: $e");
    }

    if (_callback.onError != null) {
      _callback.onError!(error);
    }
  }

  void _onPayPalOrderNoResult() {
    debugPrint("支付无结果回调");
    if (_callback.onNoResult != null) {
      _callback.onNoResult!();
    }
  }
}
