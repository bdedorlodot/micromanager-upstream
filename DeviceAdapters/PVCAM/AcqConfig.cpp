#include "AcqConfig.h"


AcqConfig::AcqConfig() :
    FrameMetadataEnabled(false),
    CentroidsEnabled(false), CentroidsRadius(0), CentroidsCount(0),
    RoiCount(1), FanSpeedSetpoint(0),
    ColorProcessingEnabled(false),
    DebayerAlgMask(0), DebayerAlgMaskAuto(false), DebayerAlgInterpolation(0),
    PMode(0), CircBufEnabled(true), CircBufSizeAuto(true), CallbacksEnabled(true)
{

}