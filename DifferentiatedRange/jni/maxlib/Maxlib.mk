LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := maxlib
LOCAL_C_INCLUDES := $(LOCAL_SHARED_LIBRARIES)/PdCore/jni/libpd/pure-data/src
LOCAL_CFLAGS := -DPD
LOCAL_SRC_FILES := allow.c arbran.c arraycopy.c average.c beat.c beta.c bilex.c borax.c cauchy.c chord.c delta.c deny.c dist.c divide.c divmod.c edge.c expo.c fifo.c gauss.c gestalt.c history.c ignore.c iso.c lifo.c limit.c linear.c listfifo.c listfunnel.c match.c minus.c mlife.c multi.c nchange.c netclient.c netdist.c netrec.c netserver.c nroute.c pitch.c plus.c poisson.c pong.c pulse.c remote.c rewrap.c rhythm.c scale.c score.c speedlim.c split.c step.c subst.c sync.c temperature.c tilt.c timebang.c triang.c unroute.c urn.c velocity.c weibull.c wrap.c
LOCAL_LDLIBS := -L$(LOCAL_SHARED_LIBRARIES)/PdCore/libs/$(TARGET_ARCH_ABI) -lpd
include $(BUILD_SHARED_LIBRARY)
