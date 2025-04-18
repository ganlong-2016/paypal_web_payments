class FPayPalApprovalData {
  String? payerId;
  String? orderId;

  FPayPalApprovalData({this.payerId, this.orderId});

  FPayPalApprovalData.fromJson(Map<String, dynamic> json) {
    payerId = json['payer_id'];
    orderId = json['order_id'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = {};
    data['payer_id'] = payerId;
    data['order_id'] = orderId;
    return data;
  }
}
