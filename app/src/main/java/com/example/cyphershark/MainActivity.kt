package com.example.cyphershark

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.MotionEvent
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.example.syphershark.R
import com.example.syphershark.databinding.ActivityMainBinding

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var secretKeySpec : SecretKey

    private lateinit var clipboardManager: ClipboardManager
    private lateinit var clipData: ClipData


    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        val scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up)
        val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)


        binding.mainLock.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.mainLock.startAnimation(scaleUp)
                view.performClick()
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) binding.mainLock.startAnimation(scaleDown)
            true
        }

        binding.mainLock.setOnClickListener {
            if (binding.mainEdit.text.isEmpty()) Toast.makeText(this, "type message, please", Toast.LENGTH_SHORT).show()
            else {
                binding.mainText.text = encrypt(binding.mainEdit.text.toString())
                binding.mainEdit.text.clear()
            }
        }

        binding.mainUnlock.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.mainUnlock.startAnimation(scaleUp)
                view.performClick()
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) binding.mainUnlock.startAnimation(scaleDown)
            true
        }

        binding.mainUnlock.setOnClickListener {
            if (binding.mainEdit.text.isEmpty()) Toast.makeText(this, "type message, please", Toast.LENGTH_SHORT).show()
            else {
                binding.mainText.text = decrypt(binding.mainEdit.text.toString())
                binding.mainEdit.text.clear()
            }
        }

        binding.mainText.setOnClickListener {
            if (binding.mainText.text.isNotEmpty()) {
                clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipData = ClipData.newPlainText("cypher", binding.mainText.text)
                clipboardManager.setPrimaryClip(clipData)
                binding.mainText.text = ""
            }
        }

        binding.mainShark.setOnClickListener{
            if (binding.mainEdit.text.isNotEmpty()) binding.mainEdit.text.clear()
            if (binding.mainText.text.isNotEmpty()) binding.mainText.text = ""
        }

        binding.mainShark.setOnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                binding.mainShark.startAnimation(scaleUp)
                view.performClick()
            }
            if (motionEvent.action == MotionEvent.ACTION_UP) binding.mainShark.startAnimation(scaleDown)
            true
        }


    }

    private fun encrypt(message: String): String{
        val key = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES).apply {
            KeyGenParameterSpec.Builder(Constants.KEY, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_CBC).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7).setUserAuthenticationRequired(false).setRandomizedEncryptionRequired(true).build()
        }.generateKey()
        secretKeySpec = key
        var cypher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cypher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        var encryptBytes = cypher.doFinal(message.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptBytes)
    }

    private fun decrypt(message: String): String{
        return try {
            var cypher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cypher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            var decryptedBytes = cypher.doFinal(Base64.getDecoder().decode(message))
            String(decryptedBytes, Charsets.UTF_8)
        }catch (e:Exception){
            Toast.makeText(this, "incorrect cypher", Toast.LENGTH_SHORT).show()
            ""
        }
    }
}