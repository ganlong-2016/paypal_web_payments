import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:paypal_web_payments/models/order_callback.dart';
import 'package:paypal_web_payments/paypal_web_payments.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  final _paypalWebPaymentsPlugin = PaypalWebPayments.instance;

  @override
  void initState() {
    super.initState();
    initPlatformState();
    _paypalWebPaymentsPlugin.init(
      FPayPalOrderCallback(
        onSuccess: (data) {
          debugPrint("成功回调");
        },
        onCancel: (data) {
          debugPrint("取消回调");
        },
        onError: (data) {
          debugPrint("错误回调");
        },
        onNoResult: () {
          debugPrint("无回调");
        },
      ),
    );
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await _paypalWebPaymentsPlugin.getPlatformVersion() ??
          'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Plugin example app')),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Center(child: Text('Running on: $_platformVersion\n')),
            FilledButton.tonal(
              onPressed: () {
                _paypalWebPaymentsPlugin.startWebPayments(
                  clientId: "AX07eHwOfPLsZelXbDfFQRQvFUt9OVdQA8PfRHvSOL9uz-ieeDD2xGL9py6ytVfLmJejaCJYsmwSWU-2",
                  orderId: "8EF907765Y663212G",
                  urlScheme: "nativexo",
                );
              },
              child: Text("paypal web"),
            ),
            FilledButton.tonal(
              onPressed: () {
                _paypalWebPaymentsPlugin.startNativePayments(
                  clientId: "AX07eHwOfPLsZelXbDfFQRQvFUt9OVdQA8PfRHvSOL9uz-ieeDD2xGL9py6ytVfLmJejaCJYsmwSWU-2",
                  orderId: "8EF907765Y663212G",
                  urlScheme: "nativexo",
                );
              },
              child: Text("paypal native"),
            ),
          ],
        ),
      ),
    );
  }
}
