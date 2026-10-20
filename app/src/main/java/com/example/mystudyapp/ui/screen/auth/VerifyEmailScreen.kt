package com.example.mystudyapp.ui.screen.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.mystudyapp.R
import com.example.mystudyapp.ui.component.MascotExpression
import com.example.mystudyapp.ui.component.MascotView
import com.example.mystudyapp.ui.component.shared.rememberWindowInfo
import com.example.mystudyapp.ui.theme.BeVietnamPro
import com.example.mystudyapp.ui.theme.LingoBlue
import kotlinx.coroutines.delay

@Composable
fun VerifyEmailScreen(
    email: String,
    isChecking: Boolean,
    isResending: Boolean,
    message: String?,
    resendSuccessMessage: String?,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
    onLogout: () -> Unit,
    onClearMessage: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val windowInfo = rememberWindowInfo()

    // Cooldown đếm ngược 60 giây khi bấm gửi lại email
    var cooldownSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(cooldownSeconds) {
        if (cooldownSeconds > 0) {
            delay(1000L)
            cooldownSeconds -= 1
        }
    }

    // Tự động kiểm tra trạng thái xác thực mỗi khi người dùng chuyển từ Gmail/trình duyệt quay lại app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                onCheckVerification()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Sky Background Image
        Image(
            painter = painterResource(id = R.drawable.bg_home_sky),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = if (windowInfo.isCompact) 16.dp else 20.dp,
                    vertical = if (windowInfo.isCompact) 16.dp else 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(12.dp))

            // Mascot LingoStudy in Sky
            MascotView(
                modifier = Modifier.size(if (windowInfo.isCompact) 100.dp else 130.dp),
                expression = MascotExpression.HAPPY,
                enableAnimation = true
            )

            Spacer(Modifier.height(12.dp))

            // Tiêu đề
            Text(
                text = "Xác thực địa chỉ Email",
                fontFamily = BeVietnamPro,
                fontSize = if (windowInfo.isCompact) 20.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F2B66),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Chúng tôi đã gửi một liên kết xác thực từ Firebase đến email của bạn. Vui lòng mở email và nhấn vào liên kết đó để kích hoạt tài khoản.",
                fontFamily = BeVietnamPro,
                fontSize = if (windowInfo.isCompact) 13.sp else 14.sp,
                color = Color(0xFF334155),
                textAlign = TextAlign.Center,
                lineHeight = if (windowInfo.isCompact) 18.sp else 20.sp,
                modifier = Modifier.widthIn(max = 400.dp)
            )

            Spacer(Modifier.height(18.dp))

            // Frosted Card Container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.92f)
                ),
                border = BorderStroke(1.5.dp, Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (windowInfo.isCompact) 18.dp else 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hộp hiển thị Email của người dùng
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFDBEAFE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = LingoBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Email đăng ký:",
                                    fontFamily = BeVietnamPro,
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = email.ifBlank { "Tài khoản của bạn" },
                                    fontFamily = BeVietnamPro,
                                    fontSize = 14.5.sp,
                                    lineHeight = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 2
                                )
                            }
                        }
                    }

                    // Thông báo lỗi / trạng thái chưa xác thực
                    AnimatedVisibility(
                        visible = message != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (message != null) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFFEF2F2))
                                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Info,
                                                contentDescription = "Thông báo",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = message,
                                                fontFamily = BeVietnamPro,
                                                fontSize = 13.sp,
                                                color = Color(0xFF991B1B),
                                                lineHeight = 19.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                            }
                        }
                    }

                    // Thông báo gửi lại email thành công
                    AnimatedVisibility(
                        visible = resendSuccessMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        if (resendSuccessMessage != null) {
                            Column {
                                Spacer(Modifier.height(14.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF0FDF4))
                                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(14.dp))
                                        .padding(14.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Thành công",
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Text(
                                                text = resendSuccessMessage,
                                                fontFamily = BeVietnamPro,
                                                fontSize = 13.sp,
                                                color = Color(0xFF166534),
                                                lineHeight = 19.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Nút chính: Tôi đã xác thực
                    Button(
                        onClick = {
                            onClearMessage()
                            onCheckVerification()
                        },
                        enabled = !isChecking,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = LingoBlue),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Đang kiểm tra...",
                                fontFamily = BeVietnamPro,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Tôi đã xác thực",
                                fontFamily = BeVietnamPro,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Nút phụ: Gửi lại email xác thực (có countdown 60s)
                    OutlinedButton(
                        onClick = {
                            if (cooldownSeconds == 0 && !isResending) {
                                cooldownSeconds = 60
                                onResendEmail()
                            }
                        },
                        enabled = (cooldownSeconds == 0) && !isResending,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(25.dp),
                        border = BorderStroke(1.dp, if ((cooldownSeconds == 0) && !isResending) LingoBlue else Color(0xFFCBD5E1))
                    ) {
                        if (isResending) {
                            CircularProgressIndicator(
                                color = LingoBlue,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Đang gửi lại...",
                                fontFamily = BeVietnamPro,
                                fontSize = 14.sp,
                                color = LingoBlue
                            )
                        } else if (cooldownSeconds > 0) {
                            Text(
                                text = "Gửi lại email sau (${cooldownSeconds}s)",
                                fontFamily = BeVietnamPro,
                                fontSize = 14.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Text(
                                text = "Gửi lại email xác thực",
                                fontFamily = BeVietnamPro,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = LingoBlue
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Nút: Đăng xuất / Dùng tài khoản khác
                    TextButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Đăng xuất / Dùng tài khoản khác",
                            fontFamily = BeVietnamPro,
                            fontSize = 14.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
