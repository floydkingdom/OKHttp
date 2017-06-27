package com.example.administrator.okhttp.util;

import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by Administrator on 2017/6/24.
 */

public class CountingRequestBody extends RequestBody {
    private RequestBody mDelegate;
    private Listener mListener;
    private CountingSink mCountingSink;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        mDelegate = delegate;
        mListener = listener;
    }

    /**
     * Returns the Content-Type header for this body.
     */
    @Nullable
    @Override
    public MediaType contentType() {
        return mDelegate.contentType();
    }

    @Override
    public long contentLength(){
        try {
            return mDelegate.contentLength();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Writes the content of this request to {@code out}.
     *
     * @param sink
     */
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        mCountingSink = new CountingSink(sink);
        BufferedSink bufferedSink = Okio.buffer(mCountingSink);
        mDelegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected final class CountingSink extends ForwardingSink{
        private long bytesWritten;

        public CountingSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            bytesWritten += byteCount;
            mListener.onRequestProgress(bytesWritten,contentLength());
        }
    }

    public interface Listener {
        void onRequestProgress(long bytesWritten,long contentLength);
    }
}
