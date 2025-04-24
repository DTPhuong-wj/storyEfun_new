package com.example.storyefun.admin.ui

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storyefun.viewModel.UserViewModel
import com.example.storyefun.zaloPay.Api.CreateOrder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.json.JSONObject
import vn.zalopay.sdk.Environment
import vn.zalopay.sdk.ZaloPayError
import vn.zalopay.sdk.ZaloPaySDK
import vn.zalopay.sdk.listeners.PayOrderListener

class OrderPayment : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cho phép gọi mạng trên main thread (chỉ dùng khi test)
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder().permitAll().build()
        )

        // Khởi tạo ZaloPay SDK
        ZaloPaySDK.init(2553, Environment.SANDBOX)

        val amount = intent.getIntExtra("amount", 0)  // Lấy số tiền từ Intent
        val coin = intent.getIntExtra("coin", 0)      // Lấy số coin từ Intent

        setContent {
            OrderPaymentScreen(amount, coin)  // Truyền dữ liệu vào màn hình
        }
    }

    // Sử dụng phương thức onNewIntent từ Activity thay vì ComponentActivity
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent?.let {
            ZaloPaySDK.getInstance().onResult(it)
        }
    }
}

@Composable
fun OrderPaymentScreen(amount: Int, coin: Int, userViewModel: UserViewModel = UserViewModel()) {
    val context = LocalContext.current
    val amountFormatted = String.format("%.0f", amount.toDouble())  // Chuyển số tiền sang định dạng chuỗi
    var paymentStatus by remember { mutableStateOf<String?>(null) }

    // Dữ liệu người dùng
    var userName = FirebaseAuth.getInstance().currentUser?.displayName
    var coinBalance = userViewModel.getBalance()
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // Chạy effect để lấy thông tin người dùng từ Firebase
    LaunchedEffect(uid) {
        if (uid != null) {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("username") ?: "Người dùng không xác định"
                    coinBalance = document.getLong("coin")!!
                }
                .addOnFailureListener {
                    userName = "Không thể lấy thông tin người dùng"
                    coinBalance = 0 // hoặc hiển thị lỗi nếu không thể lấy thông tin người dùng
                }
        }
    }

    // Đợi cho đến khi `userName` có giá trị không null
    if (userName == null) {
        // Hiển thị loading spinner trong khi đang tải thông tin
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF6B48FF))
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header với gradient tím, chứa cả "Đơn hàng" và "Chào, $userName"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6B48FF), Color(0xFFA855F7))
                        )
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Đơn hàng",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Chào, $userName",
                        fontSize = 20.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }


            // Số dư hiện tại
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF0F0F0))
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Số dư hiện tại:",
                    fontSize = 20.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    "$coinBalance Coin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5722)
                )
            }

            // Thông tin nạp tiền
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Số tiền nạp: $amountFormatted VNĐ",
                    fontSize = 20.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    "Số coin: $coin Coin",
                    fontSize = 20.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    "Coin: $coin",
                    fontSize = 20.sp,
                    color = Color(0xFF333333)
                )
            }

            // Trạng thái thanh toán (nếu có)
            paymentStatus?.let {
                Text(
                    text = it,
                    color = Color(0xFF6B48FF),
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                )
            }

            // Nút thanh toán
            Button(
                onClick = {
                    val orderApi = CreateOrder()
                    try {
                        val data: JSONObject = orderApi.createOrder(amountFormatted)  // Gửi số tiền đã chọn
                        if (data.getString("return_code") == "1") {
                            val token = data.getString("zp_trans_token")
                            ZaloPaySDK.getInstance().payOrder(
                                context as ComponentActivity,
                                token,
                                "demozpdk://app",  // URL callback
                                object : PayOrderListener {

                                    override fun onPaymentSucceeded(s: String?, s1: String?, s2: String?) {
                                        paymentStatus = "Thanh toán thành công"

                                        // Cập nhật số dư coin vào Firestore
                                        val uid = FirebaseAuth.getInstance().currentUser?.uid
                                        if (uid != null && coinBalance != null) {
                                            val newCoin = coinBalance!! + coin

                                            // Cập nhật Firestore
                                            FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(uid)
                                                .update("coin", newCoin)
                                                .addOnSuccessListener {
                                                    Log.d("OrderPayment", "Cập nhật coin thành công: $newCoin")
                                                    // Sau khi cập nhật Firestore thành công, chuyển đến màn hình thông báo
                                                    context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                        putExtra("result", "Thanh toán thành công")
                                                    })
                                                }
                                                .addOnFailureListener {
                                                    // Nếu có lỗi trong việc cập nhật Firestore, hiển thị thông báo lỗi
                                                    context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                        putExtra("result", "Cập nhật coin thất bại")
                                                    })
                                                }
                                        } else {
                                            // Trường hợp không lấy được uid hoặc coinBalance
                                            paymentStatus = "Lỗi khi lấy thông tin người dùng"
                                            context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                                putExtra("result", "Lỗi khi lấy thông tin người dùng")
                                            })
                                        }
                                    }

                                    override fun onPaymentCanceled(s: String?, s1: String?) {
                                        paymentStatus = "Hủy thanh toán"
                                        context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                            putExtra("result", "Hủy thanh toán")
                                        })
                                    }

                                    override fun onPaymentError(zaloPayError: ZaloPayError?, s: String?, s1: String?) {
                                        paymentStatus = "Lỗi thanh toán"
                                        context.startActivity(Intent(context, PaymentNotification::class.java).apply {
                                            putExtra("result", "Lỗi thanh toán")
                                        })
                                    }
                                }
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        paymentStatus = "Lỗi tạo đơn hàng"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(64.dp)
                    .shadow(5.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6B48FF), Color(0xFFA855F7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Thanh toán bằng Zalo Pay",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}