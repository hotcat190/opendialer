package com.samsung.sip.pjsua

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.samsung.sip.callcontroller.PjsuaIncomingCallListener
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.alenajam.opendialer.core.common.SharedPreferenceHelper
import javax.inject.Inject
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AudioMedia
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.LogEntry
import org.pjsip.pjsua2.LogWriter
import org.pjsip.pjsua2.SrtpCrypto
import org.pjsip.pjsua2.SrtpCryptoVector
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pj_log_decoration
import org.pjsip.pjsua2.pjmedia_srtp_use
import org.pjsip.pjsua2.pjsip_transport_type_e
import javax.inject.Singleton

@Singleton
class PjsuaManager @Inject constructor(
    @ApplicationContext private  var context: Context
) {
    companion object {
        private val TAG = PjsuaManager::class.simpleName
        //const val SIP_LISTENING_PORT: Long = 6000
    }

    init {

    }
    private val endpoint = Endpoint()
    private var account: PjsuaAccount = PjsuaAccount(this)
    private val calls = mutableMapOf<Int, PjsuaCall>()

    private var isRunning: Boolean = false



    fun initialize() {
        Log.d(TAG, "$TAG initialize()")
        if (isRunning) {
            Log.d(TAG, "This $TAG instance is already initialized, returning early")
            return
        }

        /* Endpoint config */
        val epConfig = EpConfig()

        /* Setup our log writer */
        configureLogging(epConfig)

        endpoint.libCreate()
        endpoint.libInit(epConfig)

        /* Setup transport */
        createTransports()

        /* Setup account */
        createAccount()

        /* Lib start */
        endpoint.libStart()

        /* Prioritize PCMA for compatibility with demo's FreeSwitch & MicroSIP SDP negotiation */
        endpoint.codecSetPriority("PCMA/8000", 255)

        isRunning = true
    }


    fun shutdown() {
        Log.d(TAG, "$TAG shutdown()")
        if (!isRunning) {
            Log.d(TAG, "This $TAG instance is already shut down, returning early")
            return
        }
        try {
            endpoint.hangupAllCalls()
            account.shutdown()
            endpoint.libDestroy()
        } finally {
            calls.clear()
            isRunning = false
        }
    }

    fun makeCall(destination: String): PjsuaCall {
        Log.d(TAG, "$TAG makeCall")
        check(isRunning) {
            "PJSUA2 is not running"
        }

        Log.d(TAG, destination)

        /* Setup null audio (good for emulator) */
        endpoint.audDevManager().setNullDev()

        val call = PjsuaCall(
            manager = this,
            account = account,
            callId = -1,
        )

        val prm = CallOpParam(true)
        prm.opt.videoCount = 0
        prm.txOption.localUri

        call.makeCall(destination, prm)

        registerCall(call)
        return call
    }

    fun connectAudio(audioMedia: AudioMedia) {
        endpoint.audDevManager().captureDevMedia.startTransmit(audioMedia)
        audioMedia.startTransmit(endpoint.audDevManager().playbackDevMedia)
    }

    fun reconnect() {
        shutdown()
        initialize()
    }
    private fun registerCall(call: PjsuaCall) {
        try {
            calls[call.id] = call
        } catch (e: Exception) {
            println("Failed registering PJSUA2 call: $e")
            e.printStackTrace()
        }
    }

    internal fun removeCall(call: PjsuaCall) {
        calls.remove(call.id)
    }

    fun getCall(callId: Int): PjsuaCall? {
        return calls[callId]
    }

    private var incomingCallListener: PjsuaIncomingCallListener? = null

    fun setIncomingCallListener(listener: PjsuaIncomingCallListener) {
        incomingCallListener = listener
    }

    internal fun onIncomingCall(call: PjsuaCall) {
        registerCall(call)
        endpoint.audDevManager().setNullDev()
        incomingCallListener?.onIncomingCall(call)
    }

    private fun createAccount() {
        val ACC_DOMAIN =  SharedPreferenceHelper.getSipDomain(context)
        val ACC_USER =  SharedPreferenceHelper.getSipUser(context)

        val ACC_PASSWD =  SharedPreferenceHelper.getSipPassword(context)

        val ACC_ID_URI = "Kotlin <sip:$ACC_USER@${ACC_DOMAIN}>"
        val ACC_REGISTRAR = "sip:${ACC_DOMAIN}"
        val ACC_PROXY  = "sip:${ACC_DOMAIN};lr"

        val accConfig = AccountConfig()
        accConfig.idUri = ACC_ID_URI
        accConfig.regConfig.registrarUri = ACC_REGISTRAR
        accConfig.sipConfig.authCreds.add(
            AuthCredInfo(
                "Digest", "*", ACC_USER, 0,
                ACC_PASSWD
            )
        )
        accConfig.sipConfig.proxies.add(ACC_PROXY)

        accConfig.videoConfig.autoShowIncoming = false
        accConfig.videoConfig.autoTransmitOutgoing = false

        accConfig.mediaConfig.srtpUse = pjmedia_srtp_use.PJMEDIA_SRTP_DISABLED
        accConfig.mediaConfig.srtpSecureSignaling = 0

        println("Available crypto suites:")
        endpoint.srtpCryptoEnum().forEach { println(it) }
        accConfig.mediaConfig.srtpOpt.cryptos = SrtpCryptoVector(
            endpoint.srtpCryptoEnum()
            .filter { it.startsWith("AES_CM_128") }
            .map { SrtpCrypto().apply { name = it } }
        )

        println("Chosen crypto suites:")
        accConfig.mediaConfig.srtpOpt.cryptos.forEach { println(it.name) }

        account.create(accConfig)
    }

    private fun createTransports() {
        val SIP_LISTENING_PORT: Long = SharedPreferenceHelper.getSipPort(context)?.toLongOrNull() ?: 0L
        val udpConfig = TransportConfig()
        udpConfig.port = SIP_LISTENING_PORT
        endpoint.transportCreate(
            pjsip_transport_type_e.PJSIP_TRANSPORT_UDP,
            udpConfig
        )
    }

    /* Maintain reference to avoid auto garbage collecting */
    lateinit var logWriter: LogWriter

    private fun configureLogging(epConfig: EpConfig) {
        val logCfg = epConfig.logConfig
        this.logWriter = object : LogWriter() {
            override fun write(entry: LogEntry) {
                println(entry.msg)
            }
        }
        logCfg.writer = this.logWriter
            logCfg.decor = logCfg.decor and
                (pj_log_decoration.PJ_LOG_HAS_CR or
                        pj_log_decoration.PJ_LOG_HAS_NEWLINE).inv().toLong()
    }
}