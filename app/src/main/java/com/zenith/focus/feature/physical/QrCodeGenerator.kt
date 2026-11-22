package com.zenith.focus.feature.physical

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * 100% offline QR code bitmap generator using ZXing Core.
 * Generates high-contrast QR codes for physical paper focus keys.
 */
object QrCodeGenerator {

    fun generateQrBitmap(content: String, size: Int = 512): Bitmap? {
        if (content.isBlank() || size <= 0) return null
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (_: Exception) {
            null
        }
    }
}
