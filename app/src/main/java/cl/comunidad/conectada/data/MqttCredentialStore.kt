package cl.comunidad.conectada.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import cl.comunidad.conectada.domain.Resident
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal class MqttCredentials(val host: String, val username: String, val password: CharArray)

internal class MqttCredentialStore(context: Context) {
    private val preferences = context.getSharedPreferences("mqtt_credentials", Context.MODE_PRIVATE)

    fun save(resident: Resident, credentials: MqttCredentials) {
        val scope = scope(resident)
        val key = storedKey() ?: KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
            }.generateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher.updateAAD(scope.toByteArray())
        val plaintext =
            JSONObject().put("host", credentials.host).put("username", credentials.username)
                .put("password", credentials.password.concatToString()).toString().toByteArray()
        val encrypted = try {
            cipher.doFinal(plaintext)
        } finally {
            plaintext.fill(0)
        }
        check(
            preferences.edit().putString(scope, encode(cipher.iv) + ":" + encode(encrypted))
                .commit()
        )
    }

    fun load(resident: Resident): MqttCredentials? {
        val scope = scope(resident)
        val stored = preferences.getString(scope, null) ?: return null
        val (iv, encrypted) = stored.split(":", limit = 2)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            checkNotNull(storedKey()),
            GCMParameterSpec(128, decode(iv))
        )
        cipher.updateAAD(scope.toByteArray())
        val plaintext = cipher.doFinal(decode(encrypted))
        return try {
            val data = JSONObject(plaintext.decodeToString())
            MqttCredentials(
                data.getString("host"), data.getString("username"),
                data.getString("password").toCharArray()
            )
        } finally {
            plaintext.fill(0)
        }
    }

    fun forget(resident: Resident) {
        check(preferences.edit().remove(scope(resident)).commit())
    }

    private fun scope(resident: Resident) =
        JSONArray().put(resident.uid).put(resident.communityId).toString()

    private fun storedKey() = KeyStore.getInstance("AndroidKeyStore").run {
        load(null)
        getKey(KEY_ALIAS, null) as? SecretKey
    }

    private fun encode(value: ByteArray) = Base64.encodeToString(value, Base64.NO_WRAP)
    private fun decode(value: String) = Base64.decode(value, Base64.NO_WRAP)

    private companion object {
        const val KEY_ALIAS = "neighborloop-mqtt-v1"
    }
}
