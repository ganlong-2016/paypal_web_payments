package com.paypal.paypal_web_payments

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.fragment.app.FragmentActivity
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutClient
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutListener
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutRequest
import com.paypal.android.paypalnativepayments.PayPalNativeCheckoutResult
import com.paypal.android.paypalwebpayments.PayPalPresentAuthChallengeResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFinishStartResult
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch


/** PaypalWebPaymentsPlugin */
class PaypalWebPaymentsPlugin : FlutterPlugin, MethodCallHandler, ActivityAware,
    PluginRegistry.NewIntentListener {
    final val TAG = "PaypalWebPaymentsPlugin"

    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: ComponentActivity? = null
    private var authState: String? = null
    private var paypalClient: PayPalWebCheckoutClient? = null
    private lateinit var preferences: SharedPreferences

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "paypal_web_payments")
        channel.setMethodCallHandler(this)
        Log.i(TAG, "onAttachedToEngine: ")

    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        Log.i(TAG, "onMethodCall: ")
        if (call.method == "getPlatformVersion") {
            result.success("Android ${android.os.Build.VERSION.RELEASE}")
        } else if (call.method == "FlutterPaypal#startWebCheckout") {
            startWebCheckout(call, result)
        } else if (call.method == "FlutterPaypal#startNativeCheckout") {
            startNativeCheckout(call, result)
        } else {
            result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        Log.i(TAG, "onDetachedFromEngine: ")
        channel.setMethodCallHandler(null)
    }

    private fun startWebCheckout(call: MethodCall, result: Result) {
        if (activity == null) {
            result.error("20001", "Activity must be FlutterFragmentActivity!", null)
            return
        }

        val orderId = call.argument<String>("order_id")
        val clientId = call.argument<String>("client_id")
        val urlScheme = call.argument<String>("url_scheme")
        if (orderId.isNullOrBlank()) {
            result.error("20002", "order_id is empty!", null)
            return
        }
        if (clientId.isNullOrBlank()) {
            result.error("20003", "client_id is empty!", null)
            return
        }
        if (urlScheme.isNullOrBlank()) {
            result.error("20004", "return_url is empty!", null)
            return
        }
        val coreConfig = CoreConfig(clientId)

        paypalClient = PayPalWebCheckoutClient(activity!!, coreConfig, urlScheme)
        val checkoutRequest =
            PayPalWebCheckoutRequest(orderId, PayPalWebCheckoutFundingSource.PAYPAL)
        when (val startResult = paypalClient?.start(activity!!, checkoutRequest)) {
            is PayPalPresentAuthChallengeResult.Success -> {
                authState = startResult.authState
                result.success(startResult.authState)
            }

            is PayPalPresentAuthChallengeResult.Failure -> {
                result.error(
                    startResult.error.code.toString(),
                    startResult.error.errorDescription,
                    startResult.error.correlationId
                )
            }

            null -> {
                result.error("20000", "UnKnown error", null)
            }
        }
    }

    private fun startNativeCheckout(call: MethodCall, result: Result) {
        val orderId = call.argument<String>("order_id")
        val clientId = call.argument<String>("client_id")
        val urlScheme = call.argument<String>("url_scheme")

        if (orderId.isNullOrBlank()) {
            result.error("20002", "order_id is empty!", null)
            return
        }
        if (clientId.isNullOrBlank()) {
            result.error("20003", "client_id is empty!", null)
            return
        }
        if (urlScheme.isNullOrBlank()) {
            result.error("20004", "return_url is empty!", null)
            return
        }

        val coreConfig = CoreConfig(clientId, Environment.SANDBOX)

        val paypalClient = PayPalNativeCheckoutClient(
            application = activity!!.application,
            coreConfig = coreConfig,
            returnUrl = "$urlScheme://paypalpay"
        )
        val request = PayPalNativeCheckoutRequest(orderId)
        paypalClient.startCheckout(request)
        paypalClient.listener = object : PayPalNativeCheckoutListener {
            override fun onPayPalCheckoutCanceled() {
            }

            override fun onPayPalCheckoutFailure(error: PayPalSDKError) {
            }

            override fun onPayPalCheckoutStart() {
            }

            override fun onPayPalCheckoutSuccess(result: PayPalNativeCheckoutResult) {
            }

        }
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        Log.i(TAG, "onAttachedToActivity: ")

        if (binding.activity is FragmentActivity) {
            activity = binding.activity as FragmentActivity
            Toast.makeText(activity, "onAttachedToActivity 回调", Toast.LENGTH_SHORT).show();
            binding.addOnNewIntentListener(this)
            // 检查当前 Intent 是否包含支付结果
            val currentIntent = activity?.intent
            if (currentIntent != null) {
                Log.i(TAG, "onAttachedToActivity: 检查当前 Intent 是否包含支付结果")
                completeAuthChallenge(currentIntent)
            }
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        Log.i(TAG, "onDetachedFromActivityForConfigChanges: ")
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        Log.i(TAG, "onReattachedToActivityForConfigChanges: ")
        if (binding.activity is FragmentActivity) {
            activity = binding.activity as FragmentActivity
            Toast.makeText(
                activity,
                "onReattachedToActivityForConfigChanges 回调",
                Toast.LENGTH_SHORT
            ).show();
            binding.addOnNewIntentListener(this)
            // 检查当前 Intent 是否包含支付结果
            val currentIntent = activity?.intent
            if (currentIntent != null) {
                Log.i(
                    TAG,
                    "onReattachedToActivityForConfigChanges: 检查当前 Intent 是否包含支付结果"
                )
                completeAuthChallenge(currentIntent)
            }
        }
    }

    override fun onDetachedFromActivity() {
        activity = null
    }

    override fun onNewIntent(intent: Intent): Boolean {
        completeAuthChallenge(intent)
        return true
    }

    private fun checkIfPayPalAuthFinished(intent: Intent): PayPalWebCheckoutFinishStartResult? =
        authState?.let { paypalClient?.finishStart(intent, it) }

    fun completeAuthChallenge(intent: Intent) {
        if (authState == null || paypalClient == null) {
            Log.e(TAG, "completeAuthChallenge: authState 或 paypalClient 为空")
            return
        }
        checkIfPayPalAuthFinished(intent)?.let { payPalAuthResult ->
            when (payPalAuthResult) {
                is PayPalWebCheckoutFinishStartResult.Success -> {
                    MainScope().launch {
                        Log.i(TAG, "支付成功: ")
                        channel.invokeMethod(
                            "FlutterPaypal#onSuccess", mapOf(
                                "order_id" to payPalAuthResult.orderId,
                                "payer_id" to payPalAuthResult.payerId,
                            )
                        )
                    }

                }

                is PayPalWebCheckoutFinishStartResult.Canceled -> {
                    MainScope().launch {
                        Log.i(TAG, "支付关闭: ")

                        channel.invokeMethod(
                            "FlutterPaypal#onCancel", mapOf(
                                "order_id" to payPalAuthResult.orderId
                            )
                        )
                    }
                }

                is PayPalWebCheckoutFinishStartResult.Failure -> {
                    MainScope().launch {
                        Log.i(TAG, "支付失败: ")

                        channel.invokeMethod(
                            "FlutterPaypal#onError", mapOf(
                                "order_id" to payPalAuthResult.orderId,
                                "code" to payPalAuthResult.error.code,
                                "error_message" to payPalAuthResult.error.errorDescription,
                                "correlation_id" to payPalAuthResult.error.correlationId,
                            )
                        )
                    }
                }

                PayPalWebCheckoutFinishStartResult.NoResult -> {
                    // no result; re-enable PayPal button so user can retry
                    MainScope().launch {
                        Log.i(TAG, "未知结果: ")
                        channel.invokeMethod("FlutterPaypal#onNoResult", null)
                    }

                }
            }
        }
    }
}
