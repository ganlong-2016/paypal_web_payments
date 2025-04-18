# PayPal Web Payments

Flutter 插件，用于集成 PayPal Web 支付功能，支持 Android  平台。

## 功能

- 支持 PayPal Web 支付流程
- 处理支付结果回调
- 支持 Android 平台
- iOS 平台暂未支持

## 安装

```yaml
dependencies:
  paypal_web_payments: ^0.1.0

## 使用方法
## 初始化
```dart
import 'package:paypal_web_payments/paypal_web_payments.dart';

PaypalWebPayments.instance.init(
  FPayPalOrderCallback(
    onSuccess: (data) {
      print('支付成功: ${data.orderId}, ${data.payerId}');
    },
    onCancel: (orderId) {
      print('支付取消: $orderId');
    },
    onError: (error) {
      print('支付错误: ${error.code}, ${error.errorMessage}');
    },
    onOnResult: () {
      print('无结果');
    },
  ),
);
```

## 发起支付
```dart
await PaypalWebPayments.instance.startWebPayments(
  clientId: 'YOUR_PAYPAL_CLIENT_ID',
  orderId: 'YOUR_ORDER_ID',
  urlScheme: 'YOUR_APP_URL_SCHEME',
);
```

### Android 配置
```
<activity
    android:name=".MainActivity"
    android:launchMode="singleInstance"
    ...>
    <intent-filter>
        <action android:name="android.intent.action.VIEW"/>
        <data android:scheme="YOUR_APP_URL_SCHEME"/>
        <category android:name="android.intent.category.DEFAULT"/>
        <category android:name="android.intent.category.BROWSABLE"/>
    </intent-filter>
</activity>

将 activity 替换成FlutterFragmentActivity
```

