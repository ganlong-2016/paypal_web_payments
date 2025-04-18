
import 'package:paypal_web_payments/models/map_helper.dart';

class FPayPalErrorInfo extends MapHelper {
  String code = "";
  String orderId = "";
  String message = "";

  FPayPalErrorInfo fromJson(Map<String, dynamic> data) {
    setMap(data);

    code = getString("code");
    orderId = getString("order_id");
    message = getString("error_message");
    return this;
  }
}
