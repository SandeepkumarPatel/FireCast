package com.google.plus.dougnlamb.firecast;

import java.io.IOException;

import android.graphics.Bitmap;

import com.google.cast.ApplicationChannel;
import com.google.cast.ApplicationMetadata;
import com.google.cast.ApplicationSession;
import com.google.cast.CastContext;
import com.google.cast.CastDevice;
import com.google.cast.MediaProtocolMessageStream;
import com.google.cast.SessionError;

public class FireCastSession {
	private CastDevice mDevice;
	private ApplicationSession mSession;
	private FireCastMessageStream mMessageStream;

	public CastDevice getDevice() {
		return mDevice;
	}

	public FireCastMessageStream getMessageStream() {
		return mMessageStream;
	}

	public void setDevice(CastDevice device) {
		mDevice = device;
	}

	private boolean mIsSessionReady;

	public boolean isSessionReady() {
		return mIsSessionReady;
	}

	private boolean mIsSessionStarting;

	public boolean isSessionStarting() {
		return mIsSessionStarting;
	}

	public void sendMedia(String mimeType, String url) throws Exception {
		sendMedia(mimeType, url, 0);
	}

	private String mMimeType;
	private String mUrl;
	private int mOrientation;

	private void sendPendingMessage() {
		try {
			if (mMimeType != null && mUrl != null) {
				mMessageStream.sendMedia(mMimeType, mUrl, mOrientation);
				mMimeType = null;
				mUrl = null;
				mOrientation = 0;
			}
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
		}
	}

	public void startSession(CastContext castContext) {

		mSession = new ApplicationSession(castContext, getDevice());
		mSession.setListener(new SessionListener());

		try {
			mSession.startSession("App Id");
		} catch (IOException e) {
			System.err.println(e);
		}

	}

	private class SessionListener implements ApplicationSession.Listener {
		@Override
		public void onSessionStarted(ApplicationMetadata appMetadata) {

			ApplicationChannel channel = mSession.getChannel();
			if (channel == null) {

				return;
			}
			mMessageStream = new FireCastMessageStream(
					"com.google.plus.dougnlamb.firecast");
			channel.attachMessageStream(mMessageStream);

			mIsSessionStarting = false;
			mIsSessionReady = true;

			sendPendingMessage();
		}

		@Override
		public void onSessionStartFailed(SessionError error) {
			mIsSessionStarting = false;
			mIsSessionReady = false;
		}

		@Override
		public void onSessionEnded(SessionError error) {
			mIsSessionStarting = false;
			mIsSessionReady = false;
		}
	}

	public void endSession() throws Exception {
		if (mSession != null) {
			mSession.endSession();
		}
	}

	public void sendMedia(String mimeType, String url, int orientation)
			throws Exception {
		if (mIsSessionReady) {
			mMessageStream.sendMedia(mimeType, url, orientation);
		} else {
			preparePendingMessage(mimeType, url, orientation);
		}

	}

	private void preparePendingMessage(String mimeType, String url,
			int orientation) {
		mMimeType = mimeType;
		mUrl = url;
		mOrientation = orientation;
	}

}
