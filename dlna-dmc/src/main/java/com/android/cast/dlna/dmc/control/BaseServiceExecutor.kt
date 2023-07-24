package com.android.cast.dlna.dmc.control

import android.os.Handler
import android.os.Looper
import com.android.cast.dlna.core.Utils.getStringTime
import com.android.cast.dlna.dmc.action.GetBrightness
import com.android.cast.dlna.dmc.action.SetBrightness
import org.fourthline.cling.controlpoint.ActionCallback
import org.fourthline.cling.controlpoint.ControlPoint
import org.fourthline.cling.model.action.ActionInvocation
import org.fourthline.cling.model.message.UpnpResponse
import org.fourthline.cling.model.meta.Service
import org.fourthline.cling.support.avtransport.callback.GetMediaInfo
import org.fourthline.cling.support.avtransport.callback.GetPositionInfo
import org.fourthline.cling.support.avtransport.callback.GetTransportInfo
import org.fourthline.cling.support.avtransport.callback.Pause
import org.fourthline.cling.support.avtransport.callback.Play
import org.fourthline.cling.support.avtransport.callback.Seek
import org.fourthline.cling.support.avtransport.callback.SetAVTransportURI
import org.fourthline.cling.support.avtransport.callback.Stop
import org.fourthline.cling.support.lastchange.LastChangeParser
import org.fourthline.cling.support.model.MediaInfo
import org.fourthline.cling.support.model.PositionInfo
import org.fourthline.cling.support.model.TransportInfo
import org.fourthline.cling.support.renderingcontrol.callback.GetMute
import org.fourthline.cling.support.renderingcontrol.callback.GetVolume
import org.fourthline.cling.support.renderingcontrol.callback.SetMute
import org.fourthline.cling.support.renderingcontrol.callback.SetVolume

/**
 *
 */
internal abstract class BaseServiceExecutor(
    private val controlPoint: ControlPoint,
    protected val service: Service<*, *>?,
) {
    private val handler = Handler(Looper.getMainLooper())

    protected fun invalidServiceAction(actionName: String?): Boolean = service?.getAction(actionName) == null

    protected fun executeAction(actionCallback: ActionCallback?) {
        controlPoint.execute(actionCallback)
    }

    fun subscribe(subscriptionCallback: SubscriptionListener, lastChangeParser: LastChangeParser) {
        controlPoint.execute(CastSubscriptionCallback(service, callback = subscriptionCallback, lastChangeParser = lastChangeParser))
    }

    protected fun <T> notifyResponse(listener: ServiceActionCallback<T>?, result: T? = null, exception: String? = null) {
        listener?.let { l ->
            notify { l.onResponse(ActionResponse(result, exception)) }
        }
    }

    private fun notify(runnable: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            handler.post(runnable)
        } else {
            runnable.run()
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // AvService
    // ---------------------------------------------------------------------------------------------------------
    internal class AVServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?,
    ) : BaseServiceExecutor(controlPoint, service), AVServiceAction {

        override fun cast(listener: ServiceActionCallback<String>?, uri: String, metadata: String?) {
            if (invalidServiceAction("SetAVTransportURI")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : SetAVTransportURI(service, uri, metadata) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = uri)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "cast failed.")
                }
            })
        }

        override fun play(listener: ServiceActionCallback<String>?) {
            if (invalidServiceAction("Play")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : Play(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = "Play")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "play failed.")
                }
            })
        }

        override fun pause(listener: ServiceActionCallback<String>?) {
            if (invalidServiceAction("Pause")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : Pause(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = "Pause")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "pause failed.")
                }
            })
        }

        override fun stop(listener: ServiceActionCallback<String>?) {
            if (invalidServiceAction("Stop")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : Stop(service) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = "Stop")
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "stop failed.")
                }
            })
        }

        override fun seek(listener: ServiceActionCallback<Long>?, position: Long) {
            if (invalidServiceAction("Seek")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : Seek(service, getStringTime(position)) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = position)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "seek failed.")
                }
            })
        }

        override fun getPositionInfo(listener: ServiceActionCallback<PositionInfo>?) {
            if (invalidServiceAction("GetPositionInfo")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetPositionInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, positionInfo: PositionInfo) {
                    notifyResponse(listener, result = positionInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "getPosition failed.")
                }
            })
        }

        override fun getMediaInfo(listener: ServiceActionCallback<MediaInfo>?) {
            if (invalidServiceAction("GetMediaInfo")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetMediaInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, mediaInfo: MediaInfo) {
                    notifyResponse(listener, result = mediaInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "getMedia failed.")
                }
            })
        }

        override fun getTransportInfo(listener: ServiceActionCallback<TransportInfo>?) {
            if (invalidServiceAction("GetTransportInfo")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetTransportInfo(service) {
                override fun received(invocation: ActionInvocation<*>?, transportInfo: TransportInfo) {
                    notifyResponse(listener, result = transportInfo)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "getTransport failed.")
                }
            })
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // RendererService
    // ---------------------------------------------------------------------------------------------------------
    internal class RendererServiceExecutorImpl(
        controlPoint: ControlPoint,
        service: Service<*, *>?,
    ) : BaseServiceExecutor(controlPoint, service), RendererServiceAction {

        override fun setVolume(listener: ServiceActionCallback<Int>?, volume: Int) {
            if (invalidServiceAction("SetVolume")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : SetVolume(service, volume.toLong()) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = null)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "setVolume failed.")
                }
            })
        }

        override fun getVolume(listener: ServiceActionCallback<Int>?) {
            if (invalidServiceAction("GetVolume")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetVolume(service) {
                override fun received(invocation: ActionInvocation<*>?, currentVolume: Int) {
                    notifyResponse(listener, result = currentVolume)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "getVolume failed.")
                }
            })
        }

        override fun setMute(listener: ServiceActionCallback<Boolean>?, mute: Boolean) {
            if (invalidServiceAction("SetMute")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : SetMute(service, mute) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = mute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "setMute failed.")
                }
            })
        }

        override fun isMute(listener: ServiceActionCallback<Boolean>?) {
            if (invalidServiceAction("GetMute")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetMute(service) {
                override fun received(invocation: ActionInvocation<*>?, currentMute: Boolean) {
                    notifyResponse(listener, result = currentMute)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "isMute failed.")
                }
            })
        }

        override fun setBrightness(listener: ServiceActionCallback<Int>?, percent: Int) {
            if (invalidServiceAction("SetBrightness")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : SetBrightness(service!!, percent.toLong()) {
                override fun success(invocation: ActionInvocation<*>?) {
                    notifyResponse(listener, result = percent)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "play failed.")
                }
            })
        }

        override fun getBrightness(listener: ServiceActionCallback<Int>?) {
            if (invalidServiceAction("GetBrightness")) {
                notifyResponse(listener, exception = "service not support this action.")
                return
            }
            executeAction(object : GetBrightness(service!!) {
                override fun received(actionInvocation: ActionInvocation<*>?, brightness: Int) {
                    notifyResponse(listener, result = brightness)
                }

                override fun failure(invocation: ActionInvocation<*>?, operation: UpnpResponse?, defaultMsg: String?) {
                    notifyResponse(listener, exception = defaultMsg ?: "getBrightness failed.")
                }
            })
        }
    }
}

class ActionResponse<T>(val data: T?, val exception: String?)