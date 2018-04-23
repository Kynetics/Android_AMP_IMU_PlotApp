package com.kynetics.ampsensors.device;

import java.io.InputStream;

public interface StreamConsumer {
    void onStreamOpen(InputStream inputStream, DataType dataType);
    void onStreamClosing();
}
