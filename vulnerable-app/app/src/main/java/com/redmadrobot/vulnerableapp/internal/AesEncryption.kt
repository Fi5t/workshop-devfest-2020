package com.redmadrobot.vulnerableapp.internal

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


class AesEncryption {
    companion object {
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    }

    private val key by lazy {
        SecretKeySpec(Base64.decode("cmpVN21mNmhRNUVQWWdkYw==", Base64.DEFAULT), "AES")
    }

    private val iv by lazy {
        IvParameterSpec(Base64.decode("a2FzdzIzRGF3a2FtR05neA==", Base64.DEFAULT))
    }

    fun encrypt(plainText: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)

            cipher.init(Cipher.ENCRYPT_MODE, key, iv)

            Base64.encodeToString(cipher.doFinal(plainText.toByteArray()), Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun decrypt(cipherText: String): String {
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, iv)

            val bytes = Base64.decode(cipherText, Base64.DEFAULT)
            val plainText = cipher.doFinal(bytes)

            String(plainText)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
