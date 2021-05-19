package com.booksapp.auth

import android.content.Context
import android.content.pm.PackageManager
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.booksapp.App
import com.booksapp.data.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import java.security.*
import java.security.spec.X509EncodedKeySpec
import javax.security.auth.x500.X500Principal


class UserAuth(private val useStrongBox: Boolean) {
    private val keyAlias = "BooksAppUserKey"

    init {
        if (!keyExists()) {
            creteKeys()
        }
    }

    fun signReview(rating: Float, reviewText: String, time: Long): String {
        val reviewData = "$rating|$reviewText|$time"

        return signData(reviewData)
    }

    /**
     * @param key is Base64 URL safe encoded public key
     * @param signature is Base64 URL safe encoded signature
     */
    fun verifyReview(rating: Float, reviewText: String, time: Long, signature: String, key: String): Boolean {
        val reviewData = "$rating|$reviewText|$time"

        return verifyData(reviewData, signature, key)
    }

    /**
     * @param key is Base64 URL safe encoded public key
     * @param signature is Base64 URL safe encoded signature
     */
    fun verifyData(data: String, signature: String, key: String): Boolean {
        val publicKeyBytes: ByteArray = Base64.decode(key, Base64.URL_SAFE)
        val spec = X509EncodedKeySpec(publicKeyBytes)
        val fact = KeyFactory.getInstance("RSA")
        val publicKey = fact.generatePublic(spec)


        return Signature.getInstance("SHA512withRSA/PSS").run {
            initVerify(publicKey)
            update(data.toByteArray())
            verify(Base64.decode(signature, Base64.URL_SAFE))
        }
    }

    fun signData(data: String): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val entry: KeyStore.Entry = keyStore.getEntry(keyAlias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw IllegalStateException("Key pair is not generated")
        }
        val signature: ByteArray = Signature.getInstance("SHA512withRSA/PSS").run {
            initSign(entry.privateKey)
            update(data.toByteArray(Charset.defaultCharset()))
            sign()
        }

        return Base64.encodeToString(signature, Base64.URL_SAFE)
    }

    fun getPublicKey(): String {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }

        val cert = keyStore.getCertificate(keyAlias)
        val publicKey = cert.publicKey

        return Base64.encodeToString(publicKey.encoded, Base64.URL_SAFE)
    }

    private fun keyExists(): Boolean{
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }


        return keyStore.containsAlias(keyAlias)
    }

    private fun creteKeys() {
        val kpg = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore").apply {
            val certBuilder = KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                .setCertificateSubject(X500Principal("CN=BooksApp"))
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
                .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                .setRandomizedEncryptionRequired(true)
                .setIsStrongBoxBacked(useStrongBox) /* Enable StrongBox */

            initialize(
                certBuilder
                    .build()
            )
        }

        kpg.generateKeyPair()
    }

    companion object {
        private lateinit var instance: UserAuth
        var userId: Int? = null

        /**
         * For debug usage only!!
         */
        fun initAsNew(context: Context) {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
            keyStore.deleteEntry("BooksAppUserKey")

            init(context)
        }

        fun init(context: Context) {
            instance = UserAuth(hasStrongBox(context))

            GlobalScope.launch {
                val userDB = (context.applicationContext as App).db!!.reviewDao()

                val maybeUser = userDB.findUserByKey(instance.getPublicKey())

                userId = if (maybeUser == null) {
                    userDB.insert(User(null, instance.getPublicKey()))
                    userDB.findUserByKey(instance.getPublicKey())!!.userId
                } else {
                    maybeUser!!.userId
                }
            }
        }

        fun getInstance(): UserAuth {
            if (this::instance.isInitialized) {
                return instance
            } else {
                throw IllegalStateException("instance has to be initialized")
            }
        }

        private fun hasStrongBox(context: Context): Boolean {
            return context.packageManager
                .hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        }
    }
}